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

package io.github.slimjar.resolver;

import io.github.slimjar.resolver.data.Repository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

public final class ResolutionResult {
    @NotNull private final Repository repository;
    @NotNull private final URL dependencyURL;
    @Nullable private final URL checksumURL;
    private final boolean aggregator;
    private transient boolean checked;

    @Contract(pure = true)
    public ResolutionResult(
        @NotNull final Repository repository,
        @NotNull final URL dependencyURL,
        @Nullable final URL checksumURL,
        final boolean aggregator,
        final boolean checked
    ) {
        this.repository = repository;
        this.dependencyURL = dependencyURL;
        this.checksumURL = checksumURL;
        this.aggregator = aggregator;
        this.checked = checked;

        if (!aggregator) {
            Objects.requireNonNull(dependencyURL, "Resolved URL must not be null for non-aggregator dependencies");
        }
    }

    public @NotNull Repository repository() {
        return repository;
    }

    public @NotNull URL dependencyURL() {
        return dependencyURL;
    }

    public @Nullable URL checksumURL() {
        return checksumURL;
    }

    public boolean aggregator() {
        return aggregator;
    }

    public boolean checked() {
        return checked;
    }

    public void setChecked() {
        this.checked = true;
    }

    @Override
    @Contract(pure = true)
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (!(o instanceof ResolutionResult otherResult)) return false;

        // String comparison to avoid all blocking calls
        return Objects.equals(dependencyURL.toString(), otherResult.dependencyURL.toString()) &&
            Objects.equals(checksumURL != null ? checksumURL.toString() : null, otherResult.checksumURL != null ? otherResult.checksumURL.toString() : null) &&
            aggregator == otherResult.aggregator &&
            checked == otherResult.checked;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return Objects.hash(
            dependencyURL.toString(),
            checksumURL != null ? checksumURL.toString() : null,
            aggregator,
            checked
        );
    }
}
