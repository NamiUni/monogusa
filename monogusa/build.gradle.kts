plugins {
    id("monogusa.base")
}

val projectVersion: String by project
version = projectVersion

dependencies {
    // Gremlin
    api(libs.gremlin.runtime)

    // Configurate
    compileOnly(libs.configurate.core) {
        exclude("net.kyori", "option")
    }

    // Adventure
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    api(libs.adventure.serializer.configurate) {
        isTransitive = false
    }
}

indraSpotlessLicenser {
    property("name", "monogusa")
    property("author", "Namiu (うにたろう)")
    property("contributors", "")
}
