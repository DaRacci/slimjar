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

package io.github.slimjar.app.builder;

import io.github.slimjar.app.module.ModuleExtractor;
import io.github.slimjar.app.module.TemporaryModuleExtractor;
import io.github.slimjar.util.Modules;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record IsolationConfiguration(
    @NotNull String applicationClass,
    @NotNull Collection<String> modules,
    @NotNull ClassLoader parentClassloader,
    @NotNull ModuleExtractor moduleExtractor
) {

    @Contract(pure = true)
    public IsolationConfiguration(
        @NotNull String applicationClass,
        @NotNull Collection<String> modules,
        @NotNull ClassLoader parentClassloader,
        @NotNull ModuleExtractor moduleExtractor
    ) {
        this.applicationClass = applicationClass;
        this.modules = Collections.unmodifiableCollection(modules);
        this.parentClassloader = parentClassloader;
        this.moduleExtractor = moduleExtractor;
    }

    @Contract(pure = true)
    public static @NotNull Builder builder(@NotNull final String applicationClass) {
        return new Builder().applicationClass(applicationClass);
    }

    public static final class Builder {
        private String applicationClass;
        @NotNull private Set<String> modules = Collections.emptySet();
        private ClassLoader parentClassloader;
        private ModuleExtractor moduleExtractor;

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder applicationClass(final String applicationClass) {
            this.applicationClass = applicationClass;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder modules(@NotNull final String... modules) {
            this.modules = Stream.concat(this.modules.stream(), Arrays.stream(modules)).collect(Collectors.toUnmodifiableSet());
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder parentClassLoader(@NotNull final ClassLoader classLoader) {
            this.parentClassloader = classLoader;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder moduleExtractor(@NotNull final ModuleExtractor moduleExtractor) {
            this.moduleExtractor = moduleExtractor;
            return this;
        }

        @Contract(pure = true)
        @NotNull String getApplicationClass() {
            if (applicationClass == null) {
                throw new AssertionError("Application Class not Provided!");
            }

            return applicationClass;
        }

        @Contract(mutates = "this")
        @NotNull Collection<String> getModules() {
            if (modules.isEmpty()) {
                this.modules = Collections.unmodifiableSet(Modules.findLocalModules());
            }

            return modules;
        }

        @Contract(mutates = "this")
        @NotNull ClassLoader getParentClassloader() {
            if (parentClassloader == null) {
                this.parentClassloader = ClassLoader.getSystemClassLoader().getParent();
            }

            return parentClassloader;
        }

        @Contract(mutates = "this")
        @NotNull ModuleExtractor getModuleExtractor() {
            if (moduleExtractor == null) {
                this.moduleExtractor = new TemporaryModuleExtractor();
            }

            return moduleExtractor;
        }

        @Contract(value = " -> new", mutates = "this")
        public @NotNull IsolationConfiguration build() {
            return new IsolationConfiguration(
                getApplicationClass(),
                getModules(),
                getParentClassloader(),
                getModuleExtractor()
            );
        }
    }
}
