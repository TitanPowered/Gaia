plugins {
    `maven-publish`
}

dependencies {
    testImplementation(libs.bundles.junit)
    compileOnly(libs.configurate.hocon)
    compileOnly(libs.slf4j.api)
    compileOnly(libs.gson)
    compileOnly(libs.adventure)
    compileOnly(libs.worldedit.core)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.minecraft)
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
