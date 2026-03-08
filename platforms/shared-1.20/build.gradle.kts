plugins {
    id("fabric-loom") version "1.14.10"
    kotlin("jvm") version "2.0.0"
}

version = "1.0.0"
group = "simplesorter"

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1") 
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modCompileOnly("net.fabricmc:fabric-loader:0.15.11")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1")

    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:11.1.118")
    modCompileOnly("com.terraformersmc:modmenu:7.2.2")

    implementation(project(":platforms:core"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
