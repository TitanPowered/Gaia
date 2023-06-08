plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.checker)
}

allprojects {
    group = "me.moros"
    version = "1.8.2"

    apply(plugin = "java")
    apply(plugin = "org.checkerframework")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks {
        withType<JavaCompile> {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
            options.encoding = "UTF-8"
        }
        assemble {
            dependsOn(shadowJar)
        }
    }
}
