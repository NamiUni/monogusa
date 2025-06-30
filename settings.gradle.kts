@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "monogusa"

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

include("monogusa-example-paper")
include("monogusa")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
