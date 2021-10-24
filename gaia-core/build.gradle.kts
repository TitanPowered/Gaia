dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
    compileOnly("co.aikar", "acf-core", "0.5.0-SNAPSHOT")
    compileOnly("org.checkerframework", "checker-qual", "3.18.1")
    compileOnly("net.kyori", "adventure-api", "4.9.2")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.2.6")
    compileOnly("com.google.code.gson", "gson", "2.8.8")
}

tasks.test {
    useJUnitPlatform()
}
