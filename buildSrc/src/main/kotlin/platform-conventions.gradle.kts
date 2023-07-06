plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
}

val platformExt = extensions.create("gaiaPlatform", GaiaPlatformExtension::class)

configurations.create("gaiaImplementation")
configurations.implementation {
    extendsFrom(configurations.getByName("gaiaImplementation"))
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.getByName("gaiaImplementation"))
        exclude("org/checkerframework/") // Try to catch the myriad dependency leaks
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        from("$rootDir/LICENSE") {
            rename { "${rootProject.name.uppercase()}_${it}" }
        }
        dependencies {
            reloc("org.bstats", "bstats")
            reloc("net.kyori.event", "eventbus")
            reloc("org.enginehub.linbus", "linbus")
            reloc("cloud.commandframework", "cloudframework")
            reloc("com.typesafe", "typesafe")
            reloc("org.spongepowered.configurate", "configurate")
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


