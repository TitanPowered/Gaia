plugins {
    id("base-conventions")
}

val platformExt = extensions.create("gaiaPlatform", GaiaPlatformExtension::class)

configurations.create("gaiaImplementation")
configurations.implementation {
    extendsFrom(configurations.getByName("gaiaImplementation"))
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.getByName("gaiaImplementation"))
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        from("$rootDir/LICENSE") {
            rename { "${rootProject.name.uppercase()}_${it}" }
        }
        val excluded = setOf("checker-qual", "error_prone_annotations", "geantyref", "slf4j-api")
        dependencies {
            reloc("org.bstats", "bstats")
            reloc("net.kyori.event", "eventbus")
            reloc("org.enginehub.linbus", "linbus")
            reloc("cloud.commandframework", "cloudframework")
            reloc("com.typesafe", "typesafe")
            reloc("org.spongepowered.configurate", "configurate")
            exclude {
                excluded.contains(it.moduleName)
            }
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


