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
import io.github.slimjar.relocation.Relocator;
import io.github.slimjar.relocation.meta.MetaMediator;
import io.github.slimjar.relocation.meta.MetaMediatorFactory;
import io.github.slimjar.resolver.data.Dependency;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

public final class VerifyingRelocationHelper implements RelocationHelper {
    @NotNull private final FilePathStrategy outputFilePathStrategy;
    @NotNull private final Relocator relocator;
    @NotNull private final String selfHash;
    @NotNull private final MetaMediatorFactory mediatorFactory;

    public VerifyingRelocationHelper(
        @NotNull final String selfHash,
        @NotNull final FilePathStrategy outputFilePathStrategy,
        @NotNull final Relocator relocator,
        @NotNull final MetaMediatorFactory mediatorFactory
    ) {
        this.mediatorFactory = mediatorFactory;
        this.outputFilePathStrategy = outputFilePathStrategy;
        this.relocator = relocator;
        this.selfHash = selfHash;
    }

    @Override
    public @NotNull File relocate(
        @NotNull final Dependency dependency,
        @NotNull final File file
    ) {
        final var relocatedFile = outputFilePathStrategy.selectFileFor(dependency);
        final var metaMediator = mediatorFactory.create(relocatedFile.toPath());

        if (relocatedFile.exists()) {
            try {
                final var ownerHash = metaMediator.readAttribute("slimjar.owner");
                if (ownerHash != null && selfHash.trim().equals(ownerHash.trim())) return relocatedFile;
            } catch (final IOException err) {
                // Possible incomplete relocation present.
                // todo: Log incident
                //noinspection ResultOfMethodCallIgnored
                relocatedFile.delete();
            }
        }
        relocator.relocate(file, relocatedFile);
        metaMediator.writeAttribute("slimjar.owner", selfHash);
        return relocatedFile;
    }
}
