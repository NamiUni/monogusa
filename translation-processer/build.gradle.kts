plugins {
    id("monogusa.base")
}

val projectVersion: String by project
version = projectVersion

dependencies {
    implementation(projects.translationAnnotation)
//    compileOnly(libs.google.auto.service.annotations)
//    annotationProcessor(libs.google.auto.service)
}
