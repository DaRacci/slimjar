package dev.racci.slimjar.extensions

import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.capabilities.Capability
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.accessors.runtime.addConfiguredDependencyTo
import org.gradle.kotlin.dsl.accessors.runtime.addExternalModuleDependencyTo
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmModule
import kotlin.reflect.full.primaryConstructor

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slim(dependencyNotation: Any): Dependency? = addDependencyByAnyNotation(sourceSet.slimConfigurationName, dependencyNotation)

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slim(
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): ExternalModuleDependency = addDependencyByStringNotation(sourceSet.slimConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slim(
    dependencyNotation: Provider<*>,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): Unit = addConfiguredDependencyTo(project.dependencies, sourceSet.slimConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slim(
    dependencyNotation: ProviderConvertible<*>,
    dependencyConfiguration: Action<ExternalModuleDependency>
): Unit = addConfiguredDependencyTo(project.dependencies, sourceSet.slimConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param group the group of the module to be added as a dependency.
 * @param name the name of the module to be added as a dependency.
 * @param version the optional version of the module to be added as a
 *     dependency.
 * @param configuration the optional configuration of the module to be
 *     added as a dependency.
 * @param classifier the optional classifier of the module artifact to be
 *     added as a dependency.
 * @param ext the optional extension of the module artifact to be added as
 *     a dependency.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slim(
    group: String,
    name: String,
    version: String? = null,
    configuration: String? = null,
    classifier: String? = null,
    ext: String? = null,
    dependencyConfiguration: Action<ExternalModuleDependency>? = null
): ExternalModuleDependency = addExternalModuleDependencyTo(project.dependencies, sourceSet.slimConfigurationName, group, name, version, configuration, classifier, ext, dependencyConfiguration)

/**
 * Adds a dependency to the 'slim' configuration.
 *
 * @param dependency dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun <T : ModuleDependency> KotlinDependencyHandler.slim(
    dependency: T,
    dependencyConfiguration: T.() -> Unit
): T = addDependency(sourceSet.slimConfigurationName, dependency, dependencyConfiguration)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slimApi(dependencyNotation: Any): Dependency? = addDependencyByAnyNotation(sourceSet.slimApiConfigurationName, dependencyNotation)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slimApi(
    dependencyNotation: String,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): ExternalModuleDependency = addDependencyByStringNotation(sourceSet.slimApiConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slimApi(
    dependencyNotation: Provider<*>,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit
): Unit = addConfiguredDependencyTo(project.dependencies, sourceSet.slimApiConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param dependencyNotation notation for the dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slimApi(
    dependencyNotation: ProviderConvertible<*>,
    dependencyConfiguration: Action<ExternalModuleDependency>
): Unit = addConfiguredDependencyTo(project.dependencies, sourceSet.slimApiConfigurationName, dependencyNotation, dependencyConfiguration)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param group the group of the module to be added as a dependency.
 * @param name the name of the module to be added as a dependency.
 * @param version the optional version of the module to be added as a
 *     dependency.
 * @param configuration the optional configuration of the module to be
 *     added as a dependency.
 * @param classifier the optional classifier of the module artifact to be
 *     added as a dependency.
 * @param ext the optional extension of the module artifact to be added as
 *     a dependency.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun KotlinDependencyHandler.slimApi(
    group: String,
    name: String,
    version: String? = null,
    configuration: String? = null,
    classifier: String? = null,
    ext: String? = null,
    dependencyConfiguration: Action<ExternalModuleDependency>? = null
): ExternalModuleDependency = addExternalModuleDependencyTo(project.dependencies, sourceSet.slimApiConfigurationName, group, name, version, configuration, classifier, ext, dependencyConfiguration)

/**
 * Adds a dependency to the 'slimApi' configuration.
 *
 * @param dependency dependency to be added.
 * @param dependencyConfiguration expression to use to configure the
 *     dependency.
 * @return The dependency.
 */
public fun <T : ModuleDependency> KotlinDependencyHandler.slimApi(
    dependency: T,
    dependencyConfiguration: T.() -> Unit
): T = addDependency(sourceSet.slimApiConfigurationName, dependency, dependencyConfiguration)

private fun KotlinDependencyHandler.addDependencyByAnyNotation(
    configurationName: String,
    dependencyNotation: Any
): Dependency? {
    val dependency = when (dependencyNotation) {
        is GradleKpmModule -> project.dependencies.create(dependencyNotation.project).apply {
            (this as ModuleDependency).capabilities {
                if (dependencyNotation.moduleClassifier != null) {
                    val capability = Class.forName("org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util").kotlin.primaryConstructor!!.call(
                        project.provider { project.group.toString() },
                        project.name,
                        project.provider { project.version.toString() },
                        dependencyNotation.moduleClassifier
                    ) as Capability
                    requireCapability(capability)
                }
            }
        }

        else -> dependencyNotation
    }

    return project.dependencies.add(configurationName, dependency)
}

private fun KotlinDependencyHandler.addDependencyByStringNotation(
    configurationName: String,
    dependencyNotation: Any,
    configure: ExternalModuleDependency.() -> Unit = { }
): ExternalModuleDependency = addDependency(configurationName, project.dependencies.create(dependencyNotation) as ExternalModuleDependency, configure)

private fun <T : Dependency> KotlinDependencyHandler.addDependency(
    configurationName: String,
    dependency: T,
    configure: T.() -> Unit
): T = dependency.also {
    configure(it)
    project.dependencies.add(configurationName, it)
}

private val KotlinDependencyHandler.sourceSet: KotlinSourceSet
    get() = (this as DefaultKotlinDependencyHandler).parent as KotlinSourceSet
