plugins {
    java
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    implementation("co.aikar","acf-core", "0.5.0-SNAPSHOT")
    implementation("org.checkerframework", "checker-qual","3.10.0")
    compileOnly("net.kyori", "adventure-api", "4.7.0")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.0.0")
}
