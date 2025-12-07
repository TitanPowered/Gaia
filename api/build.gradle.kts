plugins {
    id("gaia.publish-conventions")
}

version = apiVersion()

dependencies {
    api(libs.math.core)
    compileOnlyApi(libs.adventure)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
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
