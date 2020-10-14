plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":gaia-core"))
    implementation("org.bstats", "bstats-bukkit-lite", "1.7")
    implementation("net.kyori", "adventure-platform-bukkit", "4.0.0-SNAPSHOT")
    implementation("co.aikar","acf-paper", "0.5.0-SNAPSHOT")
    implementation("io.papermc", "paperlib", "1.0.5")
    compileOnly("org.spigotmc", "spigot-api", "1.16.3-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.0.0")

}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(rootProject.name)
        dependencies {
            relocate("org.bstats", "me.moros.gaia.bstats")
            relocate("net.kyori", "me.moros.gaia.internal.kyori")
            relocate("org.checkerframework", "me.moros.gaia.internal.checkerframework")
            relocate("io.papermc.lib", "me.moros.gaia.internal.paperlib")
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
