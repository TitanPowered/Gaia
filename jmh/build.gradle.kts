plugins {
    id("base-conventions")
    alias(libs.plugins.jmh)
}

dependencies {
    implementation(projects.gaiaCommon)
    implementation(libs.fastutil)
}
