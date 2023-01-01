package dev.racci.slimjar.extension

import io.github.slimjar.relocation.RelocationConfig
import io.github.slimjar.relocation.RelocationRule
import io.github.slimjar.resolver.data.Mirror
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

public abstract class SlimJarExtension {
    @get:Input
    public abstract val relocations: SetProperty<RelocationRule>

    @get:Input
    public abstract val mirrors: SetProperty<Mirror>

    @get:Input
    public abstract val compileTimeResolution: Property<Boolean>

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
