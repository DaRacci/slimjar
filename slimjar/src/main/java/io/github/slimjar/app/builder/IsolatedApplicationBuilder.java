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

package io.github.slimjar.app.builder;

import io.github.slimjar.app.Application;
import io.github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import io.github.slimjar.util.Modules;
import io.github.slimjar.util.Parameters;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class IsolatedApplicationBuilder extends ApplicationBuilder {
    @NotNull private final IsolationConfiguration isolationConfiguration;
    @NotNull private final Object[] arguments;

    @Contract(pure = true)
    public IsolatedApplicationBuilder(
        @NotNull final String applicationName,
        @NotNull final IsolationConfiguration isolationConfiguration,
        @NotNull final Object... arguments
    ) {
        super(applicationName);
        this.isolationConfiguration = isolationConfiguration;
        this.arguments = arguments.clone();
    }

    @Override
    @Contract(value = "-> new", mutates = "this")
    public @NotNull Application buildApplication() {
        final var injector = createInjector();
        final var moduleUrls = Modules.extract(isolationConfiguration.moduleExtractor(), isolationConfiguration.modules());

        final var classLoader = new IsolatedInjectableClassLoader(moduleUrls, Collections.singleton(Application.class), isolationConfiguration.parentClassloader());

        final var dataProvider = getDataProviderFactory().create(getDependencyFileUrl());
        final var selfDependencyData = dataProvider.get();

        final var preResolutionDataProvider = getPreResolutionDataProviderFactory().create(getPreResolutionFileUrl());
        final var preResolutionResultMap = preResolutionDataProvider.get();

        injector.inject(classLoader, selfDependencyData, preResolutionResultMap);

        for (final var module : moduleUrls) {
            final var moduleDataProvider = getModuleDataProviderFactory().create(module);
            final var dependencyData = moduleDataProvider.get();
            // TODO:: fetch isolated pre-resolutions
            injector.inject(classLoader, dependencyData, preResolutionResultMap);
        }

        final var applicationClass = (Class<Application>) Class.forName(isolationConfiguration.applicationClass(), true, classLoader);

        // TODO:: Fix constructor resolution
        return applicationClass.getConstructor(Parameters.typesFrom(arguments)).newInstance(arguments);
    }

}
