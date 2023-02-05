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

package io.github.slimjar.util;

import io.github.slimjar.app.module.ModuleExtractor;
import io.github.slimjar.exceptions.ResolutionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Modules {

    private Modules() { }

    public static @NotNull URL findModule(@NotNull final String moduleName) {
        final ClassLoader classLoader = Modules.class.getClassLoader();
        return Objects.requireNonNull(classLoader.getResource(moduleName + ".isolated-jar"));
    }

    public static @NotNull URL[] extract(
        @NotNull final ModuleExtractor extractor,
        @NotNull final Collection<String> modules
    ) {
        final var urls = new URL[modules.size()];
        int index = 0;
        for (final var moduleName : modules) {
            final var modulePath = findModule(moduleName);
            final var extractedModule = extractor.extractModule(modulePath, moduleName);
            urls[index++] = extractedModule;
        }

        return urls;
    }

    public static @NotNull Set<String> findLocalModules() throws ResolutionException {
        final var url = Modules.class.getProtectionDomain().getCodeSource().getLocation();
        final Path resourcesPath;
        try {
            resourcesPath = Paths.get(url.toURI());
        } catch (final URISyntaxException err) {
            // Shouldn't be possible.
            throw new ResolutionException("Failed to resolve local modules", err);
        }

        try (final var stream = Files.walk(resourcesPath, 1)) {
            return stream.filter(path -> path.endsWith(".isolated-jar"))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toUnmodifiableSet());
        } catch (final IOException err) {
            throw new ResolutionException("Encountered exception while walking files.", err);
        }
    }
}
