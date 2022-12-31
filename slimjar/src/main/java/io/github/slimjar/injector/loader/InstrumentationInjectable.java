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

package io.github.slimjar.injector.loader;

import io.github.slimjar.exceptions.InjectorException;
import io.github.slimjar.exceptions.RelocatorException;
import io.github.slimjar.injector.agent.ByteBuddyInstrumentationFactory;
import io.github.slimjar.injector.agent.InstrumentationFactory;
import io.github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import io.github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import io.github.slimjar.resolver.data.Repository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.jar.JarFile;

public final class InstrumentationInjectable implements Injectable {

    @NotNull private final Instrumentation instrumentation;

    public InstrumentationInjectable(@NotNull final Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void inject(@NotNull final URL url) throws InjectorException {
        try {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(new File(url.toURI())));
        } catch (final IOException | URISyntaxException e) {
            throw new InjectorException("Failed to inject %s into classLoader.".formatted(url), e);
        }
    }

    public static @NotNull Injectable create(
        @NotNull final Path downloadPath,
        @NotNull final Collection<Repository> repositories
    ) throws InjectorException {
        final JarRelocatorFacadeFactory jarRelocatorFacadeFactory;
        try {
            jarRelocatorFacadeFactory = ReflectiveJarRelocatorFacadeFactory.create(downloadPath, repositories);
        } catch (final RelocatorException err) {
            throw new InjectorException("Failed to create relocator facade factory.", err);
        }

        return create(new ByteBuddyInstrumentationFactory(jarRelocatorFacadeFactory));
    }

    @Contract("_ -> new")
    public static @NotNull Injectable create(@NotNull final InstrumentationFactory factory) throws InjectorException {
        return new InstrumentationInjectable(factory.create());
    }
}
