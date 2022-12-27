package io.github.slimjar // ktlint-disable filename

import io.github.slimjar.extension.SlimJarExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.HasConfigurableValue
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByType

internal val TaskContainer.targetedJarTask: Task get() {
    return findByName("reobfJar")
        ?: findByName("shadowJar")
        ?: findByName("jar")
        ?: error("No jar task found")
}

internal val Project.slimExtension: SlimJarExtension get() = extensions.getByType()

internal fun <T : HasConfigurableValue> T.andFinalizeValueOnRead(): T = apply { finalizeValueOnRead() }
internal fun <T : HasConfigurableValue> T.andDisallowUnsafeRead(): T = apply { disallowChanges() }
