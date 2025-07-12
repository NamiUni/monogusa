plugins {
    id("monogusa.base")
}

val projectVersion: String by project
version = projectVersion

dependencies {
    // Gremlin
    api(libs.gremlin.runtime)

    // Configurate
    compileOnlyApi(libs.configurate.core) {
        exclude("net.kyori", "option")
    }

    // Adventure
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.minimessage)
    api(libs.adventure.serializer.configurate) {
        isTransitive = false
    }

    // Google guava
    api(libs.google.guava)
}

indraSpotlessLicenser {
    property("name", "monogusa")
    property("author", "Namiu (うにたろう)")
    property("contributors", "")
}
