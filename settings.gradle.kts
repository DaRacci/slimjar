enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "slimjar"

include("slimjar", "gradle-plugin", "loader-agent")

pluginManagement {
    repositories {
        mavenLocal { mavenContent { snapshotsOnly() } }
        mavenCentral()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }
    }

    plugins {
        kotlin("plugin.sam.with.receiver") version "1.7.22"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }
    }

    versionCatalogs.create("libs") {
        val kotlinVersion: String by settings
        val build: String by settings
        val conventions = kotlinVersion.plus("-").plus(build)
        from("dev.racci:catalog:$conventions")
    }
}
