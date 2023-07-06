enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "gaia"

setupSubproject("api")
setupSubproject("common")
setupSubproject("paper")
setupSubproject("fabric")

fun setupSubproject(moduleName: String) {
    val name = "gaia-$moduleName"
    include(name)
    project(":$name").projectDir = file(moduleName)
}
