dependencies {
    implementation(project(":gaia-core"))
    project.project(":gaia-paper:nms").subprojects.forEach {
        implementation(project(it.path, "reobf"))
    }
    implementation(libs.bstats.bukkit)
    implementation(libs.configurate.hocon)
    implementation(libs.cloud.paper)
    implementation(libs.cloud.minecraft) { isTransitive = false }
    compileOnly(libs.paper)
    compileOnly(libs.worldedit.bukkit)
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
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    named<Copy>("processResources") {
        expand("pluginVersion" to project.version)
        from("../LICENSE") {
            rename { "${rootProject.name.uppercase()}_${it}" }
        }
    }
}
