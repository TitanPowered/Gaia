plugins {
    id("base-conventions")
    `maven-publish`
    signing
}

java {
    if (!isSnapshot()) { withJavadocJar() }
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Gaia api for Minecraft")
                url.set("https://github.com/PrimordialMoros/Gaia")
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set("The GNU General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                        distribution.set("repo")
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
    }
    repositories {
        val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        maven {
            name = "sonatype"
            credentials(PasswordCredentials::class)
            url = if (isSnapshot()) snapshotUrl else releaseUrl
        }
    }
}

signing {
    setRequired { !isSnapshot() }
    sign(publishing.publications["maven"])
}
