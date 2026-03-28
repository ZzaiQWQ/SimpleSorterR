plugins {
    id("fabric-loom") version "1.14.10"
    id("maven-publish")
    kotlin("jvm") version "2.0.0"
}

version = "3.1.0"
group = "simplesorter"

base {
    archivesName.set("simplesorter-fabric-1.21.8")
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.8") 
    mappings("net.fabricmc:yarn:1.21.8+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.134.0+1.21.8")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Cloth Config (JiJ included into the built jar)
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:19.0.147")
    include("me.shedaniel.cloth:cloth-config-fabric:19.0.147")
    
    // ModMenu for testing locally
    modLocalRuntime("com.terraformersmc:modmenu:15.0.0")

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
