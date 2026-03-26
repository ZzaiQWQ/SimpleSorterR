plugins {
    id("fabric-loom") version "1.14.10"
    kotlin("jvm") version "2.0.0"
}

version = "2.0.0"
group = "simplesorter"

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1") 
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modCompileOnly("net.fabricmc:fabric-loader:0.16.5")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:0.103.0+1.21.1")

    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:15.0.140")
    modCompileOnly("com.terraformersmc:modmenu:11.0.2")

    implementation(project(":platforms:core"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
