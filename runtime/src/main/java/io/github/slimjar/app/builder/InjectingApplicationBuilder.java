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

import io.github.slimjar.app.AppendingApplication;
import io.github.slimjar.app.Application;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.injector.loader.InjectableFactory;
import io.github.slimjar.resolver.data.Repository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Function;

public final class InjectingApplicationBuilder extends ApplicationBuilder {
    @NotNull private final Function<ApplicationBuilder, Injectable> injectableSupplier;

    @Contract(pure = true)
    public InjectingApplicationBuilder(
        @NotNull final String applicationName,
        @NotNull final Injectable injectable
    ) { this(applicationName, it -> injectable); }

    @Contract(pure = true)
    public InjectingApplicationBuilder(
        @NotNull final String applicationName,
        @NotNull final Function<ApplicationBuilder, Injectable> injectableSupplier
    ) {
        super(applicationName);
        this.injectableSupplier = injectableSupplier;
    }

    @Override
    @Contract(value = "-> new", mutates = "this")
    public @NotNull Application buildApplication() {
        final var dataProvider = getDataProviderFactory().create(getDependencyFileUrl());
        final var dependencyData = dataProvider.get();
        final var dependencyInjector = createInjector();

        final var preResolutionDataProvider = getPreResolutionDataProviderFactory().create(getPreResolutionFileUrl());
        final var preResolutionResultMap = preResolutionDataProvider.get();

        dependencyInjector.inject(injectableSupplier.apply(this), dependencyData, preResolutionResultMap);
        return new AppendingApplication();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ApplicationBuilder createAppending(@NotNull final String applicationName) {
        final var classLoader = ApplicationBuilder.class.getClassLoader();
        return createAppending(applicationName, classLoader);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ApplicationBuilder createAppending(
        @NotNull final String applicationName,
        @NotNull final ClassLoader classLoader
    ) {
        return new InjectingApplicationBuilder(applicationName, (final ApplicationBuilder builder) -> InjectableFactory.create(
            builder.getDownloadDirectoryPath(),
            Collections.singleton(Repository.central()),
            classLoader
        ));
    }
}

