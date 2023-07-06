plugins {
    id("platform-conventions")
    alias(libs.plugins.runpaper)
    alias(libs.plugins.userdev)
}

dependencies {
    paperweight.paperDevBundle("${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")
    gaiaImplementation(projects.gaiaCommon)
    gaiaImplementation(libs.bstats.bukkit)
    gaiaImplementation(libs.tasker.bukkit)
    gaiaImplementation(libs.cloud.paper)
    gaiaImplementation(libs.cloud.minecraft) { isTransitive = false }
    compileOnly(libs.worldedit.bukkit)
}

tasks {
    shadowJar {
        dependencies {
            reloc("io.leangen", "leangen")
        }
    }
    named<Copy>("processResources") {
        filesMatching("*plugin.yml") {
            expand("pluginVersion" to project.version)
        }
    }
}

gaiaPlatform {
    productionJar.set(tasks.reobfJar.flatMap { it.outputJar })
}
