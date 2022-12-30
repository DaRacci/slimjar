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

package io.github.slimjar.resolver;

import io.github.slimjar.logging.LogDispatcher;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.enquirer.RepositoryEnquirer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

public final class DummyRepositoryEnquirer implements RepositoryEnquirer {
    private static final @NotNull ProcessLogger LOGGER = LogDispatcher.getMediatingLogger();

    @Override
    public @Nullable ResolutionResult enquire(final @NotNull Dependency dependency) {
        final String groupPath = dependency.groupId().replace('.', '/');
        try {
            return new ResolutionResult(new Repository(new URL("https://repo.tld")), new URL(String.format("https://repo.tld/%s/%s/%s/%2$s-%3$s.jar", groupPath, dependency.artifactId(), dependency.version())), null, false, false);
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to create dummy URL", e);
            return null;
        }
    }
}
