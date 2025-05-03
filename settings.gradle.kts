enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
    includeBuild("build-logic")
}

rootProject.name = "gaia"

include("jmh")
setupSubproject("api")
setupSubproject("common")
setupSubproject("paper")
setupSubproject("fabric")

fun setupSubproject(moduleName: String) {
    val name = "gaia-$moduleName"
    include(name)
    project(":$name").projectDir = file(moduleName)
}
