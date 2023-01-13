plugins {
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
}

repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val shadowImplementation: Configuration by configurations.creating
val compileAndTest: Configuration by configurations.creating
configurations {
    compileAndTest.extendsFrom(shadowImplementation)
    compileOnly.get().extendsFrom(compileAndTest)
    testImplementation.get().extendsFrom(compileAndTest)
}

@Suppress("UnstableApiUsage")
dependencies {
    shadowImplementation(project(":slimjar"))
    shadowImplementation("com.google.code.gson:gson:2.10")
    shadowImplementation(libs.kotlinx.coroutines)

    compileAndTest(gradleApi())
    compileAndTest(gradleKotlinDsl())
    compileAndTest(libs.gradle.shadow)
    compileAndTest(libs.gradle.minecraft.paperweight)
    compileAndTest(libs.gradle.kotlin.jvm)
    compileAndTest(libs.gradle.kotlin.mpp)
    compileAndTest("dev.racci.minix:dev.racci.minix.gradle.plugin:0.5.0")

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
        group = "verification"
        dependsOn(shadowJar)

        doLast {
            val nonInlinedDependencies = mutableListOf<String>()
            zipTree(shadowJar.flatMap { it.archiveFile }).visit {
                if (isDirectory) return@visit

                val path = relativePath
                if (
                    !path.startsWith("META-INF") &&
                    path.lastName.endsWith(".class") &&
                    !path.pathString.startsWith("io/github/slimjar") &&
                    !path.pathString.startsWith("dev/racci/slimjar")
                ) nonInlinedDependencies.add(path.pathString)
            }

            if (nonInlinedDependencies.isEmpty()) return@doLast
            throw GradleException("Found non inlined dependencies: $nonInlinedDependencies")
        }
    }

    // Disabling default jar task as it is overridden by shadowJar
    jar { enabled = false }

    test { useJUnitPlatform() }

    check { dependsOn(ensureDependenciesAreInlined, validatePlugins) }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowImplementation)

        exclude("kotlin/**")

        listOf(
            "me.lucko.jarrelocator",
            "com.google.gson",
            "arrow",
            "kotlinx",
            "org.intellij",
            "org.jetbrains.annotations",
            "org.codehaus.mojo.animal_sniffer"
        ).map { it to it.split('.').last() }.forEach { (original, last) ->
            relocate(original, "dev.racci.slimjar.libs.$last")
        }
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

    testSourceSets(sourceSets.test.get())
}

pluginBundle {
    website = "https://github.com/DaRacci/slimjar"
    vcsUrl = "https://github.com/DaRacci/slimjar"
    tags = listOf("runtime dependency", "relocation")
    description = "Very easy to setup and downloads any public dependency at runtime!"
}
