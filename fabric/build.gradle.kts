plugins {
    id("gaia.platform-conventions")
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft(libs.fabric.minecraft)
    implementation(libs.fabric.api)
    implementation(libs.fabric.loader)
    compileOnly(libs.worldedit.coremc)

    implementation(libs.adventure.fabric)
    include(libs.adventure.fabric)
    implementation(libs.cloud.fabric)
    include(libs.cloud.fabric)

    implementation(libs.cloud.minecraft)
    include(libs.cloud.minecraft)
    implementation(libs.bundles.configurate)
    include(libs.bundles.configurate)

    shadow(projects.gaiaCommon)
    shadow(libs.tasker.fabric)
}

loom {
    interfaceInjection.enableDependencyInterfaceInjection
}

tasks {
    shadowJar {
        val processedFabricModJson = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile.absoluteFile
        eachFile {
            if (path == "fabric.mod.json" && file.absoluteFile == processedFabricModJson) {
                exclude()
            }
        }
        from(zipTree(jar.flatMap { it.archiveFile }))
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName = "${project.name}-mc${libs.versions.minecraft.get()}-${project.version}.jar"
    }
    named<Copy>("processResources") {
        expandProperties("fabric.mod.json",
            mapOf("version" to project.version, "mcVersion" to libs.versions.minecraft.get())
        )
    }
}

gaiaPlatform {
    productionJar = tasks.shadowJar.flatMap { it.archiveFile }
}

modrinth {
    versionName = "fabric-$version"
    gameVersions.add(libs.versions.minecraft)
    dependencies {
        required.project("fabric-api")
    }
}
