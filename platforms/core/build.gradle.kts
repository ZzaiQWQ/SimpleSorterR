plugins {
    kotlin("jvm") version "2.0.0"
}

version = "1.0.0"
group = "simplesorter"

repositories {
    mavenCentral()
}

dependencies {
    // Only standard Kotlin stdlib or JSON parsing libraries (e.g. kotlinx.serialization) if needed
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
