plugins {
    id("gaia.platform-conventions")
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.hangar)
    alias(libs.plugins.run.paper)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.api)
    gaiaImplementation(projects.gaiaCommon)
    gaiaImplementation(libs.bstats.bukkit)
    gaiaImplementation(libs.tasker.paper)
    gaiaImplementation(libs.cloud.paper)
    gaiaImplementation(libs.cloud.minecraft)
    runtimeDownload(libs.bundles.configurate.loaders)
    compileOnly(libs.worldedit.bukkit)
}

tasks {
    shadowJar {
        dependencies {
            reloc("org.incendo.cloud", "cloud")
        }
    }
    named<Copy>("processResources") {
        filesMatching("paper-plugin.yml") {
            expand(mapOf("version" to project.version, "mcVersion" to libs.versions.minecraft.get()))
        }
    }
}

val generateRuntimeDependencies = tasks.register("writeDependencies", WriteDependencies::class) {
    val runtimeDownloadConfig = configurations.getByName("runtimeDownload")
    tree = runtimeDownloadConfig.incoming.resolutionResult.rootComponent
    files.from(runtimeDownloadConfig)
    outputFileName = "gaia-dependencies"
    outputDir = layout.buildDirectory.dir("generated/dependencies")
}

sourceSets.main {
    resources {
        srcDir(generateRuntimeDependencies)
    }
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

gaiaPlatform {
    productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}

hangarPublish.publications.register("plugin") {
    version = project.version as String
    channel = "Release"
    id = "Gaia"
    changelog = releaseNotes
    apiKey = providers.environmentVariable("HANGAR_TOKEN")
    platforms.paper {
        jar = gaiaPlatform.productionJar
        platformVersions.add(libs.versions.minecraft)
        dependencies.hangar("WorldEdit") { required = false }
    }
}

modrinth {
    versionName = "paper-$version"
    loaders = listOf("paper", "folia")
    gameVersions.add(libs.versions.minecraft)
}
