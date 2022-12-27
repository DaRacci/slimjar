package io.github.slimjar.extension

import io.github.slimjar.andDisallowUnsafeRead
import io.github.slimjar.andFinalizeValueOnRead
import io.github.slimjar.relocation.RelocationConfig
import io.github.slimjar.relocation.RelocationRule
import io.github.slimjar.resolver.data.Mirror
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

public abstract class SlimJarExtension protected constructor(project: Project) {

    @get:Input
    @get:Optional
    public val relocations: SetProperty<RelocationRule> = project.objects.setProperty<RelocationRule>()
        .andFinalizeValueOnRead().andDisallowUnsafeRead()

    @get:Input
    @get:Optional
    public val mirrors: SetProperty<Mirror> = project.objects.setProperty<Mirror>()
        .andFinalizeValueOnRead().andDisallowUnsafeRead()

    @get:Input
    @get:Optional
    public val compileTimeResolution: Property<Boolean> = project.objects.property<Boolean>()
        .convention(true).andFinalizeValueOnRead().andDisallowUnsafeRead()

    /**
     * Sets a global repositories that will be used to resolve dependencies,
     * If not set each dependency will attempt to resolve from one of the projects repositories.
     *
     * When set the global repositories will be the only used repositories.
     */
    @get:Input
    @get:Optional
    public val globalRepositories: SetProperty<String> = project.objects.setProperty<String>()
        .andFinalizeValueOnRead().andDisallowUnsafeRead()

    /**
     * Contracts that when building the slimjar, all dependencies must be resolved and there is no ambiguity.
     * If any dependency is not found in the global repository, the build will fail.
     *
     * Defaults to false.
     */
    @get:Input
    @get:Optional
    public val requirePreResolve: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false).andFinalizeValueOnRead().andDisallowUnsafeRead()

    /**
     * Contracts that when building the slimjar, all pre-resolved dependencies must have a valid checksum.
     *
     * Defaults to false.
     */
    @get:Input
    @get:Optional
    public val requireChecksum: Property<Boolean> = project.objects.property<Boolean>()
        .convention(false).andFinalizeValueOnRead().andDisallowUnsafeRead()

    /**
     * @receiver the original path
     * @param target the prefixed path to relocate to.
     */
    @JvmName("relocateInfix")
    public infix fun String.relocate(target: String) {
        addRelocation(this, target)
    }

    public fun relocate(original: String, target: String) {
        addRelocation(original, target)
    }

    private fun addRelocation(
        original: String,
        relocated: String,
        configure: Action<RelocationConfig> = Action { }
    ): SlimJarExtension {
        val relocationConfig = RelocationConfig()
        configure.execute(relocationConfig)
        val rule = RelocationRule(original, relocated, relocationConfig.exclusions, relocationConfig.inclusions)
        relocations.add(rule)
        return this
    }
}
