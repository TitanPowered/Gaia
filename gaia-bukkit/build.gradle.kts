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
    implementation("io.papermc", "paperlib", "1.0.5")
    implementation("net.kyori", "adventure-platform-bukkit", "4.0.0-SNAPSHOT")
    implementation("org.bstats", "bstats-bukkit-lite", "1.7")
    compileOnly("org.spigotmc", "spigot-api", "1.16.1-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.0.0")

}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(rootProject.name)
        dependencies {
            relocate("org.bstats", "com.github.primordialmoros.gaia.bstats")
            relocate("io.papermc.lib", "com.github.primordialmoros.gaia.internal.paperlib")
            relocate("com.google.gson", "com.github.primordialmoros.gaia.internal.gson")
            relocate("net.kyori", "com.github.primordialmoros.gaia.internal.kyori")
            relocate("org.checkerframework", "com.github.primordialmoros.gaia.internal.checkerframework")
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
