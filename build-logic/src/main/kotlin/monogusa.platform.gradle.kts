plugins {
    id("java")
    id("xyz.jpenilla.gremlin-gradle")
    id("com.gradleup.shadow")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveVersion = rootProject.version.toString()
        archiveClassifier = null as String?

        relocateDependency("net.kyori.adventure.serializer.configurate4")
        relocateDependency("xyz.jpenilla.gremlin")
    }

    writeDependencies {
        repos.add("https://repo.papermc.io/repository/maven-public/")
        repos.add("https://repo.maven.apache.org/maven2/")
    }
}

gremlin {
    defaultJarRelocatorDependencies = false
    defaultGremlinRuntimeDependency = false
}
