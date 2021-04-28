plugins {
    java
    id("com.github.johnrengelman.shadow").version("7.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":gaia-core"))
    implementation("org.bstats", "bstats-bukkit", "2.2.1")
    implementation("co.aikar", "acf-paper", "0.5.0-SNAPSHOT")
    compileOnly("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.0.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set(rootProject.name)
        dependencies {
            relocate("org.bstats", "me.moros.gaia.bstats")
            relocate("org.checkerframework", "me.moros.gaia.internal.checkerframework")
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
