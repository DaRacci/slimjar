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

package io.github.slimjar.resolver.reader.dependency;

import io.github.slimjar.exceptions.ResolutionException;
import io.github.slimjar.resolver.data.DependencyData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;

public final class URLDependencyDataProvider implements DependencyDataProvider {
    @NotNull private final DependencyReader dependencyReader;
    @NotNull private final URL depFileURL;
    @Nullable private DependencyData cachedData = null;

    @Contract(pure = true)
    public URLDependencyDataProvider(
        @NotNull final DependencyReader dependencyReader,
        @NotNull final URL depFileURL
    ) {
        this.dependencyReader = dependencyReader;
        this.depFileURL = depFileURL;
    }

    @Contract(pure = true)
    public @NotNull DependencyReader getDependencyReader() {
        return dependencyReader;
    }

    @Override
    @Contract(pure = true)
    public @NotNull DependencyData get() {
        if (cachedData != null) {
            return cachedData;
        }

        try {
            final var connection = depFileURL.openConnection();
            // Do not cache so we can re-read (ex during some form of reload) from this jar file if it changes.
            connection.setUseCaches(false);

            try (final var is = connection.getInputStream()) {
                cachedData = dependencyReader.read(is);
                return cachedData;
            }
        } catch (final IOException err) {
            throw new ResolutionException("Unable to read dependency data.", err);
        }
    }
}
