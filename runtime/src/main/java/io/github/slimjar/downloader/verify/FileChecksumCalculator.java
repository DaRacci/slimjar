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

import io.github.slimjar.exceptions.VerificationException;
import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// TODO: Possibly implement a thread group to calculate checksums in batches.
public final class FileChecksumCalculator implements ChecksumCalculator {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    @NotNull private static final String DIRECTORY_HASH = "DIRECTORY";

    @NotNull private final ThreadLocal<MessageDigest> digestThreadLocal;

    @Contract(pure = true)
    public FileChecksumCalculator(@NotNull final String algorithm) throws VerificationException {
        final MessageDigest templateDigest;
        try {
            templateDigest = MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            // This should never happen, as the algorithm isn't proved by the user.
            throw new VerificationException("Failed to initialize checksum calculator", e);
        }

        digestThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return (MessageDigest) templateDigest.clone();
            } catch (final CloneNotSupportedException err) {
                // This should never happen.
                throw new VerificationException("Failed to clone digest template for thread local use.", err);
            }
        });
    }

    @Override
    @Contract(pure = true)
    public @NotNull String calculate(@NotNull final File file) throws VerificationException {
        LOGGER.debug("Calculating hash for %s", file.getPath());

        // This helps run IDE environment as a special case
        if (file.isDirectory()) {
            return DIRECTORY_HASH;
        }

        // TODO: Ensure this is safe like this.
        final var digest = digestThreadLocal.get();

        try (final var stream = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = stream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        } catch (final IOException err) {
            throw new VerificationException("Encountered error while reading checksum file %s.".formatted(file.getPath()), err);
        }

        final var bytes = digest.digest();
        final var sb = new StringBuilder();
        for (final var b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        sb.trimToSize();
        final var result = sb.toString();

        LOGGER.debug("Hash for %s -> %s", file.getPath(), result);

        digestThreadLocal.remove();
        return result;
    }
}
