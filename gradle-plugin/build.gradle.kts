import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.jetbrains.kotlin.util.prefixIfNot

plugins {
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
    id(libs.plugins.kotlin.plugin.serialization.get().pluginId)
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val shadowImplementation: Configuration by configurations.creating
val compileAndTest: Configuration by configurations.creating
configurations {
    compileOnly.get().extendsFrom(shadowImplementation, compileAndTest)
    testImplementation.get().extendsFrom(shadowImplementation, compileAndTest)
}

@Suppress("UnstableApiUsage")
dependencies {
    shadowImplementation(libs.kotlin.stdlib)
    shadowImplementation(project(":slimjar"))
    shadowImplementation(libs.arrow.core)
    shadowImplementation(libs.kotlinx.coroutines)
    shadowImplementation(libs.kotlinx.serialization.json)
    shadowImplementation(libs.kotlinx.immutableCollections)

    compileAndTest(gradleApi())
    compileAndTest(gradleKotlinDsl())
    compileAndTest(libs.gradle.shadow)
    compileAndTest(libs.gradle.kotlin.jvm)
    compileAndTest(libs.gradle.kotlin.mpp)
    compileAndTest(libs.gradle.kotlin.dsl)

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation(gradleTestKit())

    // For grade log4j checker.
    configurations.configureEach {
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
    }
}

tasks {
    val ensureDependenciesAreInlined by registering {
        description = "Ensures all declared dependencies are inlined into shadowed jar"
        group = HelpTasksPlugin.HELP_GROUP
        dependsOn(shadowJar)

        doLast {
            val nonInlinedDependencies = mutableListOf<String>()
            zipTree(shadowJar.flatMap { it.archiveFile }).visit {
                if (!isDirectory) return@visit

                val path = relativePath
                if (
                    !path.startsWith("META-INF") &&
                    path.lastName.endsWith(".class") &&
                    !path.pathString.startsWith("io.github.slimjar".replace(".", "/"))
                ) nonInlinedDependencies.add(path.pathString)
            }

            if (nonInlinedDependencies.isEmpty()) return@doLast
            throw GradleException("Found non inlined dependencies: $nonInlinedDependencies")
        }
    }

    val relocateShadowJar by registering(ConfigureShadowRelocation::class) {
        target = shadowJar.get()
    }

    // Disabling default jar task as it is overridden by shadowJar
    jar { enabled = false }

    test { useJUnitPlatform() }

    check { dependsOn(ensureDependenciesAreInlined, validatePlugins) }

    shadowJar {
        dependsOn(relocateShadowJar)
        archiveClassifier.set("")
        configurations = listOf(shadowImplementation)

        mapOf(
            "io.github.slimjar" to null,
            "me.lucko.jarrelocator" to "jarrelocator",
            "com.google.gson" to "gson",
            "kotlin" to "kotlin",
            "org.intellij" to "intellij",
            "org.jetbrains" to "jetbrains"
        ).forEach { relocate(it.key, "io.github.slimjar${it.value?.prefixIfNot(".") ?: ""}") }
    }

    whenTaskAdded {
        if (name != "publishPluginJar" && name != "generateMetadataFileForPluginMavenPublication") return@whenTaskAdded
        dependsOn(shadowJar)
    }

    withType<GenerateModuleMetadata> { enabled = false }
}

// Required for plugin substitution to work in sample projects.
artifacts {
    add("runtimeOnly", tasks.shadowJar)
}

// Work around publishing shadow jars
afterEvaluate {
    publishing.publications
        .withType<MavenPublication>()
        .filter { it.name == "pluginMaven" }
        .forEach { publication -> publication.setArtifacts(listOf(tasks.shadowJar)) }
}

gradlePlugin {
    plugins {
        create("slimjar") {
            id = group.toString()
            displayName = "SlimJar"
            description = "JVM Runtime Dependency Management."
            implementationClass = "io.github.slimjar.SlimJarPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/DaRacci/slimjar"
    vcsUrl = "https://github.com/DaRacci/slimjar"
    tags = listOf("runtime dependency", "relocation")
    description = "Very easy to setup and downloads any public dependency at runtime!"
}
