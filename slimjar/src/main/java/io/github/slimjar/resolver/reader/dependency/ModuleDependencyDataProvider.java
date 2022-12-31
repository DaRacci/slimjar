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

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public record ModuleDependencyDataProvider(
    @NotNull DependencyReader dependencyReader,
    @NotNull URL moduleUrl
) implements DependencyDataProvider {

    @Override
    public @NotNull DependencyData get() {
        try {
            final URL depFileURL = getURL();
            final var connection = depFileURL.openConnection();

            if (!(connection instanceof final JarURLConnection jarURLConnection)) {
                throw new AssertionError("Invalid Module URL provided(Non-Jar File)");
            }

            final var jarFile = jarURLConnection.getJarFile();
            final var dependencyFileEntry = jarFile.getEntry("slimjar.json");
            if (dependencyFileEntry == null) {
                return new DependencyData(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet()
                );
            }

            try (final var inputStream = jarFile.getInputStream(dependencyFileEntry)) {
                return dependencyReader.read(inputStream);
            }
        } catch (final IOException err) {
            throw new ResolutionException("Failed to get dependency data.", err);
        }
    }

    /**
     * Public for tests.
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull URL getURL() throws MalformedURLException {
        return new URL("jar:file:" + moduleUrl.getFile() + "!/slimjar.json");
    }
}
