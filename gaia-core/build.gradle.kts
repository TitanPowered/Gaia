dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
    compileOnly("org.spongepowered", "configurate-hocon", "4.1.2")
    compileOnly("org.slf4j", "slf4j-api", "1.7.36")
    compileOnly("cloud.commandframework","cloud-core", "1.6.2")
    compileOnly("org.checkerframework", "checker-qual", "3.22.1")
    compileOnly("net.kyori", "adventure-api", "4.11.0")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.6")
    compileOnly("com.google.code.gson", "gson", "2.9.0")
}

tasks.test {
    useJUnitPlatform()
}
