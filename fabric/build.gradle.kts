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
    compileOnly(libs.worldedit.fabric)

    implementation(libs.adventure.fabric)
    include(libs.adventure.fabric)
    implementation(libs.cloud.fabric)
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
    shadowJar {
        archiveFileName = "${project.name}-mc${libs.versions.minecraft.get()}-${project.version}.jar"
    }
}

gaiaPlatform {
    productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}

modrinth {
    versionName = "fabric-$version"
    gameVersions.add(libs.versions.minecraft)
    dependencies {
        required.project("fabric-api")
    }
}
