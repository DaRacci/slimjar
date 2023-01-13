package dev.racci.slimjar.support

import dev.racci.minix.gradle.ex.project
import dev.racci.minix.gradle.support.AbstractMultiplatformSupport
import dev.racci.slimjar.extension.SlimJarMultiplatformExtension
import dev.racci.slimjar.extensions.slimApiConfigurationName
import dev.racci.slimjar.extensions.slimConfigurationName
import dev.racci.slimjar.task.SlimJarMultiplatformTask
import io.github.slimjar.SlimJarPlugin
import io.github.slimjar.SlimJarPlugin.Companion.createConfig
import io.github.slimjar.SlimJarPlugin.Companion.createTask
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

public object SlimMultiplatformSupport : AbstractMultiplatformSupport(
    KotlinPlatformType.jvm
) {
    override fun configureTargetFiltered(target: KotlinTarget): Unit = with(target) {
        compilations[KotlinCompilation.MAIN_COMPILATION_NAME].allKotlinSourceSets.flatMap { set ->
            listOf(
                project.configurations[set.slimConfigurationName],
                project.configurations[set.slimApiConfigurationName]
            )
        }

        val slimJarExt = project.extensions.create<SlimJarMultiplatformExtension>(
            SlimJarPlugin.SLIM_EXTENSION_NAME.forTarget(target),
            target
        )

        project.createTask<SlimJarMultiplatformTask>(target, slimJarExt, target)
    }

    override fun configureSource(source: KotlinSourceSet): Unit = with(source) {
        val project = source.project()
        project.createConfig(SlimJarPlugin.SLIM_CONFIGURATION_NAME.forSourceSet(this)) {
            project.configurations[compileOnlyConfigurationName].extendsFrom(this)
        }
        project.createConfig(SlimJarPlugin.SLIM_API_CONFIGURATION_NAME.forSourceSet(this)) {
            project.configurations[apiConfigurationName].extendsFrom(this)
        }
    }
}
