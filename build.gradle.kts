plugins {
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.animalSniffer) apply false
    alias(libs.plugins.kotest.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.arrowGradleConfig.nexus)
}

allprojects {
    group = property("projects.group").toString()
}

tasks {
    val generateDoc by creating(Exec::class) {
        group = "documentation"
        commandLine("sh", "gradlew", "dokkaGfm")
    }
    val runValidation by creating(Exec::class) {
        group = "documentation"
        commandLine("sh", "gradlew", "arrow-ank:runAnk")
    }
    val buildDoc by creating(Exec::class) {
        group = "documentation"
        description = "Generates and validates the documentation"
        dependsOn(generateDoc)
        dependsOn(runValidation)
    }

    runValidation.mustRunAfter(generateDoc)
}

apiValidation {
    ignoredProjects.add("jekyll")
}
