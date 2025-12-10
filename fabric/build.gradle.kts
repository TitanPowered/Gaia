plugins {
    id("gaia.platform-conventions")
    alias(libs.plugins.fabric.loom)
}

repositories {
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft(libs.fabric.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.loader)
    modCompileOnly(libs.worldedit.fabric) {
        // TODO remove exclusions when worldedit updates constraints
        exclude(module = "gson")
        exclude(module = "guava")
        exclude(module = "fastutil")
    }

    modImplementation(libs.adventure.fabric)
    include(libs.adventure.fabric)
    modImplementation(libs.cloud.fabric)
    include(libs.cloud.fabric)

    implementation(libs.cloud.minecraft)
    include(libs.cloud.minecraft)
    implementation(libs.bundles.configurate)
    include(libs.bundles.configurate)

    gaiaImplementation(projects.gaiaCommon)
    gaiaImplementation(libs.tasker.fabric)
}

loom {
    interfaceInjection.enableDependencyInterfaceInjection
}

tasks {
    named<Copy>("processResources") {
        expandProperties("fabric.mod.json",
            mapOf("version" to project.version, "mcVersion" to libs.versions.minecraft.get())
        )
    }
    remapJar {
        val shadowJar = getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar")
        dependsOn(shadowJar)
        inputFile.set(shadowJar.archiveFile)
        addNestedDependencies.set(true)
        archiveFileName.set("${project.name}-mc${libs.versions.minecraft.get()}-${project.version}.jar")
    }
}

gaiaPlatform {
    productionJar.set(tasks.remapJar.flatMap { it.archiveFile })
}

modrinth {
    versionName = "fabric-$version"
    gameVersions.add(libs.versions.minecraft)
    dependencies {
        required.project("fabric-api")
    }
}
