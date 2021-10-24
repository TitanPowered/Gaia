allprojects {
    group = "me.moros"
    version = "1.5.0"

    apply(plugin = "java")

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://repo.aikar.co/content/groups/aikar/")
    }
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
}
