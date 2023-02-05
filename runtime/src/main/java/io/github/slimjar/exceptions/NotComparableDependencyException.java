package io.github.slimjar.exceptions;

import io.github.slimjar.resolver.data.Dependency;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class NotComparableDependencyException extends SlimJarException {

    @Contract(pure = true)
    public NotComparableDependencyException(
        @NotNull final Dependency a,
        @NotNull final Dependency b
    ) {
        super("Dependency " + a + " does not match " + b + "and cannot be compared.");
    }
}
