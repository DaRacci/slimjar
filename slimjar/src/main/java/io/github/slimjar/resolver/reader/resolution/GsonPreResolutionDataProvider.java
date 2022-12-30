package io.github.slimjar.resolver.reader.resolution;

import io.github.slimjar.resolver.ResolutionResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public final class GsonPreResolutionDataProvider implements PreResolutionDataProvider {
    @NotNull private final PreResolutionDataReader resolutionDataReader;
    @NotNull private final URL resolutionFileURL;
    @Nullable private Map<@NotNull String, @NotNull ResolutionResult> cachedData = null;

    @Contract(pure = true)
    public GsonPreResolutionDataProvider(
        @NotNull final PreResolutionDataReader resolutionDataReader,
        @NotNull final URL resolutionFileURL
    ) {
        this.resolutionDataReader = resolutionDataReader;
        this.resolutionFileURL = resolutionFileURL;
    }

    @Override
    @Contract(pure = true)
    public @NotNull Map<@NotNull String, @NotNull ResolutionResult> get() throws IOException, ReflectiveOperationException {
        if (cachedData != null) {
            return cachedData;
        }

        try (final var is = resolutionFileURL.openStream()) {
            cachedData = resolutionDataReader.read(is);
            return cachedData;
        } catch (final IOException | ReflectiveOperationException ignored) {
            return Collections.emptyMap();
        }
    }
}
