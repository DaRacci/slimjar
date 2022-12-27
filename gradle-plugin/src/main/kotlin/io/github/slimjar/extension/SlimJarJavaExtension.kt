package io.github.slimjar.extension

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import io.github.slimjar.SlimJarPlugin
import io.github.slimjar.func.slimInjectToIsolated
import io.github.slimjar.relocation.RelocationRule
import io.github.slimjar.resolver.data.Mirror
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.gradle.kotlin.dsl.withType
import javax.inject.Inject

public open class SlimJarJavaExtension @Inject constructor(
    project: Project
) : SlimJarExtension() {

    final override val relocations: SetProperty<RelocationRule> = project.objects.setProperty()

    final override val mirrors: SetProperty<Mirror> = project.objects.setProperty()

    final override val compileTimeResolution: Property<Boolean> = project.objects.property<Boolean>().convention(true)

    public val isolatedProjects: SetProperty<Project> = project.objects.setProperty()

    public fun isolate(target: Project) {
        isolatedProjects.add(target)

        if (target.slimInjectToIsolated) {
            target.pluginManager.apply(ShadowPlugin::class.java)
            target.pluginManager.apply(SlimJarPlugin::class.java)
            target.getTasksByName("slimJar", true).firstOrNull()?.setProperty("shade", false)
        }

        target.tasks {
            val jarTask = findByName("reobfJar")
                ?: findByName("shadowJar")
                ?: findByName("jar") ?: return@tasks

            withType<SlimJarTask> { dependsOn(jarTask) }
        }
    }
}
