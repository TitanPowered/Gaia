plugins {
    id("platform-conventions")
    alias(libs.plugins.fabric.loom)
}

dependencies {
    minecraft(libs.fabric.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.loader)
    modCompileOnly(libs.worldedit.fabric)
    modImplementation(include(libs.adventure.fabric.get())!!)
    modImplementation(include(libs.cloud.fabric.get())!!)
    implementation(include(libs.cloud.minecraft.get())!!)
    gaiaImplementation(projects.gaiaCommon)
    gaiaImplementation(libs.tasker.fabric)
}

loom {
    interfaceInjection.enableDependencyInterfaceInjection
}

tasks {
    named<Copy>("processResources") {
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version, "mcVersion" to libs.versions.minecraft.get()))
        }
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
