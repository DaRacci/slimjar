package io.github.slimjar.extensions

import org.gradle.api.artifacts.Dependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler

public fun KotlinDependencyHandler.slim(
    dependencyNotation: Any
): Dependency? = with(this as DefaultKotlinDependencyHandler) {
    project.dependencies.add((parent as KotlinSourceSet).slimConfigurationName, dependencyNotation)
}

public fun KotlinDependencyHandler.slimApi(
    dependencyNotation: Any
): Dependency? = with(this as DefaultKotlinDependencyHandler) {
    project.dependencies.add((parent as KotlinSourceSet).slimApiConfigurationName, dependencyNotation)
}
