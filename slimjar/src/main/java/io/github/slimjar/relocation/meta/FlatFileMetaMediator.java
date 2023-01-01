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

package io.github.slimjar.relocation.meta;

import io.github.slimjar.exceptions.RelocatorException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FlatFileMetaMediator implements MetaMediator {
    @NotNull private final Path metaFolderPath;

    public FlatFileMetaMediator(@NotNull final Path metaFolderPath) {
        this.metaFolderPath = metaFolderPath;
    }

    @Override
    public @Nullable String readAttribute(@NotNull final String name) throws RelocatorException {
        final var attributeFile = metaFolderPath.resolve(name);
        if (Files.notExists(attributeFile) || Files.isDirectory(attributeFile)) return null;
        try {
            return Files.readString(attributeFile);
        } catch (final IOException err) {
            throw new RelocatorException("Failed to read attribute " + name, err);
        }
    }

    @Override
    public void writeAttribute(
        @NotNull final String name,
        @NotNull final String value
    ) throws RelocatorException {
        final var attributeFile = metaFolderPath.resolve(name);
        try {
            Files.deleteIfExists(attributeFile);
            Files.createFile(attributeFile);
            Files.write(attributeFile, value.getBytes());
        } catch (final IOException err) {
            throw new RelocatorException("Failed to write attribute " + name, err);
        }
    }
}
