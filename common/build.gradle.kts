plugins {
    id("gaia.base-conventions")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = libs.versions.neoform.get()
}

dependencies {
    api(projects.gaiaApi)
    api(libs.tasker.core)
    implementation(libs.eventbus)
    implementation(libs.linbus)
    compileOnly(libs.bundles.configurate)
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.worldedit.core) {
        // TODO remove exclusions when worldedit updates constraints
        exclude(module = "gson")
        exclude(module = "guava")
        exclude(module = "fastutil")
    }
}
