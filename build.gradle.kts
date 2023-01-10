import com.hierynomus.gradle.license.LicenseBasePlugin

plugins {
    alias(libs.plugins.minix)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.license)
}

minix.publishing {
    create("gradle-plugin")
    create("slimjar")
}

subprojects {
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
