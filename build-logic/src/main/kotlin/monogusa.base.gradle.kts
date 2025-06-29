plugins {
    id("java-library")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.licenser.spotless")
}

indra {
    gpl3OnlyLicense()

    javaVersions {
        minimumToolchain(21)
        target(21)
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

dependencies {
    checkstyle(libs.checkstyle)
}
