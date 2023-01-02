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
import arrow.core.getOrElse
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.racci.minix.gradle.ex.recursiveSubprojects
import dev.racci.slimjar.data.Targetable
import dev.racci.slimjar.extension.SlimJarExtension
import dev.racci.slimjar.extension.SlimJarJavaExtension
import dev.racci.slimjar.extension.SlimJarMultiplatformExtension
import dev.racci.slimjar.extensions.slimApiConfigurationName
import dev.racci.slimjar.extensions.slimConfigurationName
import dev.racci.slimjar.extensions.targetTask
import dev.racci.slimjar.task.SlimJarJavaTask
import dev.racci.slimjar.task.SlimJarMultiplatformTask
import io.github.slimjar.exceptions.ShadowNotFoundException
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
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.slf4j.LoggerFactory

public class SlimJarPlugin : Plugin<Project> {
    private val slimLogger = LoggerFactory.getLogger("SlimJar")

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

        if (configureForMPP(project) || configureForJava(project)) return@with

        error("SlimJar can only be applied to a Kotlin Multiplatform Project or a root project.")
    }

    private fun configureForJava(project: Project) = with(project) {
        if (parent != null) {
            slimLogger.info("Not configuring ${project.name} as root project because it has a parent.")
            return@with false
        }
        slimLogger.info("Configuring ${project.name} as root project.")

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

        createTask<SlimJarJavaTask>(null, extensions.create<SlimJarJavaExtension>(SLIM_EXTENSION_NAME.get()))

        val slimJarConfigurations = mutableListOf<Configuration>()
        recursiveSubprojects(true).forEach { sub ->
            // Applies Java if not present, since it's required for the compileOnly configuration.
            sub.plugins.apply(JavaPlugin::class)
            sub.createConfig(SLIM_CONFIGURATION_NAME.get()) {
                sub.configurations[JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME].extendsFrom(this)
                sub.configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
                slimJarConfigurations += this
            }

            // Configures the slimApi configuration if JavaLibraryPlugin is present
            sub.plugins.withType<JavaLibraryPlugin> {
                sub.createConfig(SLIM_API_CONFIGURATION_NAME.get()) {
                    sub.configurations[JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME].extendsFrom(this)
                    sub.configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
                    slimJarConfigurations += this
                }
            }
        }

        true
    }

    private fun configureForMPP(project: Project): Boolean = with(project) {
        if (!plugins.hasPlugin(KotlinMultiplatformPluginWrapper::class)) {
            slimLogger.info("Not configuring ${project.name} as MPP project because it does not have the Kotlin Multiplatform plugin.")
            return@with false
        }
        slimLogger.info("Configuring ${project.name} for Kotlin Multiplatform.")

        val mppExt = (kotlinExtension as KotlinMultiplatformExtension)
        // TODO: Support test targets as well.
        mppExt.sourceSets.all {
            slimLogger.info("Configuring sourceSet $name for Kotlin Multiplatform.")

            createConfig(SLIM_CONFIGURATION_NAME.forSourceSet(this)) { configurations[compileOnlyConfigurationName].extendsFrom(this) }
            createConfig(SLIM_API_CONFIGURATION_NAME.forSourceSet(this)) { configurations[apiConfigurationName].extendsFrom(this) }
        }

        mppExt.targets.all {
            if (platformType != KotlinPlatformType.jvm) {
                slimLogger.info("Not configuring target $name for Kotlin Multiplatform because it is not a JVM target.")
                return@all
            }
            slimLogger.info("Configuring target $name for Kotlin Multiplatform.")

            compilations[KotlinCompilation.MAIN_COMPILATION_NAME].allKotlinSourceSets.flatMap { set ->
                listOf(configurations[set.slimConfigurationName], configurations[set.slimApiConfigurationName])
            }

            val slimJarExt = extensions.create<SlimJarMultiplatformExtension>(SLIM_EXTENSION_NAME.forTarget(this), this)
            createTask<SlimJarMultiplatformTask>(this, slimJarExt, this)
        }

        true
    }

    private fun Project.createConfig(
        configurationName: String,
        configure: Configuration.() -> Unit = {}
    ): NamedDomainObjectProvider<Configuration> = Option.catch { project.configurations.named(configurationName).also { config -> config.configure(configure) } }.getOrElse {
        project.configurations.register(configurationName) {
            isTransitive = true
            configure()
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
            doFirst {
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
