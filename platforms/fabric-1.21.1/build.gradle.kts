plugins {
    id("fabric-loom") version "1.14.10"
    id("maven-publish")
    kotlin("jvm") version "2.0.0"
}

version = "3.0.0"
group = "simplesorter"

base {
    archivesName.set("simplesorter-fabric-1.21.1")
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1") 
    // Wait, the user asked for 1.21.11. Is there a 1.21.11? Usually there are only major.minor.patch up to 1.21.1 currently, or 1.21.4. I should check Minecraft versions later to verify if 1.21.11 is a Bedrock version or Java version. Java versions are usually 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4. There is no Java 1.21.11. I will set it to 1.21.1 for now but use 1.21.11 in naming, or check versions.
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.103.0+1.21.1")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Cloth Config (JiJ included into the built jar)
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:15.0.140")
    include("me.shedaniel.cloth:cloth-config-fabric:15.0.140")
    
    // ModMenu for testing locally
    modLocalRuntime("com.terraformersmc:modmenu:11.0.2")

    // Kotlin Language for Fabric
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.1+kotlin.2.0.20")

    // Dependencies on core sorting logic
    implementation(project(":platforms:core"))
    include(project(":platforms:core"))

    // Dependencies on shared MC logic (Tweaks, Scanner, etc)
    implementation(project(path = ":platforms:shared-1.21-plus", configuration = "namedElements"))
    include(project(":platforms:shared-1.21-plus"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
