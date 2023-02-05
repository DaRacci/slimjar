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

package io.github.slimjar.injector.helper;

import io.github.slimjar.downloader.DependencyDownloaderFactory;
import io.github.slimjar.downloader.output.DependencyOutputWriterFactory;
import io.github.slimjar.downloader.strategy.FilePathStrategy;
import io.github.slimjar.downloader.verify.DependencyVerifierFactory;
import io.github.slimjar.exceptions.InjectorException;
import io.github.slimjar.injector.DependencyInjectorFactory;
import io.github.slimjar.relocation.RelocatorFactory;
import io.github.slimjar.relocation.helper.RelocationHelperFactory;
import io.github.slimjar.resolver.DependencyResolverFactory;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.DependencyData;
import io.github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;
import io.github.slimjar.resolver.mirrors.MirrorSelector;
import io.github.slimjar.resolver.reader.dependency.DependencyDataProviderFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public final class InjectionHelperFactory {
    @NotNull private final Path downloadDirectoryPath;
    @NotNull private final RelocatorFactory relocatorFactory;
    @NotNull private final RelocationHelperFactory relocationHelperFactory;
    @NotNull private final DependencyResolverFactory resolverFactory;
    @NotNull private final RepositoryEnquirerFactory enquirerFactory;
    @NotNull private final DependencyDownloaderFactory downloaderFactory;
    @NotNull private final DependencyVerifierFactory verifier;
    @NotNull private final MirrorSelector mirrorSelector;

    public InjectionHelperFactory(
        @NotNull final Path downloadDirectoryPath,
        @NotNull final RelocatorFactory relocatorFactory,
        @NotNull final DependencyDataProviderFactory dataProviderFactory, // TODO: Why is this here if it's not used?
        @NotNull final RelocationHelperFactory relocationHelperFactory,
        @NotNull final DependencyInjectorFactory injectorFactory,
        @NotNull final DependencyResolverFactory resolverFactory,
        @NotNull final RepositoryEnquirerFactory enquirerFactory,
        @NotNull final DependencyDownloaderFactory downloaderFactory,
        @NotNull final DependencyVerifierFactory verifier,
        @NotNull final MirrorSelector mirrorSelector
    ) {
        this.downloadDirectoryPath = downloadDirectoryPath;
        this.relocatorFactory = relocatorFactory;
        this.relocationHelperFactory = relocationHelperFactory;
        this.resolverFactory = resolverFactory;
        this.enquirerFactory = enquirerFactory;
        this.downloaderFactory = downloaderFactory;
        this.verifier = verifier;
        this.mirrorSelector = mirrorSelector;
    }

    @Contract("_, _ -> new")
    public @NotNull InjectionHelper create(
        @NotNull final DependencyData data,
        @NotNull final Map<String, ResolutionResult> preResolvedResults
    ) throws InjectorException {
        final var repositories = mirrorSelector.select(data.repositories(), data.mirrors());
        final var relocator = relocatorFactory.create(data.relocations());
        final var relocationHelper = relocationHelperFactory.create(relocator);
        final var filePathStrategy = FilePathStrategy.createDefault(downloadDirectoryPath.toFile());
        final var outputWriterFactory = new DependencyOutputWriterFactory(filePathStrategy);
        final var resolver = resolverFactory.create(repositories, preResolvedResults, enquirerFactory);
        final var downloader = downloaderFactory.create(outputWriterFactory, resolver, verifier.create(resolver));

        return new InjectionHelper(downloader, relocationHelper);
    }
}
