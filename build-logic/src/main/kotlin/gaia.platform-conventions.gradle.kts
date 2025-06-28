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
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        from("$rootDir/LICENSE") {
            rename { "META-INF/${it}_${rootProject.name.uppercase()}" }
        }
        dependencies {
            reloc("org.bstats", "bstats")
            reloc("com.seiama.event", "eventbus")
            reloc("org.enginehub.linbus", "linbus")
            exclude { it.moduleName.contains("geantyref") }
        }
    }
    val copyJar = register("copyJar", CopyFile::class) {
        fileToCopy.set(platformExt.productionJar)
        destination.set(platformExt.productionJar.flatMap { rootProject.layout.buildDirectory.file(it.asFile.name) })
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

