plugins {
    id("fabric-loom") version "1.14.10"
    id("maven-publish")
    kotlin("jvm") version "2.0.0"
}

version = "3.5.0"
group = "simplesorter"

base {
    archivesName.set("simplesorter-fabric-1.20.1")
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1") 
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Cloth Config (JiJ included into the built jar)
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:11.1.118")
    include("me.shedaniel.cloth:cloth-config-fabric:11.1.118")
    
    // ModMenu for testing locally
    modLocalRuntime("com.terraformersmc:modmenu:7.2.2")

    // Kotlin Language for Fabric
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.1+kotlin.2.0.20")

    // Dependencies on core sorting logic
    implementation(project(":platforms:core"))
    include(project(":platforms:core"))

    // Dependencies on shared MC logic (Tweaks, Scanner, etc)
    implementation(project(path = ":platforms:shared-1.20", configuration = "namedElements"))
    include(project(":platforms:shared-1.20"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
