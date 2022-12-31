package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VerificationException extends DownloaderException {

    @Contract(pure = true)
    public VerificationException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public VerificationException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) {
        super(message, cause);
    }
}
