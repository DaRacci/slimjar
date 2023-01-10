enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "slimjar"

include("slimjar", "gradle-plugin", "loader-agent")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("plugin.sam.with.receiver") version kotlinVersion
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.racci.dev/releases") {
            mavenContent {
                releasesOnly()
                includeModule("dev.racci", "catalog")
            }
        }
    }

    versionCatalogs.create("libs") {
        val kotlinVersion: String by settings
        val build: String by settings
        val conventions = kotlinVersion.plus("-").plus(build)
        from("dev.racci:catalog:$conventions")
    }
}
