plugins {
    id("org.spongepowered.gradle.vanilla")
}

minecraft {
    version(libs.versions.minecraft.get())
}

dependencies {
    api(projects.gaiaApi)
    api(libs.tasker.core)
    api(libs.eventbus)
    api(libs.linbus)
    compileOnlyApi(libs.worldedit.core)
    compileOnlyApi(libs.configurate.hocon)
    compileOnlyApi(libs.bundles.cloud)
    compileOnlyApi(libs.gson)
}
