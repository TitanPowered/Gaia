plugins {
    java
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.0")
    implementation("net.kyori", "adventure-api", "4.0.0-SNAPSHOT")
    implementation("co.aikar","acf-core", "0.5.0-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-core", "7.0.0")
}
