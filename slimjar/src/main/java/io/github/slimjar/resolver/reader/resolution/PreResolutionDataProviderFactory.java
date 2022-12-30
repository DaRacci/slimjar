package io.github.slimjar.resolver.reader.resolution;


import org.jetbrains.annotations.NotNull;

import java.net.URL;

@FunctionalInterface
public interface PreResolutionDataProviderFactory {
    @NotNull PreResolutionDataProvider create(@NotNull final URL resolutionFileURL);
}
