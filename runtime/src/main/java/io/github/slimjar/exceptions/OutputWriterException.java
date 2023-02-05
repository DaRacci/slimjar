package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class OutputWriterException extends SlimJarException {

    @Contract
    public OutputWriterException(@NotNull final String message) {
        super(message);
    }

    public OutputWriterException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) { super(message, cause); }
}
