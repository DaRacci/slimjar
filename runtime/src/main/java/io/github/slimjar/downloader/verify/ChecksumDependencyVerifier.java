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
import io.github.slimjar.exceptions.VerificationException;
import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.DependencyResolver;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.util.Connections;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public final class ChecksumDependencyVerifier implements DependencyVerifier {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    @NotNull private final DependencyResolver resolver;
    @NotNull private final OutputWriterFactory outputWriterFactory;
    @NotNull private final DependencyVerifier fallbackVerifier;
    @NotNull private final ChecksumCalculator checksumCalculator;

    @Contract(pure = true)
    public ChecksumDependencyVerifier(
        @NotNull final DependencyResolver resolver,
        @NotNull final OutputWriterFactory outputWriterFactory,
        @NotNull final DependencyVerifier fallbackVerifier,
        @NotNull final ChecksumCalculator checksumCalculator
    ) {
        this.resolver = resolver;
        this.outputWriterFactory = outputWriterFactory;
        this.fallbackVerifier = fallbackVerifier;
        this.checksumCalculator = checksumCalculator;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean verify(
        @NotNull final File file,
        @NotNull final Dependency dependency
    ) throws VerificationException {
        if (!file.exists()) return false;

        LOGGER.info("Verifying checksum for %s", dependency.artifactId());

        final var checksumFile = outputWriterFactory.getStrategy().selectFileFor(dependency);
        checksumFile.getParentFile().mkdirs();

        if (!checksumFile.exists() && !prepareChecksumFile(checksumFile, dependency)) {
            LOGGER.debug("Unable to resolve checksum for %s, falling back to fallbackVerifier!", dependency.artifactId());
            return fallbackVerifier.verify(file, dependency);
        }

        if (checksumFile.length() == 0L) {
            LOGGER.debug("Required checksum not found for %s, using fallbackVerifier!", dependency.artifactId());
            return fallbackVerifier.verify(file, dependency);
        }

        final var actualChecksum = checksumCalculator.calculate(file);
        final String expectedChecksum;
        try {
            expectedChecksum = new String(Files.readAllBytes(checksumFile.toPath())).trim();
        } catch (IOException e) {
            throw new VerificationException("Unable to read bytes from checksum file (%s)".formatted(checksumFile), e);
        }

        LOGGER.debug("%s -> Actual checksum: %s;", dependency.artifactId(), actualChecksum);
        LOGGER.debug("%s -> Expected checksum: %s;", dependency.artifactId(), expectedChecksum);

        if (!actualChecksum.equals(expectedChecksum)) {
            LOGGER.error("Checksum mismatch for %s, expected %s, got %s", dependency.artifactId(), expectedChecksum, actualChecksum);
            return false;
        }

        LOGGER.debug("Checksum matched for %s.", dependency.artifactId());
        return true;
    }

    @Override
    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S899"})
    public @NotNull Optional<File> getChecksumFile(@NotNull final Dependency dependency) {
        final File checksumFile = outputWriterFactory.getStrategy().selectFileFor(dependency);
        checksumFile.getParentFile().mkdirs();
        return Optional.of(checksumFile);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S899"})
    private boolean prepareChecksumFile(
        @NotNull final File checksumFile,
        @NotNull final Dependency dependency
    ) throws VerificationException {
        final var result = resolver.resolve(dependency);
        if (result.isEmpty()) return false;

        final var checkSumUrl = result.get().checksumURL();

        LOGGER.info("Resolved checksum URL for %s as %s", dependency.artifactId(), checkSumUrl);

        try {
            if (checkSumUrl == null) {
                checksumFile.createNewFile();
                return false; // TODO: Was true before, i think this should be false?
            }

            final var connection = Connections.createDownloadConnection(checkSumUrl);
            final var inputStream = connection.getInputStream();
            final var outputWriter = outputWriterFactory.create(dependency);

            outputWriter.writeFrom(inputStream, connection.getContentLength());
            Connections.tryDisconnect(connection);
        } catch (final IOException err) {
            throw new VerificationException("Unable to get checksum for %s".formatted(dependency.toString()), err);
        }

        LOGGER.info("Downloaded checksum for %s", dependency.artifactId());

        return true;
    }
}
