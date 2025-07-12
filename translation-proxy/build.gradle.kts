plugins {
    id("monogusa.base")
}

val projectVersion: String by project
version = projectVersion

dependencies {
    api(projects.translationAnnotation)
    // Adventure
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.minimessage)

    // Google guava
    api(libs.google.guava)
}
