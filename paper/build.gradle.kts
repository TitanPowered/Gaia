plugins {
    id("platform-conventions")
    alias(libs.plugins.runpaper)
    alias(libs.plugins.userdev)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")
    gaiaImplementation(projects.gaiaCommon)
    gaiaImplementation(libs.bstats.bukkit)
    gaiaImplementation(libs.tasker.paper)
    gaiaImplementation(libs.cloud.paper)
    gaiaImplementation(libs.cloud.minecraft) { isTransitive = false }
    compileOnly(libs.worldedit.bukkit)
}

tasks {
    shadowJar {
        dependencies {
            reloc("org.incendo.cloud", "cloud")
        }
    }
    named<Copy>("processResources") {
        filesMatching("*plugin.yml") {
            expand("version" to project.version)
        }
    }
}

gaiaPlatform {
    productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}
