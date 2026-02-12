plugins {
    id("gaia.base-conventions")
    id("com.modrinth.minotaur")
}

val platformExt = extensions.create("gaiaPlatform", GaiaPlatformExtension::class)

configurations.create("gaiaImplementation")
configurations.implementation {
    extendsFrom(configurations.getByName("gaiaImplementation"))
}

val runtimeDownload: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.getByName("gaiaImplementation"))
        archiveClassifier = ""
        mergeServiceFiles()
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        val licenseName = "LICENSE_${rootProject.name.uppercase()}"
        from("$rootDir/LICENSE") {
            into("META-INF")
            rename { licenseName }
        }
        dependencies {
            reloc("org.bstats", "bstats")
            reloc("com.sasorio.event", "eventbus")
            reloc("org.enginehub.linbus", "linbus")
            exclude { it.moduleName.contains("geantyref") }
        }
    }
    val copyJar = register<CopyFile>("copyJar") {
        fileToCopy = platformExt.productionJar
        destination = platformExt.productionJar.flatMap { rootProject.layout.buildDirectory.file(it.asFile.name) }
        dependsOn(jar)
    }
    assemble {
        dependsOn(copyJar)
    }
}

modrinth {
    projectId = "cbNKlKAw"
    versionType = "release"
    file = platformExt.productionJar
    changelog = releaseNotes
    token = providers.environmentVariable("MODRINTH_TOKEN")
    dependencies {
        optional.project("worldedit")
    }
}

