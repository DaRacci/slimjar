package io.github.slimjar.resolver.reader.resolution;

import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.reader.facade.GsonFacade;
import io.github.slimjar.resolver.reader.facade.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public record GsonPreResolutionDataReader(
    @NotNull GsonFacade gsonFacade
) implements PreResolutionDataReader {

    @Override
    @Contract(pure = true)
    public @NotNull Map<String, ResolutionResult> read(final @NotNull InputStream inputStream) throws ReflectiveOperationException {
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final Type rawType = new TypeToken<Map<String, ResolutionResult>>(){}.rawType();
        return gsonFacade.fromJson(inputStreamReader, rawType);
    }
}
