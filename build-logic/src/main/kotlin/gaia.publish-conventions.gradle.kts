import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("gaia.base-conventions")
    id("com.vanniktech.maven.publish")
    signing
}

mavenPublishing {
    pom {
        name = project.name
        description = "Gaia api for Minecraft"
        url = "https://github.com/PrimordialMoros/Gaia"
        inceptionYear = "2020"
        licenses {
            license {
                name = "The GNU General Public License, Version 3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "moros"
                name = "Moros"
                url = "https://github.com/PrimordialMoros"
            }
        }
        scm {
            connection = "scm:git:https://github.com/PrimordialMoros/Gaia.git"
            developerConnection = "scm:git:ssh://git@github.com/PrimordialMoros/Gaia.git"
            url = "https://github.com/PrimordialMoros/Gaia"
        }
        issueManagement {
            system = "Github"
            url = "https://github.com/PrimordialMoros/Gaia/issues"
        }
    }
    configure(JavaLibrary(JavadocJar.Javadoc(), SourcesJar.Sources()))
    publishToMavenCentral()
    signAllPublications()
}
