plugins {
    `maven-publish`
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
    compileOnly("org.spongepowered", "configurate-hocon", "4.1.2")
    compileOnly("org.slf4j", "slf4j-api", "1.7.36")
    compileOnly("cloud.commandframework", "cloud-core", "1.7.1")
    compileOnly("net.kyori", "adventure-api", "4.11.0")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.6")
    compileOnly("com.google.code.gson", "gson", "2.9.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        pom {
            name.set(project.name)
            description.set("Arena management plugin")
            url.set("https://github.com/PrimordialMoros/Gaia")
            licenses {
                license {
                    name.set("The GNU General Public License, Version 3.0")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                }
            }
            developers {
                developer {
                    id.set("moros")
                    name.set("Moros")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/PrimordialMoros/Gaia.git")
                developerConnection.set("scm:git:ssh://git@github.com/PrimordialMoros/Gaia.git")
                url.set("https://github.com/PrimordialMoros/Gaia")
            }
            issueManagement {
                system.set("Github")
                url.set("https://github.com/PrimordialMoros/Gaia/issues")
            }
        }
    }
    if (project.hasProperty("ossrhUsername") && project.hasProperty("ossrhPassword")) {
        val user = project.property("ossrhUsername") as String?
        val pass = project.property("ossrhPassword") as String?
        repositories {
            maven {
                credentials { username = user; password = pass }
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            }
        }
    }
}
