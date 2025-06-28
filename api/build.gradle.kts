plugins {
    id("gaia.publish-conventions")
}

dependencies {
    api(libs.math.core)
    compileOnlyApi(libs.adventure)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.platform)
}

tasks {
    register("printVersionStatus") {
        doLast {
            println("STATUS=${if (isSnapshot()) "snapshot" else "release"}")
        }
    }
    test {
        useJUnitPlatform()
    }
}
