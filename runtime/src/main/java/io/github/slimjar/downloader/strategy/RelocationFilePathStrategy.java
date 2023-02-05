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

package io.github.slimjar.downloader.strategy;

import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.data.Dependency;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public final class RelocationFilePathStrategy implements FilePathStrategy {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    @NotNull private static final String DEPENDENCY_FILE_FORMAT = "%s/%s/%s/%s/relocated/%5$s/%3$s-%4$s.jar";
    @NotNull private final File rootDirectory;
    @NotNull private final String applicationName;

    @Contract(pure = true)
    private RelocationFilePathStrategy(
        @NotNull final File rootDirectory,
        @NotNull final String applicationName
    ) {
        this.rootDirectory = rootDirectory;
        this.applicationName = applicationName;
    }

    @Override
    @Contract(pure = true)
    public @NotNull File selectFileFor(@NotNull final Dependency dependency) {
        final var extendedVersion = Optional.ofNullable(dependency.snapshotId()).map(s -> "-" + s).orElse("");
        final var path = String.format(
            DEPENDENCY_FILE_FORMAT,
            rootDirectory.getPath(),
            dependency.groupId().replace('.','/'),
            dependency.artifactId(),
            dependency.version() + extendedVersion,
            applicationName
        );

        LOGGER.debug("Selected file for relocated %s at %s. ", dependency.artifactId(), path);
        return new File(path);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull FilePathStrategy createStrategy(
        @NotNull final File rootDirectory,
        @NotNull final String applicationName
    ) throws IllegalStateException {
        FilePathStrategy.validateDirectory(rootDirectory);
        return new RelocationFilePathStrategy(rootDirectory, applicationName);
    }
}
