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

package io.github.slimjar.resolver.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public record Repository(@NotNull URL url) {
    @NotNull public static final String CENTRAL_URL = "https://repo1.maven.org/maven2/";
    private static Repository centralInstance;

    public static @NotNull Repository central() {
        if (centralInstance == null) {
            try {
                centralInstance = new Repository(new URL(CENTRAL_URL));
            } catch (final MalformedURLException ignored) {
                // This shouldn't ever happen, just caught to make compiler happy.
            }
        }

        return centralInstance;
    }

    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository otherRepository)) return false;

        return Objects.equals(url, otherRepository.url);
    }

    @Override
    @Contract(pure = true)
    public @NotNull String toString() {
        return "Repository{" + ", url='" + url + '\'' + '}';
    }
}
