//
// MIT License
//
// Copyright (c) 2021 Vaishnav Anil
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.slimjar.logging;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class MediatingProcessLogger implements ProcessLogger {
    @NotNull private final Collection<@NotNull ProcessLogger> loggers;

    @Contract(pure = true)
    public MediatingProcessLogger(@NotNull final Collection<@NotNull ProcessLogger> loggers) {
        this.loggers = loggers;
    }

    @Override
    public void info(
        @NotNull final String message,
        @Nullable final Object... args
    ) { loggers.forEach(logger -> logger.info(message, args)); }

    @Override
    public void debug(
        final @NotNull String message,
        final @Nullable Object @Nullable ... args
    ) { loggers.forEach(logger -> logger.debug(message, args)); }

    @Override
    public void error(
        @NotNull String message,
        @Nullable Object... args
    ) { loggers.forEach(logger -> logger.error(message, args)); }

    @Contract(pure = true)
    public void addLogger(@NotNull final ProcessLogger logger) {
        this.loggers.add(logger);
    }

    @Contract(pure = true)
    public void removeLogger(@NotNull final ProcessLogger logger) {
        this.loggers.remove(logger);
    }

    @Contract(pure = true)
    public void clearLoggers() {
        this.loggers.clear();
    }
}
