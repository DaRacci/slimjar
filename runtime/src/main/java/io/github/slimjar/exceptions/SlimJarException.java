package io.github.slimjar.exceptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed class SlimJarException extends RuntimeException permits DownloaderException, InjectorException, ModuleExtractorException, NotComparableDependencyException, OutputWriterException, RelocatorException, ResolutionException {
    @Contract(pure = true)
    public SlimJarException(@NotNull final String message) {
        super(message);
    }

    @Contract(pure = true)
    public SlimJarException(
        @NotNull final String message,
        @NotNull final Throwable cause
    ) {
        super(message);
        if (!(cause instanceof SlimJarException)) return;

        addSuppressed(cause);
        initCause(cause);
    }

    /**
     * Since we know that some exceptions are caused by interrupts,
     * we can use this method to quickly reinstate the interrupt.
     */
    public void checkInterrupted() {
        if (!(getCause() instanceof InterruptedException)) return;
        Thread.currentThread().interrupt();
    }
}
