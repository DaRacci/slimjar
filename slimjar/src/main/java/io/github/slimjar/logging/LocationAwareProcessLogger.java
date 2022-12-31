package io.github.slimjar.logging;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LocationAwareProcessLogger implements ProcessLogger {
    @NotNull private final ProcessLogger logger;
    @NotNull private final Class<?> location;

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ProcessLogger wrapping(@NotNull final ProcessLogger logger) {
        return new LocationAwareProcessLogger(logger, StackWalker.getInstance().getCallerClass());
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull ProcessLogger generic() {
        return new LocationAwareProcessLogger(LogDispatcher.getMediatingLogger(), StackWalker.getInstance().getCallerClass());
    }

    @Contract(pure = true)
    private LocationAwareProcessLogger(
        @NotNull final ProcessLogger logger,
        @NotNull final Class<?> location
    ) {
        this.logger = logger;
        this.location = location;
    }

    @Override
    public void info(
        @NotNull final String message,
        @Nullable final Object... args
    ) { logger.info(formatMessage(message), args); }

    @Override
    public void error(
        @NotNull final String message,
        @Nullable final Object... args
    ) { logger.error(formatMessage(message), args); }

    @Override
    public void debug(
        @NotNull final String message,
        @Nullable final Object... args
    ) { logger.debug(formatMessage(message), args); }

    @Contract(pure = true)
    private @NotNull String formatMessage(@NotNull final String message) {
        return String.format("[%s] %s", location.getSimpleName(), message);
    }
}
