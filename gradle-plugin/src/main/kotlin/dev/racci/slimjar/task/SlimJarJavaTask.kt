package dev.racci.slimjar.task

import dev.racci.slimjar.extension.SlimJarJavaExtension
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_API_CONFIGURATION_NAME
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_CONFIGURATION_NAME
import io.github.slimjar.targetedJarTask
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import javax.inject.Inject

public open class SlimJarJavaTask @Inject constructor() : SlimJarTask() {
    public final override val outputDirectory: File = project.buildDir.resolve("resources/slimjar/").also(File::mkdirs)

    // Only one configuration will be present for pure Java projects.
    @Transient public final override val slimJarExtension: SlimJarJavaExtension = project.extensions.getByType()

    @Transient
    public final override val slimjarConfigurations: SetProperty<Configuration> = project.objects
        .setProperty<Configuration>().convention(
            arrayOf(SLIM_CONFIGURATION_NAME, SLIM_API_CONFIGURATION_NAME).mapNotNull {
                project.configurations.findByName(it.get())
            }
        ).also(SetProperty<*>::disallowChanges)

    init {
        group = TASK_GROUP
        inputs.files(slimjarConfigurations)
        outputs.dir(outputDirectory)

        dependsOn(project.tasks.targetedJarTask)
    }

    @TaskAction
    public fun includeIsolatedJars(): Unit = with(project) {
        slimJarExtension.isolatedProjects.get().filter { it != this }.forEach { target ->
            target.tasks.targetedJarTask.apply {
                val archive = outputs.files.singleFile

                with(outputDirectory.resolve("${target.name}.isolated-jar")) {
                    archive.copyTo(this, true)
                    withShadowTask { from(this) }
                }
            }
        }
    }
}
