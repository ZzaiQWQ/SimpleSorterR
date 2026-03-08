pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "SimpleSorter"

include(":platforms:core")
include(":platforms:shared-1.20")
include(":platforms:shared-1.21-plus")
include(":platforms:fabric-1.20.1")
include(":platforms:fabric-1.21.1")
include(":platforms:fabric-1.21.11")
