plugins {
    id("gaia.base-conventions")
    id("org.spongepowered.gradle.vanilla")
}

minecraft {
    version(libs.versions.minecraft.get())
    platform(org.spongepowered.gradle.vanilla.repository.MinecraftPlatform.SERVER)
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
