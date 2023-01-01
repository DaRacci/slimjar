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

package io.github.slimjar.injector.agent;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.app.module.ModuleExtractor;
import io.github.slimjar.app.module.TemporaryModuleExtractor;
import io.github.slimjar.exceptions.InjectorException;
import io.github.slimjar.injector.loader.InstrumentationInjectable;
import io.github.slimjar.injector.loader.IsolatedInjectableClassLoader;
import io.github.slimjar.injector.loader.manifest.JarManifestGenerator;
import io.github.slimjar.relocation.JarFileRelocator;
import io.github.slimjar.relocation.PassthroughRelocator;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import io.github.slimjar.resolver.data.Dependency;
import io.github.slimjar.resolver.data.DependencyData;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.util.Packages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public final class ByteBuddyInstrumentationFactory implements InstrumentationFactory {
    @NotNull public static final String AGENT_JAR = "loader-agent.isolated-jar";
    @NotNull private static final String AGENT_PACKAGE = "io#github#slimjar#injector#agent";
    @NotNull private static final String AGENT_CLASS = "ClassLoaderAgent";
    @NotNull private static final String BYTE_BUDDY_AGENT_CLASS = "net#bytebuddy#agent#ByteBuddyAgent";

    @NotNull private final URL agentJarUrl;
    @NotNull private final ModuleExtractor extractor;
    @NotNull private final JarRelocatorFacadeFactory relocatorFacadeFactory;

    public ByteBuddyInstrumentationFactory(@NotNull final JarRelocatorFacadeFactory relocatorFacadeFactory) throws NullPointerException {
        this(
            Objects.requireNonNull(InstrumentationInjectable.class.getClassLoader().getResource(AGENT_JAR)),
            new TemporaryModuleExtractor(),
            relocatorFacadeFactory
        );
    }

    @Contract(pure = true)
    public ByteBuddyInstrumentationFactory(
        @NotNull final URL agentJarUrl,
        @NotNull final ModuleExtractor extractor,
        @NotNull final JarRelocatorFacadeFactory relocatorFacadeFactory
    ) {
        this.agentJarUrl = agentJarUrl;
        this.extractor = extractor;
        this.relocatorFacadeFactory = relocatorFacadeFactory;
    }

    @Override
    public @NotNull Instrumentation create() throws InjectorException {
        final var extractedURL = extractor.extractModule(agentJarUrl, "loader-agent");
        final var pattern = generatePattern();
        final var relocatedAgentClass = String.format("%s.%s", pattern, AGENT_CLASS);
        final var relocationRule = new RelocationRule(Packages.fix(AGENT_PACKAGE), pattern, Collections.emptySet(), Collections.emptySet());
        final var relocator = new JarFileRelocator(Collections.singleton(relocationRule), relocatorFacadeFactory);
        final File inputFile;
        final File relocatedFile;
        try {
            inputFile = new File(extractedURL.toURI());
            relocatedFile = File.createTempFile("slimjar-agent", ".jar");
        } catch (final URISyntaxException | IOException err) {
            throw new InjectorException("Failed to create temporary file for relocated agent", err);
        }

        final var classLoader = new IsolatedInjectableClassLoader();
        relocator.relocate(inputFile, relocatedFile);

        JarManifestGenerator.with(relocatedFile.toURI())
            .attribute("Manifest-Version", "1.0")
            .attribute("Agent-Class", relocatedAgentClass)
            .generate();

        ApplicationBuilder.injecting("SlimJar-Agent", classLoader)
            .dataProviderFactory(dataUrl -> ByteBuddyInstrumentationFactory::getDependency)
            .relocatorFactory(rules -> new PassthroughRelocator())
            .relocationHelperFactory(rel -> (dependency, file) -> file)
            .build();

        try {
            final var byteBuddyAgentClass = Class.forName(Packages.fix(BYTE_BUDDY_AGENT_CLASS), true, classLoader);
            final var attachMethod = byteBuddyAgentClass.getMethod("attach", File.class, String.class, String.class);

            final var processHandle = Class.forName("java.lang.ProcessHandle");
            final var currentMethod = processHandle.getMethod("current");
            final var pidMethod = processHandle.getMethod("pid");
            final var currentProcess = currentMethod.invoke(processHandle);
            final var processId = (Long) pidMethod.invoke(currentProcess);

            attachMethod.invoke(null, relocatedFile, String.valueOf(processId), "");

            final var agentClass = Class.forName(relocatedAgentClass, true, ClassLoader.getSystemClassLoader());
            final var instrMethod = agentClass.getMethod("getInstrumentation");
            return (Instrumentation) instrMethod.invoke(null);
        } catch (final ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException err) {
            throw new InjectorException("Encountered exception while creating ByteBuddy instructions.", err);
        }
    }

    private static @NotNull DependencyData getDependency() {
        final var byteBuddy = new Dependency(
            "net.bytebuddy",
            "byte-buddy-agent",
            "1.11.0",
            null,
            Collections.emptyList()
        );

        return new DependencyData(
            Collections.emptySet(),
            Collections.singleton(Repository.central()),
            Collections.singleton(byteBuddy),
            Collections.emptyList()
        );
    }

    @Contract(pure = true)
    private static @NotNull String generatePattern() {
        return "slimjar.%s".formatted(UUID.randomUUID());
    }
}
