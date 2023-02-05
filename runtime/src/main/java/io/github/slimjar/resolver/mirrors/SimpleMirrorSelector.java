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

package io.github.slimjar.resolver.mirrors;

import io.github.slimjar.resolver.data.Mirror;
import io.github.slimjar.resolver.data.Repository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public final class SimpleMirrorSelector implements MirrorSelector {
    @Override
    @Contract(pure = true)
    public @NotNull Collection<@NotNull Repository> select(
        final @NotNull Collection<@NotNull Repository> mainRepositories,
        final @NotNull Collection<@NotNull Mirror> mirrors
    ) {
        final var originals = mirrors.stream()
                .map(Mirror::original)
                .collect(Collectors.toSet());
        final var resolved = mainRepositories.stream()
                .filter(repo -> !originals.contains(repo.url()))
                .collect(Collectors.toSet());
        final var mirrored = mirrors.stream()
                .map(Mirror::mirroring)
                .map(Repository::new)
                .collect(Collectors.toSet());

        resolved.addAll(mirrored);
        return resolved;
    }
}
