package dev.racci.slimjar.extensions

import arrow.core.Option
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_API_CONFIGURATION_NAME
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_CONFIGURATION_NAME
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_EXTENSION_NAME
import io.github.slimjar.SlimJarPlugin.Companion.SLIM_JAR_TASK_NAME
import dev.racci.slimjar.data.Targetable
import dev.racci.slimjar.extension.SlimJarExtension
import io.github.slimjar.task.SlimJarTask
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * Gets a provider for the task prefixed with the targets name.
 *
 * @param taskName The name of the task to get.
 * @param T The type of the task.
 * @return The task provider.
 */
public inline fun <reified T : Task> KotlinTarget.targetTask(
    taskName: String,
    noinline configure: T.() -> Unit = {}
): TaskProvider<T> = project.tasks.named(maybePrefix(this, "", taskName), T::class).also { it.configure(configure) }

/**
 * Gets a provider for the task prefixed with the targets name.
 *
 * @param commonTask The common task that's name will be used.
 * @param T The type of the task.
 * @return The task provider.
 */
public inline fun <reified T : Task> KotlinTarget.targetTask(
    commonTask: T,
    noinline configure: T.() -> Unit = {}
): TaskProvider<T> = targetTask(commonTask.name, configure)

/**
 * Gets a provider for the task prefixed with the targets name.
 *
 * @param commonTask The common task that's name will be used.
 * @param T The type of the task.
 * @return The task provider.
 */
public inline fun <reified T : Task> KotlinTarget.targetTask(
    commonTask: TaskProvider<T>,
    noinline configure: T.() -> Unit = {}
): TaskProvider<T> = targetTask(commonTask.get(), configure)

public inline fun <reified T : Task> KotlinTarget.nullableTargetTask(taskName: String): TaskProvider<T>? = runCatching { targetTask<T>(taskName) }.getOrNull()

public val KotlinSourceSet.slimConfiguration: Option<Configuration>
    get() = Option.fromNullable(project().configurations.findByName(slimConfigurationName))

public val KotlinSourceSet.slimApiConfiguration: Option<Configuration>
    get() = Option.fromNullable(project().configurations.findByName(slimApiConfigurationName))

public val KotlinTarget.slimJarTaskName: String
    get() = SLIM_JAR_TASK_NAME.forTarget(this)

public val KotlinTarget.slimJarTask: TaskProvider<SlimJarTask>
    get() = project.tasks.named<SlimJarTask>(slimJarTaskName)

public val KotlinTarget.slimJar: SlimJarExtension
    get() = project.extensions.getByName<SlimJarExtension>(SLIM_EXTENSION_NAME.forTarget(this))

public fun KotlinTarget.slimJarTask(action: Action<in SlimJarTask>): Unit = slimJarTask.configure(action)

public fun KotlinTarget.slimJar(action: Action<in SlimJarExtension>): SlimJarExtension = slimJar.also(action::execute)

@PublishedApi
internal val KotlinSourceSet.slimConfigurationName: String
    get() = SLIM_CONFIGURATION_NAME.forSourceSet(this)

@PublishedApi
internal val KotlinSourceSet.slimApiConfigurationName: String
    get() = SLIM_API_CONFIGURATION_NAME.forSourceSet(this)

@PublishedApi
internal fun KotlinSourceSet.project(): Project {
    return this::class.java.getDeclaredField("project").apply { isAccessible = true }.get(this) as Project
}

@PublishedApi
internal inline fun <reified T : Task> TaskContainer.targetTask(
    named: Named? = null,
    taskName: String,
    noinline configure: T.() -> Unit = {}
): TaskProvider<T> = named(Targetable.prefixable(named, SourceSet.MAIN_SOURCE_SET_NAME, taskName), T::class).also { task -> task.configure(configure) }

@PublishedApi
internal fun maybePrefix(
    named: String?,
    mainName: String? = null,
    string: String
): String = if (named == null || named == mainName) {
    string
} else "${named.replaceFirstChar(Char::lowercaseChar)}${string.capitalized()}"

@PublishedApi
internal fun maybePrefix(
    named: Named?,
    mainName: String,
    string: String
): String = maybePrefix(named?.name, mainName, string)
