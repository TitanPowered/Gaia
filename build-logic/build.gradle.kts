plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.maven.publish)
    implementation(libs.sponge.vanilla)
    implementation(libs.minotaur)
    implementation(libs.checker)
}

kotlin {
    jvmToolchain(21)
}
