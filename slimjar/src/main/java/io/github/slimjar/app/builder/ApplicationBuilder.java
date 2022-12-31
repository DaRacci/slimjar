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

import io.github.slimjar.app.Application;
import io.github.slimjar.downloader.DependencyDownloaderFactory;
import io.github.slimjar.downloader.URLDependencyDownloaderFactory;
import io.github.slimjar.downloader.output.DependencyOutputWriterFactory;
import io.github.slimjar.downloader.strategy.ChecksumFilePathStrategy;
import io.github.slimjar.downloader.strategy.FilePathStrategy;
import io.github.slimjar.downloader.verify.ChecksumDependencyVerifierFactory;
import io.github.slimjar.downloader.verify.DependencyVerifierFactory;
import io.github.slimjar.downloader.verify.FileChecksumCalculator;
import io.github.slimjar.downloader.verify.PassthroughDependencyVerifierFactory;
import io.github.slimjar.injector.DependencyInjector;
import io.github.slimjar.injector.DependencyInjectorFactory;
import io.github.slimjar.injector.SimpleDependencyInjectorFactory;
import io.github.slimjar.injector.helper.InjectionHelperFactory;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.logging.LogDispatcher;
import io.github.slimjar.logging.ProcessLogger;
import io.github.slimjar.relocation.JarFileRelocatorFactory;
import io.github.slimjar.relocation.RelocatorFactory;
import io.github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import io.github.slimjar.relocation.helper.RelocationHelperFactory;
import io.github.slimjar.relocation.helper.VerifyingRelocationHelperFactory;
import io.github.slimjar.relocation.meta.FlatFileMetaMediatorFactory;
import io.github.slimjar.resolver.CachingDependencyResolverFactory;
import io.github.slimjar.resolver.DependencyResolverFactory;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.enquirer.PingingRepositoryEnquirerFactory;
import io.github.slimjar.resolver.enquirer.RepositoryEnquirerFactory;
import io.github.slimjar.resolver.mirrors.MirrorSelector;
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import io.github.slimjar.resolver.pinger.HttpURLPinger;
import io.github.slimjar.resolver.reader.dependency.DependencyDataProvider;
import io.github.slimjar.resolver.reader.dependency.DependencyDataProviderFactory;
import io.github.slimjar.resolver.reader.dependency.ExternalDependencyDataProviderFactory;
import io.github.slimjar.resolver.reader.dependency.GsonDependencyDataProviderFactory;
import io.github.slimjar.resolver.reader.facade.ReflectiveGsonFacadeFactory;
import io.github.slimjar.resolver.reader.resolution.GsonPreResolutionDataProviderFactory;
import io.github.slimjar.resolver.reader.resolution.PreResolutionDataProvider;
import io.github.slimjar.resolver.reader.resolution.PreResolutionDataProviderFactory;
import io.github.slimjar.resolver.strategy.MavenChecksumPathResolutionStrategy;
import io.github.slimjar.resolver.strategy.MavenPathResolutionStrategy;
import io.github.slimjar.resolver.strategy.MavenPomPathResolutionStrategy;
import io.github.slimjar.resolver.strategy.MavenSnapshotPathResolutionStrategy;
import io.github.slimjar.resolver.strategy.MediatingPathResolutionStrategy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * Serves as a configuration for different components slimjar will use during injection.
 * Allows completely modifying and adding upon onto the default behaviour when needed.
 */
public abstract class ApplicationBuilder {
    @NotNull private static final Path DEFAULT_DOWNLOAD_DIRECTORY = Path.of(System.getProperty("user.home"), ".slimjar");

    @NotNull private final String applicationName;
    @Nullable private URL dependencyFileUrl;
    @Nullable private URL preResolutionFileUrl;
    @Nullable private Path downloadDirectoryPath;
    @Nullable private RelocatorFactory relocatorFactory;
    @Nullable private DependencyDataProviderFactory moduleDataProviderFactory;
    @Nullable private DependencyDataProviderFactory dataProviderFactory;
    @Nullable private PreResolutionDataProviderFactory preResolutionDataProviderFactory;
    @Nullable private RelocationHelperFactory relocationHelperFactory;
    @Nullable private DependencyInjectorFactory injectorFactory;
    @Nullable private DependencyResolverFactory resolverFactory;
    @Nullable private RepositoryEnquirerFactory enquirerFactory;
    @Nullable private DependencyDownloaderFactory downloaderFactory;
    @Nullable private DependencyVerifierFactory verifierFactory;
    @Nullable private MirrorSelector mirrorSelector;
    @Nullable private ProcessLogger logger;

    /**
     * Generate a application builder for an application with given name.
     * @param applicationName Name of your application/project. This exists to uniquely identify relocations.
     */
    @Contract(pure = true)
    protected ApplicationBuilder(@NotNull final String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Creates an ApplicationBuilder that allows jar-in-jar dependency loading.
     * @param name Name of your application/project. This exists to uniquely identify relocations.
     * @param config Basic configuration that isolated classloader requires.
     * @param args Arguments to pass to created Application class (specified in <code>config</code>).
     * @return ApplicationBuilder that allows jar-in-jar dependency loading.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull ApplicationBuilder isolated(
        @NotNull final String name,
        @NotNull final IsolationConfiguration config,
        @Nullable Object... args
    ) {
        return new IsolatedApplicationBuilder(name, config, args);
    }

    /**
     * Creates an ApplicationBuilder that allows loading into current classloader.
     * @param name Name of your application/project. This exists to uniquely identify relocations.
     * @return ApplicationBuilder that allows loading into current classloader.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ApplicationBuilder appending(@NotNull final String name) {
        return InjectingApplicationBuilder.createAppending(name);
    }

    /**
     * Creates an ApplicationBuilder that allows loading into any given {@link Injectable} instance.
     * For a simple isolated classloader, use {@link io.github.slimjar.injector.loader.IsolatedInjectableClassLoader}
     * You can create a {@link Injectable} version of any classloader using {@link io.github.slimjar.injector.loader.InjectableFactory#create(Path, Collection)}
     * Alternatively you can provide a custom implementation of {@link Injectable} to specify how the dependencies must be added to the classloader.
     * @param name Name of your application/project. This exists to uniquely identify relocations.
     * @return ApplicationBuilder that allows loading into any given {@link Injectable} instance.
     */
    @Contract(value = "_, _ -> new")
    public static @NotNull ApplicationBuilder injecting(
        @NotNull final String name,
        @NotNull final Injectable injectable
    ) {
        return new InjectingApplicationBuilder(name, injectable);
    }

    /**
     *  URL to the json configuration file that defines the dependencies/repositories/mirrors
     * @param dependencyFileUrl URL to the json configuration file (Default being slimjar.json inside jar root generated by the gradle plugin)
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder dependencyFileUrl(@NotNull final URL dependencyFileUrl) {
        this.dependencyFileUrl = dependencyFileUrl;
        return this;
    }

    /**
     *  URL to the json configuration file that defines the resolutions for given dependencies
     * @param preResolutionFileUrl URL to the json resolution configuration file (Default being slimjar-resolutions.json inside jar root generated by the gradle plugin)
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder preResolutionFileUrl(@NotNull final URL preResolutionFileUrl) {
        this.preResolutionFileUrl = preResolutionFileUrl;
        return this;
    }

    /**
     * Directory to which slimjar will attempt to download dependency files into
     * @param downloadDirectoryPath Download directory for dependencies.
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder downloadDirectoryPath(@NotNull final Path downloadDirectoryPath) {
        this.downloadDirectoryPath = downloadDirectoryPath;
        return this;
    }

    /**
     * Factory class that defines the construction of {@link io.github.slimjar.relocation.Relocator}
     * This deals with the actual relocation process.
     * The default implementation uses lucko/JarRelocator
     * @param relocatorFactory Factory class to create Relocator
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder relocatorFactory(@NotNull final RelocatorFactory relocatorFactory) {
        this.relocatorFactory = relocatorFactory;
        return this;
    }

    /**
     * Factory that produces DataProvider for modules in jar-in-jar classloading. Ignored if not using jar-in-jar/isolated(...)
     * Used to fetch the `slimjar.json` file of each submodule.
     * @param moduleDataProviderFactory Factory that produces DataProvider for modules in jar-in-jar
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder moduleDataProviderFactory(@NotNull final DependencyDataProviderFactory moduleDataProviderFactory) {
        this.moduleDataProviderFactory = moduleDataProviderFactory;
        return this;
    }

    /**
     * Factory that produces {@link DependencyDataProvider} to handle `dependencyFileUrl` (by default slimjar.json)
     * Used to fetch the `slimjar.json` file of current jar-file.
     * @param dataProviderFactory Factory that produces DataProvider to handle `dependencyFileUrl`
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder dataProviderFactory(@NotNull final DependencyDataProviderFactory dataProviderFactory) {
        this.dataProviderFactory = dataProviderFactory;
        return this;
    }

    /**
     * Factory that produces {@link PreResolutionDataProvider} to handle `preResolutionFileUrl` (by default slimjar-resolutions.json)
     * Used to fetch the `slimjar.json` file of current jar-file.
     * @param preResolutionDataProviderFactory Factory that produces DataProvider to handle `preResolutionFileUrl`
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder preResolutionDataProviderFactory(@NotNull final PreResolutionDataProviderFactory preResolutionDataProviderFactory) {
        this.preResolutionDataProviderFactory = preResolutionDataProviderFactory;
        return this;
    }

    /**
     * Factory that produces a {@link io.github.slimjar.relocation.helper.RelocationHelper} using <code>relocator</code>
     * This is an abstraction over {@link io.github.slimjar.relocation.Relocator}.
     * It decides the output file for relocation and includes extra steps such as jar verification.
     * @param relocationHelperFactory Factory that produces a RelocationHelper
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder relocationHelperFactory(@NotNull final RelocationHelperFactory relocationHelperFactory) {
        this.relocationHelperFactory = relocationHelperFactory;
        return this;
    }

    /**
     * Factory that produces a {@link DependencyInjector} using <code>relocator</code>
     * {@link DependencyInjector} decides how any given {@link io.github.slimjar.resolver.data.DependencyData} is injected into an {@link Injectable}
     * @param injectorFactory Factory that produces a DependencyInjector
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder injectorFactory(@NotNull final DependencyInjectorFactory injectorFactory) {
        this.injectorFactory = injectorFactory;
        return this;
    }

    /**
     * Factory that produces a {@link DependencyResolverFactory}
     * {@link io.github.slimjar.resolver.DependencyResolver} deals with resolving the URLs to a given dependency from a given collection of repositories
     * @param resolverFactory Factory that produces a DependencyResolverFactory
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder resolverFactory(@NotNull final DependencyResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
        return this;
    }

    /**
     * Factory that produces a {@link io.github.slimjar.resolver.enquirer.RepositoryEnquirer}
     * @param enquirerFactory Factory that produces a RepositoryEnquirer
     * @return <code>this</code>
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder enquirerFactory(@NotNull final RepositoryEnquirerFactory enquirerFactory) {
        this.enquirerFactory = enquirerFactory;
        return this;
    }

    /**
     *
     * @param downloaderFactory
     * @return
     */
    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder downloaderFactory(@NotNull final DependencyDownloaderFactory downloaderFactory) {
        this.downloaderFactory = downloaderFactory;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder verifierFactory(@NotNull final DependencyVerifierFactory verifierFactory) {
        this.verifierFactory = verifierFactory;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder mirrorSelector(@NotNull final MirrorSelector mirrorSelector) {
        this.mirrorSelector = mirrorSelector;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public final @NotNull ApplicationBuilder logger(@NotNull final ProcessLogger logger) {
        this.logger = logger;
        return this;
    }

    @Contract(pure = true)
    protected final @NotNull String getApplicationName() {
        return applicationName;
    }

    @Contract(mutates = "this")
    protected final @Nullable URL getDependencyFileUrl() {
        if (dependencyFileUrl == null) {
            this.dependencyFileUrl = getClass().getClassLoader().getResource("slimjar.json");
        }

        return dependencyFileUrl;
    }

    @Contract(mutates = "this")
    protected final @Nullable URL getPreResolutionFileUrl() {
        if (preResolutionFileUrl == null) {
            this.preResolutionFileUrl = getClass().getClassLoader().getResource("slimjar-resolutions.json");
        }

        return preResolutionFileUrl;
    }

    @Contract(mutates = "this")
    protected final @NotNull Path getDownloadDirectoryPath() {
        if (downloadDirectoryPath == null) {
            this.downloadDirectoryPath = DEFAULT_DOWNLOAD_DIRECTORY;
        }

        return downloadDirectoryPath;
    }

    @Contract(mutates = "this")
    protected final @NotNull RelocatorFactory getRelocatorFactory() {
        if (relocatorFactory == null) {
            final var jarRelocatorFacadeFactory = ReflectiveJarRelocatorFacadeFactory.create(getDownloadDirectoryPath(), Collections.singleton(Repository.central()));
            this.relocatorFactory = new JarFileRelocatorFactory(jarRelocatorFacadeFactory);
        }

        return relocatorFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyDataProviderFactory getModuleDataProviderFactory() {
        if (moduleDataProviderFactory == null) {
            final var gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), Collections.singleton(Repository.central()));
            this.moduleDataProviderFactory = new ExternalDependencyDataProviderFactory(gsonFacadeFactory);
        }

        return moduleDataProviderFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyDataProviderFactory getDataProviderFactory() {
        if (dataProviderFactory == null) {
            final var gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), Collections.singleton(Repository.central()));
            this.dataProviderFactory = new GsonDependencyDataProviderFactory(gsonFacadeFactory);
        }

        return dataProviderFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull PreResolutionDataProviderFactory getPreResolutionDataProviderFactory() {
        if (preResolutionDataProviderFactory == null) {
            final var gsonFacadeFactory = ReflectiveGsonFacadeFactory.create(getDownloadDirectoryPath(), Collections.singleton(Repository.central()));
            this.preResolutionDataProviderFactory = new GsonPreResolutionDataProviderFactory(gsonFacadeFactory);
        }

        return preResolutionDataProviderFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull RelocationHelperFactory getRelocationHelperFactory() {
        if (relocationHelperFactory == null) {
            final var checksumCalculator = new FileChecksumCalculator("SHA-256");
            final var pathStrategy = FilePathStrategy.createRelocationStrategy(getDownloadDirectoryPath().toFile(), getApplicationName());
            final var mediatorFactory = new FlatFileMetaMediatorFactory();
            this.relocationHelperFactory = new VerifyingRelocationHelperFactory(checksumCalculator, pathStrategy, mediatorFactory);
        }

        return relocationHelperFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyInjectorFactory getInjectorFactory() {
        if (injectorFactory == null) {
            this.injectorFactory = new SimpleDependencyInjectorFactory();
        }

        return injectorFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyResolverFactory getResolverFactory() {
        if (resolverFactory == null) {
            final var pinger = new HttpURLPinger();
            this.resolverFactory = new CachingDependencyResolverFactory(pinger);
        }

        return resolverFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull RepositoryEnquirerFactory getEnquirerFactory() {
        if (enquirerFactory == null) {
            final var releaseStrategy = new MavenPathResolutionStrategy();
            final var snapshotStrategy = new MavenSnapshotPathResolutionStrategy();
            final var resolutionStrategy = new MediatingPathResolutionStrategy(releaseStrategy, snapshotStrategy);
            final var pomURLCreationStrategy = new MavenPomPathResolutionStrategy();
            final var checksumResolutionStrategy = new MavenChecksumPathResolutionStrategy("SHA-1", resolutionStrategy);
            final var urlPinger = new HttpURLPinger();
            this.enquirerFactory = new PingingRepositoryEnquirerFactory(resolutionStrategy, checksumResolutionStrategy, pomURLCreationStrategy, urlPinger);
        }

        return enquirerFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyDownloaderFactory getDownloaderFactory() {
        if (downloaderFactory == null) {
            this.downloaderFactory = new URLDependencyDownloaderFactory();
        }

        return downloaderFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull DependencyVerifierFactory getVerifierFactory() {
        if (verifierFactory == null) {
            final var filePathStrategy = ChecksumFilePathStrategy.createStrategy(getDownloadDirectoryPath().toFile(), "SHA-1");
            final var checksumOutputFactory = new DependencyOutputWriterFactory(filePathStrategy);
            final var fallback = new PassthroughDependencyVerifierFactory();
            final var checksumCalculator = new FileChecksumCalculator("SHA-1");
            this.verifierFactory = new ChecksumDependencyVerifierFactory(checksumOutputFactory, fallback, checksumCalculator);
        }

        return verifierFactory;
    }

    @Contract(mutates = "this")
    protected final @NotNull MirrorSelector getMirrorSelector() {
        if (mirrorSelector == null) {
            mirrorSelector = new SimpleMirrorSelector();
        }

        return mirrorSelector;
    }

    @Contract(mutates = "this")
    protected final @NotNull ProcessLogger getLogger() {
        if (logger == null) {
            logger = (msg, args) -> {};
        }

        return logger;
    }

    @Contract(value = "-> new", mutates = "this")
    protected final @NotNull DependencyInjector createInjector() {
        final var injectionHelperFactory = new InjectionHelperFactory(
            getDownloadDirectoryPath(),
            getRelocatorFactory(),
            getDataProviderFactory(),
            getRelocationHelperFactory(),
            getInjectorFactory(),
            getResolverFactory(),
            getEnquirerFactory(),
            getDownloaderFactory(),
            getVerifierFactory(),
            getMirrorSelector()
        );

        return getInjectorFactory().create(injectionHelperFactory);
    }

    @Contract(value = "-> new", mutates = "this")
    public final @NotNull Application build() {
        final var mediatingLogger = LogDispatcher.getMediatingLogger();
        final var logger = getLogger();
        mediatingLogger.addLogger(logger);
        final var result = buildApplication();
        mediatingLogger.removeLogger(logger);

        return result;
    }

    @Contract(value = "-> new", mutates = "this")
    protected abstract @NotNull Application buildApplication();
}
