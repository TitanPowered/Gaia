plugins {
    alias(libs.plugins.jmh)
}

dependencies {
    implementation(projects.gaiaCommon)
    implementation("it.unimi.dsi:fastutil-core:8.5.12")
}
