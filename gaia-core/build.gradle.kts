plugins {
    java
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    implementation("com.google.code.gson", "gson", "2.8.6")
    implementation("net.kyori", "adventure-api", "4.0.0-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.0.0")
}
