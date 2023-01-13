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

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.racci.minix.gradle.MinixGradlePlugin
import dev.racci.slimjar.data.Targetable
import dev.racci.slimjar.extension.SlimJarExtension
import dev.racci.slimjar.extensions.targetTask
import io.github.slimjar.exceptions.ShadowNotFoundException
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.GradleException
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.hasPlugin
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.LoggerFactory

public class SlimJarPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        if (!plugins.hasPlugin(ShadowPlugin::class)) {
            throw ShadowNotFoundException(
                """
                    SlimJar depends on the Shadow plugin, please apply the plugin.
                    For more information visit: https://imperceptiblethoughts.com/shadow/
                """.trimMargin()
            )
        }

        if (!plugins.hasPlugin(MinixGradlePlugin::class)) {
            throw GradleException(
                """
                    SlimJar depends on the Minix plugin, please apply the plugin.
                    For more information visit: https://github.com/DaRacci/Minix-Conventions/
                """.trimIndent()
            )
        }

        if (!plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            logger.warn("Eagerly applying java to the project because multiplatform was not found!")
            apply(plugin = "java")
        }
    }

    public companion object {
        public val SLIM_CONFIGURATION_NAME: Targetable = Targetable("slim")
        public val SLIM_API_CONFIGURATION_NAME: Targetable = Targetable("slimApi")
        public val SLIM_JAR_TASK_NAME: Targetable = Targetable("slimJar")
        public val SLIM_EXTENSION_NAME: Targetable = Targetable("slimJar")

        internal fun Project.createConfig(
            configurationName: String,
            configure: Configuration.() -> Unit = {}
        ): NamedDomainObjectProvider<Configuration> = project.configurations.register(configurationName) { config ->
            config.isTransitive = true
            config.configure()
        }

        internal inline fun <reified T : SlimJarTask> Project.createTask(
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
                        relocate(rule.originalPackagePattern(), rule.relocatedPackagePattern()) {
                            rule.inclusions().forEach { include(it) }
                            rule.exclusions().forEach { exclude(it) }
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
}

internal fun slimJarLib(version: String) = "dev.racci.slimjar:slimjar:$version"
