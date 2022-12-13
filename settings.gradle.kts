pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
rootProject.name = "gaia"
include("gaia-core")
file("gaia-paper/nms").listFiles { _, name -> name.startsWith("nms-") }?.forEach {
    include("gaia-paper:nms:${it.name}")
}
include("gaia-paper")
