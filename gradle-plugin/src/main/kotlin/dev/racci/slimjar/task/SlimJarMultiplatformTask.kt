package dev.racci.slimjar.task

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.racci.slimjar.extension.SlimJarExtension
import dev.racci.slimjar.extensions.maybePrefix
import dev.racci.slimjar.extensions.slimApiConfigurationName
import dev.racci.slimjar.extensions.slimConfigurationName
import dev.racci.slimjar.extensions.slimJar
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.setProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import javax.inject.Inject

@CacheableTask
public open class SlimJarMultiplatformTask @Inject constructor(
    @Transient private val target: KotlinTarget
) : SlimJarTask() {
    final override val outputDirectory: File = project.buildDir
        .resolve("generated/slimjar/${target.name}").also(File::mkdirs)

    @Transient
    final override val slimJarExtension: SlimJarExtension = target.slimJar

    @Transient
    final override val slimjarConfigurations: SetProperty<Configuration> = project.objects
        .setProperty<Configuration>().convention(
            target.compilations[KotlinCompilation.MAIN_COMPILATION_NAME].allKotlinSourceSets.flatMap { set ->
                listOf(
                    target.project.configurations[set.slimConfigurationName],
                    target.project.configurations[set.slimApiConfigurationName]
                )
            }
        ).also(SetProperty<*>::disallowChanges)

    init {
        group = TASK_GROUP
        inputs.files(slimjarConfigurations)
        outputs.dir(outputDirectory)
        target.compilations[KotlinCompilation.MAIN_COMPILATION_NAME].defaultSourceSet {
            resources.srcDir(outputDirectory)
        }
    }

    override fun withShadowTask(
        action: ShadowJar.() -> Unit
    ): ShadowJar? = (project.tasks.findByName(maybePrefix(target.name, null, "shadowJar")) as? ShadowJar)?.apply(action)
}
