package io.github.slimjar.task

import io.github.slimjar.SlimJarPlugin.Companion.SLIM_API_CONFIGURATION_NAME
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_CONFIGURATION_NAME
import io.github.slimjar.extension.SlimJarJavaExtension
import io.github.slimjar.targetedJarTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File
import javax.inject.Inject

public open class SlimJarJavaTask @Inject constructor() : SlimJarTask() {
    public final override val buildDirectory: File = project.buildDir

    public final override val outputDirectory: File = buildDirectory.resolve("resources/slimjar/")

    // Only one configuration will be present for pure Java projects.
    public final override val slimJarExtension: SlimJarJavaExtension = project.extensions.getByType()

    public final override val slimjarConfigurations: List<Configuration> = arrayOf(SLIM_CONFIGURATION_NAME, SLIM_API_CONFIGURATION_NAME).mapNotNull {
        project.configurations.findByName(it.get())
    }

    init {
        group = TASK_GROUP
        inputs.files(slimjarConfigurations)
        dependsOn(project.tasks.targetedJarTask)
    }

    @TaskAction
    public fun includeIsolatedJars(): Unit = with(project) {
        slimJarExtension.isolatedProjects.get().filter { it != this }.forEach { target ->
            target.tasks.targetedJarTask.apply {
                val archive = outputs.files.singleFile

                ensureOutputDir()
                with(outputDirectory.resolve("${target.name}.isolated-jar")) {
                    archive.copyTo(this, true)
                    withShadowTask { from(this) }
                }
            }
        }
    }
}
