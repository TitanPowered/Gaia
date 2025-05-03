import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project

fun ShadowJar.reloc(pkg: String, name: String) {
    relocate(pkg, "gaia.libraries.$name")
}

fun Project.isSnapshot() = project.version.toString().endsWith("-SNAPSHOT")

fun Project.apiVersion(): String = project.version.toString().replace(Regex("""\b\d+(?=[^.]*$)"""), "0")
