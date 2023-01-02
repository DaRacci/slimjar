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

package io.github.slimjar.task

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.racci.slimjar.extension.SlimJarExtension
import dev.racci.slimjar.extensions.maybePrefix
import io.github.slimjar.func.performCompileTimeResolution
import io.github.slimjar.resolver.CachingDependencyResolver
import io.github.slimjar.resolver.ResolutionResult
import io.github.slimjar.resolver.data.Dependency
import io.github.slimjar.resolver.data.DependencyData
import io.github.slimjar.resolver.data.Repository
import io.github.slimjar.resolver.enquirer.PingingRepositoryEnquirerFactory
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector
import io.github.slimjar.resolver.pinger.HttpURLPinger
import io.github.slimjar.resolver.strategy.MavenChecksumPathResolutionStrategy
import io.github.slimjar.resolver.strategy.MavenPathResolutionStrategy
import io.github.slimjar.resolver.strategy.MavenPomPathResolutionStrategy
import io.github.slimjar.resolver.strategy.MavenSnapshotPathResolutionStrategy
import io.github.slimjar.resolver.strategy.MediatingPathResolutionStrategy
import io.github.slimjar.slimExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult
import java.io.File
import java.net.URL
import javax.inject.Inject

@CacheableTask
public abstract class SlimJarTask @Inject constructor(@Transient private val extension: SlimJarExtension) : DefaultTask() {

    protected companion object {
        public const val TASK_GROUP: String = "slimJar"
        public val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    }

    @get:OutputDirectory
    public abstract val outputDirectory: File

    @get:Internal
    public abstract val slimJarExtension: SlimJarExtension

    @get:Internal
    public abstract val slimjarConfigurations: SetProperty<Configuration>

    /** Action to generate the json file inside the jar */
    @TaskAction
    internal fun createJson() = with(project) {
        val repositories = repositories.getMavenRepos()
        val dependencies = slimjarConfigurations.get().flatMap { it.incoming.getSlimDependencies() }

        with(outputDirectory.resolve("slimjar.json")) {
            val dependencyData = DependencyData(slimJarExtension.mirrors.get(), repositories, dependencies, slimJarExtension.relocations.get())
            writer().use { writer -> GSON.toJson(dependencyData, writer) }
            withShadowTask { from(this) }
        }
    }

    @TaskAction
    internal fun generateResolvedDependenciesFile() = with(project) {
        if (!project.performCompileTimeResolution) return@with

        val file = outputDirectory.resolve("slimjar-resolutions.json")
        val preResolved: Map<String, ResolutionResult> = if (file.exists()) {
            file.reader().use { reader -> GSON.fromJson(reader, object : TypeToken<Map<String, ResolutionResult>>() {}.type) }
        } else mutableMapOf()

        val dependencies = slimjarConfigurations.get().flatMap { it.incoming.getSlimDependencies() }.toMutableSet().flatten()
        val repositories = repositories.getMavenRepos()

        val releaseStrategy = MavenPathResolutionStrategy()
        val snapshotStrategy = MavenSnapshotPathResolutionStrategy()
        val resolutionStrategy = MediatingPathResolutionStrategy(releaseStrategy, snapshotStrategy)
        val pomURLCreationStrategy = MavenPomPathResolutionStrategy()
        val checksumResolutionStrategy = MavenChecksumPathResolutionStrategy("SHA-1", resolutionStrategy)
        val urlPinger = HttpURLPinger()
        val enquirerFactory = PingingRepositoryEnquirerFactory(
            resolutionStrategy,
            checksumResolutionStrategy,
            pomURLCreationStrategy,
            urlPinger
        )
        val mirrorSelector = SimpleMirrorSelector()
        val resolver = CachingDependencyResolver(
            urlPinger,
            mirrorSelector.select(repositories, slimExtension.mirrors.get()),
            enquirerFactory,
            mapOf()
        )

        val results = mutableMapOf<String, ResolutionResult>()
        // TODO: Cleanup this mess
        runBlocking(IO) {
            val globalRepositoryEnquirer = extension.globalRepositories.map { repos ->
                repos.map { repoString -> enquirerFactory.create(Repository(URL(repoString))) }
            }

            dependencies.asFlow()
                .filter { dep ->
                    // TODO: Ensure existing results match global if present
                    preResolved[dep.toString()]?.let { pre ->
                        repositories.none { r -> pre.repository().url().toString() == r.url().toString() }
                    } ?: true
                }.concurrentMap(this, 16) { dep ->
                    dep to if (globalRepositoryEnquirer.isPresent) {
                        resolver.resolve(dep, globalRepositoryEnquirer.get())
                    } else {
                        resolver.resolve(dep)
                    }
                }.filter { (dep, result) ->
                    if (!result.isEmpty) return@filter true

                    logger.warn("Failed to resolve dependency $dep")
                    if (extension.requirePreResolve.get()) {
                        error(
                            """
                            Failed to resolve dependency $dep during pre-resolve.
                            Please ensure that the dependency is available in the gradle repositories or global repositories.
                            Or disable required pre-resolve in the slimJar extension.
                            """.trimIndent()
                        )
                    }

                    false
                }.map { (dep, result) -> dep to result.get() }.onEach { (dep, result) ->
                    if (!extension.requireChecksum.get() || result.checksumURL != null) return@onEach
                    logger.warn("Failed to resolve checksum for dependency $dep")
                    error(
                        """
                            Failed to resolve checksum for dependency $dep during pre-resolve.
                            Please ensure that the dependency has a checksum.
                            Or disable required checksum in the slimJar extension.
                        """.trimIndent()
                    )
                }.onEach { (dep, result) -> results[dep.toString()] = result }.collect()
        }

        preResolved.forEach { results.putIfAbsent(it.key, it.value) }

        with(file) {
            writer().use { writer -> GSON.toJson(results, writer) }
            withShadowTask { from(this) }
        }
    }

    /**
     * Turns a [RenderableDependency] into a [Dependency] with all its
     * transitives.
     */
    private fun RenderableDependency.toSlimDependency(): Dependency? {
        return id.toString().toDependency(collectTransitive(children))
    }

    /** Recursively flattens the transitive dependencies. */
    private fun collectTransitive(
        dependencies: Collection<RenderableDependency>,
        transitive: MutableSet<Dependency> = mutableSetOf(),
    ): Set<Dependency> {
        for (dependency in dependencies) {
            val dep = dependency.id.toString().toDependency(emptyList()) ?: continue
            if (dep in transitive) continue
            if (dep.artifactId().endsWith("-bom")) continue

            transitive.add(dep)
            collectTransitive(dependency.children, transitive)
        }

        return transitive
    }

//    private fun collectTransitive(
//        dependencies: Collection<RenderableDependency>
//    ): Sequence<Dependency> = sequence {
//        for (dependency in dependencies) {
//            val dep = dependency.id.toString().toDependency(emptySequence()) ?: continue
//            yield(dep)
//            yieldAll(collectTransitive(dependency.children))
//        }
//    }

    /**
     * Creates a [Dependency] based on a string
     * group:artifact:version:snapshot - The
     * snapshot is the only nullable value.
     */
    private fun String.toDependency(transitive: Collection<Dependency>): Dependency? {
        val array = arrayOfNulls<Any>(5)
        array[4] = transitive.toList()

        split(":").takeIf { it.size >= 3 }?.forEachIndexed { index, s ->
            array[index] = s
        } ?: return null

        println("Creating dependency from ${array.joinToString(", ") { "$it (${it?.javaClass})" }}")

        return Dependency::class.java.constructors.first().newInstance(*array) as Dependency
    }

    private fun RepositoryHandler.getMavenRepos() = filterIsInstance<MavenArtifactRepository>()
        .filterNot { it.url.toString().startsWith("file") }
        .toSet()
        .map { Repository(it.url.toURL()) }

    private fun ResolvableDependencies.getSlimDependencies(): List<Dependency> =
        RenderableModuleResult(this.resolutionResult.root).children
            .mapNotNull { it.toSlimDependency() }
            .filterNot { it.artifactId().endsWith("-bom") }

    private fun Collection<Dependency>.flatten(): MutableSet<Dependency> {
        return this.flatMap { it.transitive().flatten() + it }.toMutableSet()
    }

    private fun <T, R> Flow<T>.concurrentMap(
        scope: CoroutineScope,
        concurrencyLevel: Int,
        transform: suspend (T) -> R
    ): Flow<R> = this
        .map { scope.async { transform(it) } }
        .buffer(concurrencyLevel)
        .map { it.await() }

    protected open fun withShadowTask(
        action: ShadowJar.() -> Unit
    ): ShadowJar? = (project.tasks.findByName(maybePrefix(null, null, "shadowJar")) as? ShadowJar)?.apply(action)
}
