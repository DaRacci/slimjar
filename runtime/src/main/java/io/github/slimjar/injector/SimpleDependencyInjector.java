//
// MIT License
//
// Copyright (c) 2021 Vaishnav Anil
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.slimjar.injector;

import io.github.slimjar.exceptions.InjectorException;
import io.github.slimjar.injector.helper.InjectionHelper;
import io.github.slimjar.injector.helper.InjectionHelperFactory;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.DependencyData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SimpleDependencyInjector implements DependencyInjector {
    private final InjectionHelperFactory injectionHelperFactory;
    private final List<Dependency> processingDependencies = Collections.synchronizedList(new ArrayList<>());

    public SimpleDependencyInjector(final InjectionHelperFactory injectionHelperFactory) {
        this.injectionHelperFactory = injectionHelperFactory;
    }

    @Override
    public void inject(
        @NotNull final Injectable injectable,
        @NotNull final DependencyData data,
        @NotNull final Map<String, ResolutionResult> preResolvedResults
    ) {
        final var helper = injectionHelperFactory.create(data, preResolvedResults);
        injectDependencies(injectable, helper, data.dependencies());
    }

    // TODO -> Download dependencies in parallel then check the checksums after instead of during the download
    private void injectDependencies(
        @NotNull final Injectable injectable,
        @NotNull final InjectionHelper injectionHelper,
        @NotNull final Collection<Dependency> dependencies
    ) throws InjectorException {
        dependencies.parallelStream()
            .filter(dependency -> !injectionHelper.isInjected(dependency))
            .forEach(dependency -> {
                if (processingDependencies.contains(dependency)) return;
                processingDependencies.add(dependency);

                injectionHelper.fetch(dependency).ifPresent(jarFile -> {
                    try {
                        injectable.inject(jarFile.toURI().toURL());
                    } catch (final MalformedURLException err) { /* Should never happen */ }
                    injectDependencies(injectable, injectionHelper, dependency.transitive());
                });

                processingDependencies.remove(dependency);
            });
    }

}
