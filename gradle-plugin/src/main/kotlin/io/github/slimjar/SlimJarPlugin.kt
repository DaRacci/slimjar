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

package io.github.slimjar

import arrow.core.Option
import arrow.core.flattenOption
import arrow.core.getOrElse
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.slimjar.data.Targetable
import io.github.slimjar.exceptions.ShadowNotFoundException
import io.github.slimjar.extension.SlimJarExtension
import io.github.slimjar.extension.SlimJarMultiplatformExtension
import io.github.slimjar.extensions.slimApiConfiguration
import io.github.slimjar.extensions.slimConfiguration
import io.github.slimjar.extensions.targetTask
import io.github.slimjar.task.SlimJarJavaTask
import io.github.slimjar.task.SlimJarMultiplatformTask
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

public class SlimJarPlugin : Plugin<Project> {
    public companion object {
        public val SLIM_CONFIGURATION_NAME: Targetable = Targetable("slim")
        public val SLIM_API_CONFIGURATION_NAME: Targetable = Targetable("slimApi")
        public val SLIM_JAR_TASK_NAME: Targetable = Targetable("slimJar")
        public val SLIM_EXTENSION_NAME: Targetable = Targetable("slimJar")
    }

    override fun apply(project: Project): Unit = with(project) {
        if (!plugins.hasPlugin(ShadowPlugin::class)) {
            throw ShadowNotFoundException("SlimJar depends on the Shadow plugin, please apply the plugin. For more information visit: https://imperceptiblethoughts.com/shadow/")
        }

        if (configureForMPP(project) || configurePossibleRoot(project)) return@with

        error("SlimJar can only be applied to a Kotlin Multiplatform Project or a root project.")
    }

    private fun configurePossibleRoot(project: Project): Boolean = with(project) {
        if (parent != null) {
            logger.info("Not configuring ${project.name} as root project because it has a parent.")
            return@with false
        }
        logger.info("Configuring ${project.name} as root project.")

        // Applies Java if not present, since it's required for the compileOnly configuration.
        plugins.apply(JavaPlugin::class)
        createConfig(SLIM_CONFIGURATION_NAME.get()) {
            configurations[JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME].extendsFrom(this)
            configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
        }

        // Configures the slimApi configuration if JavaLibraryPlugin is present
        plugins.withType<JavaLibraryPlugin> {
            createConfig(SLIM_API_CONFIGURATION_NAME.get()) {
                configurations[JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME].extendsFrom(this)
                configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
            }
        }

        createTask<SlimJarJavaTask>(null, extensions.create(SLIM_EXTENSION_NAME.get()))

        true
    }

    private fun configureForMPP(project: Project): Boolean = with(project) {
        if (!plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            logger.info("Not configuring ${project.name} as MPP project because it does not have the Kotlin Multiplatform plugin.")
            return@with false
        }
        logger.info("Configuring ${project.name} for Kotlin Multiplatform.")

        val mppExt = (kotlinExtension as KotlinMultiplatformExtension)
        // TODO: Support test targets as well.
        mppExt.sourceSets.all { set ->
            logger.info("Configuring sourceSet ${set.name} for Kotlin Multiplatform.")

            createConfig(SLIM_CONFIGURATION_NAME.forSourceSet(set)) { configurations[set.compileOnlyConfigurationName].extendsFrom(this) }
            createConfig(SLIM_API_CONFIGURATION_NAME.forSourceSet(set)) { configurations[set.apiConfigurationName].extendsFrom(this) }
        }

        mppExt.targets.all { target ->
            logger.info("Configuring target ${target.name} for Kotlin Multiplatform.")

            target.compilations[KotlinCompilation.MAIN_COMPILATION_NAME].allKotlinSourceSets.flatMap { set ->
                listOf(set.slimConfiguration, set.slimApiConfiguration)
            }.flattenOption().toTypedArray()

            val slimJarExt = extensions.create<SlimJarMultiplatformExtension>(SLIM_EXTENSION_NAME.forTarget(target), target)
            createTask<SlimJarMultiplatformTask>(target, slimJarExt, target)
        }

        true
    }

    private fun Project.createConfig(
        configurationName: String,
        configure: Configuration.() -> Unit = {}
    ): NamedDomainObjectProvider<Configuration> = Option.catch { project.configurations.named(configurationName).also { config -> config.configure(configure) } }.getOrElse {
        project.configurations.register(configurationName) { config ->
            config.isTransitive = true
            config.configure()
        }
    }

    private inline fun <reified T : SlimJarTask> Project.createTask(
        target: Named? = null,
        extension: SlimJarExtension,
        vararg constructorArgs: Any
    ) {
        val slimJarTask = tasks.create<T>(SLIM_JAR_TASK_NAME.forNamed(target), *constructorArgs)

        // The fuck does this do?
        dependencies.extra.set(
            "slimjar",
            asGroovyClosure("+", ::slimJarLib)
        )

        // Hooks into shadow to inject relocations
        tasks.targetTask<ShadowJar>(target, "shadowJar") {
            doFirst { _ ->
                extension.relocations.get().forEach { rule ->
                    relocate(rule.originalPackagePattern, rule.relocatedPackagePattern) {
                        rule.inclusions.forEach { include(it) }
                        rule.exclusions.forEach { exclude(it) }
                    }
                }
            }
        }

        // Runs the task once resources are being processed to save the json file.
        tasks.targetTask<ProcessResources>(target, JavaPlugin.PROCESS_RESOURCES_TASK_NAME) {
            finalizedBy(slimJarTask)
        }
    }
}

internal fun slimJarLib(version: String) = "dev.racci.slimjar:slimjar:$version"
