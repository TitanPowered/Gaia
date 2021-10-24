plugins {
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

dependencies {
    implementation(project(":gaia-core"))
    implementation("org.bstats", "bstats-bukkit", "2.2.1")
    implementation("co.aikar", "acf-paper", "0.5.0-SNAPSHOT")
    compileOnly("io.papermc.paper", "paper-api", "1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.2.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(rootProject.name)
        dependencies {
            relocate("org.bstats", "me.moros.gaia.bstats")
            relocate("co.aikar.commands", "me.moros.gaia.internal.acf")
            relocate("co.aikar.locales", "me.moros.gaia.internal.locales")
        }
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    named<Copy>("processResources") {
        expand("pluginVersion" to project.version)
    }
}
