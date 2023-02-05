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

package io.github.slimjar.downloader;

import io.github.slimjar.downloader.output.OutputWriterFactory;
import io.github.slimjar.downloader.verify.DependencyVerifier;
import io.github.slimjar.exceptions.DownloaderException;
import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.DependencyResolver;
import io.github.slimjar.exceptions.UnresolvedDependencyException;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.util.Connections;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public final class URLDependencyDownloader implements DependencyDownloader {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    private static final byte[] BOM_BYTES = "bom-file".getBytes();

    @NotNull private final OutputWriterFactory outputWriterProducer;
    @NotNull private final DependencyResolver dependencyResolver;
    @NotNull private final DependencyVerifier verifier;

    @Contract(pure = true)
    public URLDependencyDownloader(
        @NotNull final OutputWriterFactory outputWriterProducer,
        @NotNull final DependencyResolver dependencyResolver,
        @NotNull final DependencyVerifier verifier
    ) {
        this.outputWriterProducer = outputWriterProducer;
        this.dependencyResolver = dependencyResolver;
        this.verifier = verifier;
    }

    @Override
    public @NotNull Optional<File> download(@NotNull final Dependency dependency) throws DownloaderException {
        final var expectedOutputFile = outputWriterProducer.getStrategy().selectFileFor(dependency);

        if (existingBOM(expectedOutputFile.toPath()) && verifier.verify(expectedOutputFile, dependency)) {
            LOGGER.debug("Skipping download of %s because it is already downloaded.", dependency.artifactId());
            return Optional.of(expectedOutputFile);
        }

        final var result = dependencyResolver.resolve(dependency).orElseThrow(() -> new UnresolvedDependencyException(dependency));
        if (result.aggregator()) {
            writeBOM(expectedOutputFile.toPath());
            return Optional.empty();
        }
        cleanupExisting(expectedOutputFile, dependency);

        LOGGER.info("Downloading %s...", dependency.artifactId());

        final var url = result.dependencyURL();
        LOGGER.debug("Connecting to %s", url);

        final URLConnection connection;
        final InputStream inputStream;
        try {
            connection = Connections.createDownloadConnection(url);
            inputStream = connection.getInputStream();
        } catch (final IOException err) {
            throw new DownloaderException("Failed to connect to " + url, err);
        }
        LOGGER.debug("Connection successful! Downloading %s", dependency.artifactId() + "...");

        final var outputWriter = outputWriterProducer.create(dependency);
        LOGGER.debug("%s.Size = %s", dependency.artifactId(), connection.getContentLength());

        final var downloadResult = outputWriter.writeFrom(inputStream, connection.getContentLength());
        Connections.tryDisconnect(connection);
        verifier.verify(downloadResult, dependency); // TODO: Should we panic here?
        LOGGER.debug("Artifact %s downloaded successfully!", dependency.artifactId());

        LOGGER.info("Downloaded %s successfully!", dependency.artifactId());
        return Optional.of(downloadResult);
    }

    private boolean existingBOM(@NotNull final Path expectedOutputPath) throws DownloaderException {
        try {
            if (expectedOutputPath.toFile().exists() &&
                expectedOutputPath.toFile().length() == BOM_BYTES.length &&
                Arrays.equals(Files.readAllBytes(expectedOutputPath), BOM_BYTES)
            ) return true;
        } catch (final IOException err) {
            throw new DownloaderException("Failed to read existing BOM file", err);
        }

        return false;
    }

    private void writeBOM(@NotNull final Path expectedOutputPath) throws DownloaderException {
        try {
            Files.createDirectories(expectedOutputPath.getParent());
            Files.createFile(expectedOutputPath);
            Files.write(expectedOutputPath, BOM_BYTES);
        } catch (final IOException err) {
            throw new DownloaderException("Failed to write to BOM file.", err);
        }
    }

    private void cleanupExisting(
        @NotNull final File expectedOutputFile,
        @NotNull final Dependency dependency
    ) throws DownloaderException {
        try {
            if (expectedOutputFile.exists()) Files.delete(expectedOutputFile.toPath());

            final var checksumFile = verifier.getChecksumFile(dependency)
                .filter(File::exists)
                .map(File::toPath);
            if (checksumFile.isEmpty()) return;
            Files.delete(checksumFile.get());
        } catch (final IOException err) {
            throw new DownloaderException("Failed to cleanup existing files.", err);
        }
    }
}
