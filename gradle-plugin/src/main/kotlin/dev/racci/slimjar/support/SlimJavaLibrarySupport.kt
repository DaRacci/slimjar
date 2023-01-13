package dev.racci.slimjar.support

import dev.racci.minix.gradle.support.PluginSupport
import io.github.slimjar.SlimJarPlugin
import io.github.slimjar.SlimJarPlugin.Companion.createConfig
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.get

public object SlimJavaLibrarySupport : PluginSupport(
    "java-library",
    { JavaLibraryPlugin::class }
) {
    override fun configureRoot(project: Project): Unit = with(project) {
        createConfig(SlimJarPlugin.SLIM_API_CONFIGURATION_NAME.get()) {
            configurations[JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME].extendsFrom(this)
            configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
        }
    }

    override fun configureSub(project: Project): Unit = with(project) {
        createConfig(SlimJarPlugin.SLIM_API_CONFIGURATION_NAME.get()) {
            configurations[JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME].extendsFrom(this)
            configurations[JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME].extendsFrom(this)
        }
    }
}
