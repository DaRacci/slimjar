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

package io.github.slimjar.relocation.helper;

import io.github.slimjar.downloader.strategy.FilePathStrategy;
import io.github.slimjar.downloader.verify.FileChecksumCalculator;
import io.github.slimjar.exceptions.RelocatorException;
import io.github.slimjar.relocation.Relocator;
import io.github.slimjar.relocation.meta.MetaMediatorFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public final class VerifyingRelocationHelperFactory implements RelocationHelperFactory {
    @NotNull private static final URI JAR_URL;

    static {
        try {
            JAR_URL = VerifyingRelocationHelperFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (final URISyntaxException err) {
            throw new RelocatorException("Failed to convert URL to URI.", err);
        }
    }

    @NotNull private final FilePathStrategy relocationFilePathStrategy;
    @NotNull private final MetaMediatorFactory mediatorFactory;
    @NotNull private final String selfHash;

    public VerifyingRelocationHelperFactory(
        @NotNull final String selfHash,
        @NotNull final FilePathStrategy relocationFilePathStrategy,
        @NotNull final MetaMediatorFactory mediatorFactory
    ) {
        this.relocationFilePathStrategy = relocationFilePathStrategy;
        this.mediatorFactory = mediatorFactory;
        this.selfHash = selfHash;
    }

    public VerifyingRelocationHelperFactory(
        @NotNull final FileChecksumCalculator calculator,
        @NotNull final FilePathStrategy relocationFilePathStrategy,
        @NotNull final MetaMediatorFactory mediatorFactory
    ) {
        this(calculator.calculate(new File(JAR_URL)), relocationFilePathStrategy, mediatorFactory);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull RelocationHelper create(@NotNull final Relocator relocator) {
        return new VerifyingRelocationHelper(selfHash, relocationFilePathStrategy, relocator, mediatorFactory);
    }
}
