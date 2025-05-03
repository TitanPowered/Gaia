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
            rename { "META-INF/${it}_${rootProject.name.uppercase()}" }
        }
        dependencies {
            reloc("org.bstats", "bstats")
            reloc("com.seiama.event", "eventbus")
            reloc("org.enginehub.linbus", "linbus")
            reloc("com.typesafe", "typesafe")
            reloc("org.spongepowered.configurate", "configurate")
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


