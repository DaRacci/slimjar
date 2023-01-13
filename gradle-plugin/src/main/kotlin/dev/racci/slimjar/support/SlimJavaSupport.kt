package dev.racci.slimjar.support

import dev.racci.minix.gradle.support.PluginSupport
import dev.racci.slimjar.extension.SlimJarJavaExtension
import dev.racci.slimjar.task.SlimJarJavaTask
import io.github.slimjar.SlimJarPlugin
import io.github.slimjar.SlimJarPlugin.Companion.createConfig
import io.github.slimjar.SlimJarPlugin.Companion.createTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

public object SlimJavaSupport : PluginSupport(
    "java",
    { JavaPlugin::class }
) {
    override fun configureRoot(project: Project): Unit = with(project) {
        createConfig(SlimJarPlugin.SLIM_CONFIGURATION_NAME.get()) {
            configurations[JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME].extendsFrom(this)
            configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
        }

        createTask<SlimJarJavaTask>(
            null,
            extensions.create<SlimJarJavaExtension>(SlimJarPlugin.SLIM_EXTENSION_NAME.get())
        )
    }

    override fun configureSub(project: Project): Unit = with(project) {
        createConfig(SlimJarPlugin.SLIM_CONFIGURATION_NAME.get()) {
            configurations[JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME].extendsFrom(this)
            configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
            // rootProject.configurations[SlimJarPlugin.SLIM_CONFIGURATION_NAME.get()].extendsFrom(this)
        }
    }
}
