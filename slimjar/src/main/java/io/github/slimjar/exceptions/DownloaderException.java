package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed class DownloaderException extends SlimJarException permits UnresolvedDependencyException, VerificationException {
    @Contract(pure = true)
    public DownloaderException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public DownloaderException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) {
        super(message, cause);
    }
}
