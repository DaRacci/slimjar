package io.github.slimjar.resolver.reader.resolution;

import io.github.slimjar.resolver.ResolutionResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface PreResolutionDataProvider {
    @NotNull Map<@NotNull String, @NotNull ResolutionResult> get() throws IOException, ReflectiveOperationException;
}
