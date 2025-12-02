pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://dl.bintray.com/brambolt/public")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "NoxesiumFabricServer"