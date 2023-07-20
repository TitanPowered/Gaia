plugins {
    id("publish-conventions")
}

dependencies {
    testImplementation(libs.bundles.junit)
    api(libs.math.core)
    compileOnlyApi(libs.adventure)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
