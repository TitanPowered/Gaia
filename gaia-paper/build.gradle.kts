plugins {
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

dependencies {
    implementation(project(":gaia-core"))
    implementation("org.bstats", "bstats-bukkit", "2.2.1")
    implementation("org.spongepowered", "configurate-hocon", "4.1.2")
    implementation("cloud.commandframework","cloud-paper", "1.7.1")
    implementation("cloud.commandframework","cloud-minecraft-extras", "1.7.1") {
        exclude(group = "net.kyori")
    }
    compileOnly("io.papermc.paper", "paper-api", "1.18.2-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.2.6")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(rootProject.name)
        destinationDirectory.set(rootProject.buildDir)
        dependencies {
            relocate("org.bstats", "me.moros.gaia.bstats")
            relocate("cloud.commandframework", "me.moros.gaia.internal.cf")
            relocate("io.leangen", "me.moros.gaia.internal.leangen")
            relocate("com.typesafe", "me.moros.gaia.internal.typesafe")
            relocate("org.spongepowered.configurate", "me.moros.gaia.internal.configurate")
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
        from("../LICENSE") {
            rename { "${rootProject.name.toUpperCase()}_${it}"}
        }
    }
}
