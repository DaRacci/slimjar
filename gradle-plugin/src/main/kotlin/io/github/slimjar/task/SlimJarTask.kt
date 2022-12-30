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
import io.github.slimjar.extension.SlimJarExtension
import io.github.slimjar.extensions.maybePrefix
import io.github.slimjar.func.performCompileTimeResolution
import io.github.slimjar.resolver.CachingDependencyResolver
import io.github.slimjar.resolver.ResolutionResult
import io.github.slimjar.resolver.data.Dependency
import io.github.slimjar.resolver.data.DependencyData
import io.github.slimjar.resolver.data.Mirror
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
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
import javax.inject.Inject

@CacheableTask
@OptIn(ExperimentalSerializationApi::class)
public abstract class SlimJarTask @Inject constructor() : DefaultTask() {

    protected companion object {
        public const val TASK_GROUP: String = "slimJar"
    }

    @get:OutputDirectory
    public abstract val outputDirectory: File

    @get:Internal
    public abstract val slimJarExtension: SlimJarExtension

    @get:Internal
    public abstract val slimjarConfigurations: SetProperty<Configuration>

    /**
     * Action to generate the json file inside the jar
     */
    @TaskAction
    internal fun createJson() = with(project) {
        val repositories = repositories.getMavenRepos()
        val dependencies = slimjarConfigurations.get().flatMap { it.incoming.getSlimDependencies() }

        with(outputDirectory.resolve("slimjar.json")) {
            val dependencyData = DependencyData(slimJarExtension.mirrors.get(), repositories, dependencies, slimJarExtension.relocations.get())
            outputStream().use { Json.encodeToStream(dependencyData, it) }
            withShadowTask { from(this) }
        }
    }

    @TaskAction
    internal fun generateResolvedDependenciesFile() = with(project) {
        if (!project.performCompileTimeResolution) return@with

        val file = outputDirectory.resolve("slimjar-resolutions.json")
        val preResolved: MutableMap<String, ResolutionResult> = if (file.exists()) {
            file.inputStream().use(Json::decodeFromStream)
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

        val result = mutableMapOf<String, ResolutionResult>()
        runBlocking(IO) {
            dependencies.asFlow()
                .filter {
                    preResolved[it.toString()]?.let { pre ->
                        repositories.repository { r -> pre.repository.url().toString() == r.url().toString() }
                    } ?: true
                }.concurrentMap(this, 16) {
                    it.toString() to resolver.resolve(it).orElse(null)
                }.onEach { result[it.first] = it.second }.collect()
        }

        preResolved.forEach { result.putIfAbsent(it.key, it.value) }

        with(file) {
            outputStream().use { Json.encodeToStream(result, it) }
            withShadowTask { from(this) }
        }
    }

    /**
     * Turns a [RenderableDependency] into a [Dependency] with all its transitives.
     */
    private fun RenderableDependency.toSlimDependency(): Dependency? {
        return id.toString().toDependency(collectTransitive(children))
    }

    /**
     * Recursively flattens the transitive dependencies.
     */
    private fun collectTransitive(
        dependencies: Set<RenderableDependency>
    ): Sequence<Dependency> = sequence { // TODO: Might be better to use a flow here // Might also just not work
        dependencies.forEach { renderable ->
            val dependency = renderable.id.toString().toDependency(emptySequence()) ?: return@forEach
            yield(dependency)
            yieldAll(collectTransitive(renderable.children))
        }
    }.distinct()

    /**
     * Creates a [Dependency] based on a string
     * group:artifact:version:snapshot - The snapshot is the only nullable value.
     */
    private fun String.toDependency(transitive: Sequence<Dependency>): Dependency? {
        val array = arrayOfNulls<Any>(5)
        array[4] = transitive

        split(":").takeIf { it.size >= 3 }?.forEachIndexed { index, s ->
            array[index] = s
        } ?: return null

        return Dependency::class.java.constructors.first().newInstance(*array) as Dependency
    }

    private fun RepositoryHandler.getMavenRepos() = filterIsInstance<MavenArtifactRepository>()
        .filterNot { it.url.toString().startsWith("file") }
        .toSet()
        .map { Repository(it.url.toURL()) }

    private fun ResolvableDependencies.getSlimDependencies(): List<Dependency> = RenderableModuleResult(this.resolutionResult.root)
        .children.mapNotNull { it.toSlimDependency() }

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

    private object DependencyDataSerializer : KSerializer<DependencyData> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DependencyData") {
            element<List<Mirror>>("mirrors")
            element<List<Repository>>("repositories")
            element<List<Dependency>>("dependencies")
        }

        override fun deserialize(decoder: Decoder): DependencyData {
            val input = decoder.beginStructure(descriptor)
            var mirrors: List<Mirror>? = null
            var repositories: List<Repository>? = null
            var dependencies: List<Dependency>? = null
            while (true) {
                when (val index = input.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> mirrors = input.decodeSerializableElement(descriptor, index, ListSerializer(MirrorSerializer))
                    1 -> repositories = input.decodeSerializableElement(descriptor, index, ListSerializer(RepositorySerializer))
                    2 -> dependencies = input.decodeSerializableElement(descriptor, index, ListSerializer(DependencySerializer))
                    else -> error("Unexpected index: $index")
                }
            }
            input.endStructure(descriptor)
            return DependencyData(mirrors!!, repositories!!, dependencies!!, emptyList())
        }

        override fun serialize(
            encoder: Encoder,
            value: DependencyData
        ) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(descriptor, 0, ListSerializer(Mirror.serializer()), value.mirrors())
                encodeSerializableElement(descriptor, 1, ListSerializer(Repository.serializer()), value.repositories())
                encodeSerializableElement(descriptor, 2, ListSerializer(Dependency.serializer()), value.dependencies())
            }
        }
    }
}
