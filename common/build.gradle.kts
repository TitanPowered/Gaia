plugins {
    id("org.spongepowered.gradle.vanilla")
}

minecraft {
    version(libs.versions.minecraft.get())
}

dependencies {
    api(projects.gaiaApi)
    implementation(libs.eventbus)
    implementation(libs.linbus)
    implementation(libs.configurate.hocon)
    compileOnlyApi(libs.bundles.cloud)
    compileOnly(libs.worldedit.core)
}
