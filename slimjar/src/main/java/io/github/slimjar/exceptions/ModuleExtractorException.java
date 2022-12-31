package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed class ModuleExtractorException extends RuntimeException permits ModuleNotFoundException {

    @Contract(pure = true)
    public ModuleExtractorException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public ModuleExtractorException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) { super(message, cause); }

    @Contract(pure = true)
    public ModuleExtractorException(@NotNull final Throwable cause) {
        super(cause);
    }
}
