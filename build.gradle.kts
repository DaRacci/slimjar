import com.hierynomus.gradle.license.LicenseBasePlugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.racci.minix") version "0.1.4-SNAPSHOT"
    alias(libs.plugins.minix.publication)
    alias(libs.plugins.gradle.license)
}

allprojects {
    apply<JavaLibraryPlugin>()
    apply<LicenseBasePlugin>()

    license {
        header = rootProject.file("LICENSE")
        includes(listOf("**/*.java', '**/*.kt"))
        mapping("kt", "DOUBLESLASH_STYLE")
        mapping("java", "DOUBLESLASH_STYLE")
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    }
}
