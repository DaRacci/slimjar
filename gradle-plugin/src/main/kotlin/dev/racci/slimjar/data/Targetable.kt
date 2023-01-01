package dev.racci.slimjar.data

import org.gradle.api.Named
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

@JvmInline
public value class Targetable(private val mainString: String) {
    public companion object {
        @PublishedApi
        internal fun prefixable(
            named: String?,
            mainName: String? = null,
            string: String
        ): String = if (named == null || named == (mainName ?: "")) {
            string
        } else prefixed(named, string)

        @PublishedApi
        internal fun prefixable(
            named: Named?,
            mainName: String? = null,
            string: String
        ): String = prefixable(named?.name, mainName, string)

        @PublishedApi
        internal fun prefixed(
            named: String,
            string: String
        ): String = "${named.replaceFirstChar(Char::lowercaseChar)}${string.capitalized()}"
    }

    public fun forSourceSet(sourceSet: KotlinSourceSet): String = prefixed(sourceSet.name, mainString)

    public fun forTarget(target: KotlinTarget): String = prefixed(target.name, mainString)

    public fun forNamed(named: Named?): String = prefixable(named?.name, SourceSet.MAIN_SOURCE_SET_NAME, mainString)

    public fun get(): String = mainString
}
