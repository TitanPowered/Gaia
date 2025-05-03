plugins {
    id("publish-conventions")
}

dependencies {
    api(libs.math.core)
    compileOnlyApi(libs.adventure)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.platform)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
