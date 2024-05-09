plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
    id("org.checkerframework")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.enginehub.org/repo/")
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        options.encoding = "UTF-8"
    }
    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
