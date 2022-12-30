package io.github.slimjar.resolver.reader.resolution;

import io.github.slimjar.resolver.reader.facade.GsonFacade;
import io.github.slimjar.resolver.reader.facade.GsonFacadeFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public record GsonPreResolutionDataProviderFactory(
    @NotNull GsonFacade gsonFacade
) implements PreResolutionDataProviderFactory {

    @Contract(pure = true)
    public GsonPreResolutionDataProviderFactory(@NotNull final GsonFacadeFactory gson) throws ReflectiveOperationException {
        this(gson.createFacade());
    }

    @Override
    @Contract(pure = true)
    public @NotNull PreResolutionDataProvider create(@NotNull final URL resolutionFileURL) {
        final var resolutionDataReader = new GsonPreResolutionDataReader(gsonFacade);
        return new GsonPreResolutionDataProvider(resolutionDataReader, resolutionFileURL);
    }
}
