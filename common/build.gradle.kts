plugins {
    id("gaia.base-conventions")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = libs.versions.neoform.get()
}

repositories {
    maven("https://repo.moros.me/snapshots/") {
        mavenContent { includeGroup("org.incendo") }
    }
}

dependencies {
    api(projects.gaiaApi)
    api(libs.tasker.core)
    implementation(libs.eventbus)
    implementation(libs.linbus)
    compileOnly(libs.bundles.configurate)
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.worldedit.core)
}
