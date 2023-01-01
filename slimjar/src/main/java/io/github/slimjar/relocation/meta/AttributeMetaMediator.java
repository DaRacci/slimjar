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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public final class AttributeMetaMediator implements MetaMediator {
    @NotNull private final UserDefinedFileAttributeView view;

    public AttributeMetaMediator(@NotNull final Path path) {
        this.view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
    }

    @Override
    public @Nullable String readAttribute(@NotNull final String name) {
        try {
            final var buf = ByteBuffer.allocate(view.size(name));
            view.read(name, buf);
            buf.flip();
            return Charset.defaultCharset().decode(buf).toString();
        } catch (final IOException ignored) {
            return null;
        }
    }

    @Override
    public void writeAttribute(
        @NotNull final String name,
        @NotNull final String value
    ) {
        try {
            view.write(name, Charset.defaultCharset().encode(value));
        } catch (final IOException ignored) { /* Ignored */ }
    }
}
