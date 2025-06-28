plugins {
    id("gaia.base-conventions")
    alias(libs.plugins.jmh)
}

dependencies {
    implementation(projects.gaiaCommon)
    implementation(libs.fastutil)
}
