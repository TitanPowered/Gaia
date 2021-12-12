dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
    compileOnly("org.spongepowered", "configurate-hocon", "4.1.2")
    compileOnly("org.slf4j", "slf4j-api", "1.7.32")
    compileOnly("cloud.commandframework","cloud-core", "1.6.0")
    compileOnly("org.checkerframework", "checker-qual", "3.20.0")
    compileOnly("net.kyori", "adventure-api", "4.9.3")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.0")
    compileOnly("com.google.code.gson", "gson", "2.8.8")
}

tasks.test {
    useJUnitPlatform()
}
