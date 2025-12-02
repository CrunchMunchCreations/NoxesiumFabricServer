plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    id("fabric-loom") version "1.13-SNAPSHOT"
    id("maven-publish")
}

group = "xyz.crunchmunch"
version = "1.0.0"

loom {
    accessWidenerPath = file("src/main/resources/noxesium-fabric.accesswidener")
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    maven("https://maven.noxcrew.com/public")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}:${property("parchment_release")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Just because I like Kotlin more than Java
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    api(include("com.github.Phyrone:brigardier-kotlin:${property("brigadier_kotlin_version")}")!!)

    include(modApi("com.noxcrew.noxesium:api:${rootProject.property("noxesium_version")}")!!)
    include(modApi("com.noxcrew.noxesium:common:${rootProject.property("noxesium_version")}")!!)
    modRuntimeOnly("com.noxcrew.noxesium:fabric:${rootProject.property("noxesium_version")}")
}

tasks {
    // Configure remapJar to run when invoking the build task
    named("assemble", DefaultTask::class.java) {
        dependsOn(named("remapJar").get())
    }

    processResources {
        filteringCharset = "UTF-8" // We want UTF-8 for everything
        var props = mapOf(
            "version" to project.version,
            "loader_version" to rootProject.property("loader_version") as String,
            "fabric_version" to rootProject.property("fabric_version") as String,
            "minecraft_version" to rootProject.property("minecraft_version") as String
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }
}