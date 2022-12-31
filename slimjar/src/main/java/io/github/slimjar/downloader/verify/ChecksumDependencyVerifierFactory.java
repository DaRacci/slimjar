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

package io.github.slimjar.downloader.verify;

import io.github.slimjar.downloader.output.OutputWriterFactory;
import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.DependencyResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ChecksumDependencyVerifierFactory implements DependencyVerifierFactory {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    @NotNull private final OutputWriterFactory outputWriterFactory;
    @NotNull private final DependencyVerifierFactory fallbackVerifierFactory;
    @NotNull private final ChecksumCalculator checksumCalculator;

    @Contract(pure = true)
    public ChecksumDependencyVerifierFactory(
        @NotNull final OutputWriterFactory outputWriterFactory,
        @NotNull final DependencyVerifierFactory fallbackVerifierFactory,
        @NotNull final ChecksumCalculator checksumCalculator
    ) {
        this.outputWriterFactory = outputWriterFactory;
        this.fallbackVerifierFactory = fallbackVerifierFactory;
        this.checksumCalculator = checksumCalculator;
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public @NotNull DependencyVerifier create(final @NotNull DependencyResolver resolver) {
        LOGGER.debug("Creating verifier...");
        return new ChecksumDependencyVerifier(
            resolver,
            outputWriterFactory,
            fallbackVerifierFactory.create(resolver),
            checksumCalculator
        );
    }
}
