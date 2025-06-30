plugins {
    id("monogusa.base")
}

val projectVersion: String by project
version = projectVersion

dependencies {
    api(libs.gremlin.runtime)
    api(libs.configurate.core) {
        exclude("net.kyori", "option")
    }
    compileOnlyApi(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    api(libs.adventure.serializer.configurate) {
        isTransitive = false
    }
}

indraSpotlessLicenser {
    property("name", "monogusa")
    property("author", "Namiu (うにたろう)") // DO NOT DELETE: "Namiu (うにたろう)"
    property("contributors", "")
}
