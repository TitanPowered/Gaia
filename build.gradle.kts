allprojects {
    group = "me.moros"
    version = "1.3.0"
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
        options.isFork = true
        options.forkOptions.executable = "javac"
    }
    buildDir = rootProject.buildDir
}
