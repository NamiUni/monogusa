import org.gradle.kotlin.dsl.invoke

plugins {
    id("monogusa.base")
    id("monogusa.platform")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory.paper)
}

dependencies {
    implementation(projects.common)
    compileOnly(libs.paper.api) {
        exclude("net.md-5")
    }

    compileOnly(libs.mini.placeholders)
}

tasks {
    runServer {
        version.set(libs.versions.minecraft)
        systemProperty("log4j.configurationFile", "log4j2.xml")
        downloadPlugins {
            modrinth("luckperms", "v5.5.0-bukkit")
            modrinth("miniplaceholders", "wck4v0R0")
            modrinth("miniplaceholders-placeholderapi-expansion", "1.2.0")
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
}

paperPluginYaml {
    name = "MonogusaPaper"
    loader = "io.github.namiuni.monogusa.example.MonogusaLoader"
    bootstrapper = "io.github.namiuni.monogusa.example.MonogusaBootstrap"
    main = "io.github.namiuni.monogusa.example.MonogusaPaper"
    apiVersion = "1.21"
    author = "Namiu (うにたろう)"
    version = rootProject.version.toString()
}

indraSpotlessLicenser {
    property("name", paperPluginYaml.name)
    property("author", paperPluginYaml.author)
    property("contributors", paperPluginYaml.contributors)
}
