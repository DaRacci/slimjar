package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class RelocatorException extends SlimJarException {

    @Contract(pure = true)
    public RelocatorException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public RelocatorException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) { super(message, cause); }
}
