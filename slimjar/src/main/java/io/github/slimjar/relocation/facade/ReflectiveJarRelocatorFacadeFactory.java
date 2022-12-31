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

package io.github.slimjar.relocation.facade;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.exceptions.RelocatorException;
import io.github.slimjar.injector.loader.InjectableClassLoader;
import io.github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import io.github.slimjar.relocation.PassthroughRelocator;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.DependencyData;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.util.Packages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ReflectiveJarRelocatorFacadeFactory implements JarRelocatorFacadeFactory {
    @NotNull private static final String JAR_RELOCATOR_PACKAGE = "me#lucko#jarrelocator#JarRelocator";
    @NotNull private static final String RELOCATION_PACKAGE = "me#lucko#jarrelocator#Relocation";

    @NotNull private final Constructor<?> jarRelocatorConstructor;
    @NotNull private final Constructor<?> relocationConstructor;
    @NotNull private final Method jarRelocatorRunMethod;

    private ReflectiveJarRelocatorFacadeFactory(
        @NotNull final Constructor<?> jarRelocatorConstructor,
        @NotNull final Constructor<?> relocationConstructor,
        @NotNull final Method jarRelocatorRunMethod
    ) {
        this.jarRelocatorConstructor = jarRelocatorConstructor;
        this.relocationConstructor = relocationConstructor;
        this.jarRelocatorRunMethod = jarRelocatorRunMethod;
    }

    @Override
    public @NotNull JarRelocatorFacade createFacade(
        final @NotNull File input,
        final @NotNull File output,
        final @NotNull Collection<RelocationRule> relocationRules
    ) {
        final Object relocator;
        try {
            final var relocations = relocationRules.stream()
                .map(rule -> createRelocation(relocationConstructor, rule))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            relocator = createRelocator(jarRelocatorConstructor, input, output, relocations);
        } catch (final IllegalAccessException | InstantiationException | InvocationTargetException err) {
            throw new RelocatorException("Failed to create JarRelocator", err);
        }

        return new ReflectiveJarRelocatorFacade(relocator, jarRelocatorRunMethod);
    }

    private static @Nullable Object createRelocation(
        @NotNull final Constructor<?> relocationConstructor,
        @NotNull final RelocationRule rule
    ) {
        try {
            return relocationConstructor.newInstance(
                rule.getOriginalPackagePattern(),
                rule.getRelocatedPackagePattern(),
                rule.getExclusions(),
                rule.getInclusions()
            );
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    private static Object createRelocator(
        final Constructor<?> jarRelocatorConstructor,
        final File input,
        final File output,
        final Collection<Object> rules
    ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return jarRelocatorConstructor.newInstance(input, output, rules);
    }

    private static DependencyData getJarRelocatorDependency(final Collection<Repository> repositories) {
        final Dependency asm = new Dependency(
            Packages.fix("org#ow2#asm"),
            "asm",
            "9.1",
            null,
            Collections.emptyList()
        );
        final Dependency asmCommons = new Dependency(
            Packages.fix("org#ow2#asm"),
            "asm-commons",
            "9.1",
            null,
            Collections.emptyList()
        );
        final Dependency jarRelocator = new Dependency(
            Packages.fix("me#lucko"),
            "jar-relocator",
            "1.5",
            null,
            Arrays.asList(asm, asmCommons)
        );
        return new DependencyData(
            Collections.emptySet(),
            repositories,
            Collections.singleton(jarRelocator),
            Collections.emptyList()
        );
    }

    public static JarRelocatorFacadeFactory create(
        final Path downloadPath,
        final Collection<Repository> repositories
    ) throws RelocatorException {
        final InjectableClassLoader classLoader = new IsolatedInjectableClassLoader();
        return create(downloadPath, repositories, classLoader);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull JarRelocatorFacadeFactory create(
        @NotNull final Path downloadPath,
        @NotNull final Collection<Repository> repositories,
        @NotNull final InjectableClassLoader classLoader
    ) throws RelocatorException {
        ApplicationBuilder.injecting("SlimJar", classLoader)
            .downloadDirectoryPath(downloadPath)
            .preResolutionDataProviderFactory(a -> Collections::emptyMap)
            .dataProviderFactory(url -> () -> ReflectiveJarRelocatorFacadeFactory.getJarRelocatorDependency(repositories))
            .relocatorFactory(rules -> new PassthroughRelocator())
            .relocationHelperFactory(relocator -> (dependency, file) -> file)
            .build();

        final Constructor<?> jarRelocatorConstructor;
        final Constructor<?> relocationConstructor;
        final Method runMethod;
        try {
            final var jarRelocatorClass = Class.forName(Packages.fix(JAR_RELOCATOR_PACKAGE), true, classLoader);
            final var relocationClass = Class.forName(Packages.fix(RELOCATION_PACKAGE), true, classLoader);

            jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);
            relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);
            runMethod = jarRelocatorClass.getMethod("run");
        } catch (final NoSuchMethodException err) {
            throw new RelocatorException("Failed to find JarRelocator constructor", err);
        } catch (final ClassNotFoundException err) {
            // Shouldn't be possible.
            throw new RelocatorException("Failed to find JarRelocator class", err);
        }

        return new ReflectiveJarRelocatorFacadeFactory(jarRelocatorConstructor, relocationConstructor, runMethod);
    }
}
