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

package io.github.slimjar.resolver.enquirer;

import io.github.slimjar.logging.LogDispatcher;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.strategy.PathResolutionStrategy;
import io.github.slimjar.resolver.pinger.URLPinger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

public record PingingRepositoryEnquirer(
    @NotNull Repository repository,
    @NotNull PathResolutionStrategy dependencyURLCreationStrategy,
    @NotNull PathResolutionStrategy checksumURLCreationStrategy,
    @NotNull PathResolutionStrategy pomURLCreationStrategy,
    @NotNull URLPinger urlPinger

) implements RepositoryEnquirer {
    private static final @NotNull ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();

    @Override
    @Contract(pure = true)
    public @NotNull ResolutionResult enquire(final @NotNull Dependency dependency) {
        LOGGER.debug("Enquiring repositories to find %s", dependency.artifactId());

        return dependencyURLCreationStrategy.pathTo(repository, dependency)
                .stream()
                .map(this::createURL)
                .filter(urlPinger::ping)
                .findFirst()
                .map(url -> {
                    final var resolvedChecksum = checksumURLCreationStrategy.pathTo(repository, dependency)
                            .parallelStream()
                            .map(this::createURL)
                            .filter(urlPinger::ping)
                            .findFirst()
                            .orElse(null);
                    return new ResolutionResult(repository, url, resolvedChecksum, false, true);
                }).orElseGet(() -> pomURLCreationStrategy.pathTo(repository, dependency).parallelStream()
                        .map(this::createURL)
                        .filter(urlPinger::ping)
                        .findFirst()
                        .map(url -> new ResolutionResult(repository, null, null, true, false))
                        .orElse(null)
        );
    }

    @Override
    public String toString() {
        return repository.url().toString();
    }

    @Contract(pure = true)
    private @Nullable URL createURL(@NotNull final String path) {
        try {
            return new URL(repository.url(), path);
        } catch (final MalformedURLException ignored) {
            LOGGER.error("Failed to create URL for %s", path);
            return null;
        }
    }
}
