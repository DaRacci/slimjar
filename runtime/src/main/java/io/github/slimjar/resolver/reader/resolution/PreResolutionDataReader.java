package io.github.slimjar.resolver.reader.resolution;

import io.github.slimjar.resolver.ResolutionResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@FunctionalInterface
public interface PreResolutionDataReader {
    @NotNull Map<@NotNull String, @NotNull ResolutionResult> read(@NotNull final InputStream inputStream) throws ReflectiveOperationException;
}
