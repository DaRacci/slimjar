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

package io.github.slimjar.downloader.output;

import io.github.slimjar.exceptions.OutputWriterException;
import io.github.slimjar.logging.LocationAwareProcessLogger;
import io.github.slimjar.logging.ProcessLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class ChanneledFileOutputWriter implements OutputWriter {
    @NotNull private static final ProcessLogger LOGGER = LocationAwareProcessLogger.generic();
    @NotNull private final File outputFile;

    @Contract(pure = true)
    public ChanneledFileOutputWriter(@NotNull final File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    @Contract(mutates = "param1")
    public @NotNull File writeFrom(
        @NotNull final InputStream inputStream,
        final long length
    ) throws OutputWriterException {
        LOGGER.debug("Attempting to write from inputStream...");

        try {
            if (outputFile.exists()) return outputFile;

            LOGGER.debug("Writing %s bytes...", length == -1 ? "unknown" : length);
            Files.copy(inputStream, outputFile.toPath());
        } catch (final Exception err) {
            throw new OutputWriterException("Unable to copy from input stream to %s.".formatted(outputFile), err);
        } finally {
            try {
                inputStream.close();
            } catch (final IOException err) {
                LOGGER.error("Unable to close stream.", err);
            }
        }

        return outputFile;
    }
}
