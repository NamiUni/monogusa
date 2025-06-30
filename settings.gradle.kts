@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "monogusa-parent"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Paper
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

pluginManagement {
    includeBuild("build-logic")
}

include("monogusa")
include("monogusa-example-paper")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
