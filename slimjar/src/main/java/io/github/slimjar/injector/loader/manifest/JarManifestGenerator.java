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

package io.github.slimjar.injector.loader.manifest;

import io.github.slimjar.exceptions.InjectorException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public final class JarManifestGenerator implements ManifestGenerator {
    @NotNull private final Map<String, String> attributes = new HashMap<>();
    @NotNull private final URI jarURI;

    @Contract(pure = true)
    public JarManifestGenerator(@NotNull final URI jarURI) {
        this.jarURI = jarURI;
    }

    @Override
    @Contract(value = "_, _ -> this", mutates = "this")
    public @NotNull ManifestGenerator attribute(
        final @NotNull String key,
        final @NotNull String value
    ) {
        attributes.put(key, value);
        return this;
    }

    @Override
    public void generate() throws InjectorException {
        final var env = Map.of("create", "true");

        final var uri = URI.create(String.format("jar:%s", jarURI));
        try (final var fs = FileSystems.newFileSystem(uri, env)) {
            final Path nf = fs.getPath("META-INF/MANIFEST.MF");
            Files.createDirectories(nf.getParent());
            try (final var writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                for (final var entry : attributes.entrySet()) {
                    writer.write(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
                }
            }
        } catch (final IOException err) {
            throw new InjectorException("Failed to generate manifest.", err);
        }
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ManifestGenerator with(@NotNull final URI uri) {
        return new JarManifestGenerator(uri);
    }
}
