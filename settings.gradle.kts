@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "monogusa-template"

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

include("example-paper")
include("monogusa-common")
