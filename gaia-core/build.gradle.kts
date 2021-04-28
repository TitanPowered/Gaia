plugins {
    java
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    implementation("co.aikar", "acf-core", "0.5.0-SNAPSHOT")
    implementation("org.checkerframework", "checker-qual", "3.12.0")
    compileOnly("net.kyori", "adventure-api", "4.7.0")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.0.0")
}
