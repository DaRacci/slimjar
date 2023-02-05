package io.github.slimjar.exceptions;

import org.jetbrains.annotations.NotNull;

public final class ResolutionException extends SlimJarException {
    public ResolutionException(@NotNull final String message) {
        super(message);
    }

    public ResolutionException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) {
        super(message, cause);
    }
}
