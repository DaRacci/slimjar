package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed class SlimJarException extends RuntimeException permits ModuleExtractorException, NotComparableDependencyException, OutputWriterException {
    @Contract(pure = true)
    public SlimJarException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public SlimJarException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) { super(message, cause); }
}
