import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.the
import xyz.jpenilla.gremlin.gradle.ShadowGremlin

val Project.libs: LibrariesForLibs
    get() = the()

fun Task.relocateDependency(pkg: String) {
    ShadowGremlin.relocate(this, pkg, "${project.name}.libs.$pkg")
}
