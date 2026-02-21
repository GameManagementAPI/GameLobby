plugins {
    kotlin("jvm") version "2.1.10"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "de.c4vxl"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://mvn.c4vxl.de/gma/")
}

dependencies {
    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Paper API
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    // CommandAPI
    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.1.0")

    // GameManagementAPI
    implementation("de.c4vxl:gamemanagementapi:1.0.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

// Mojang mapping
paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

bukkit {
    name = "GameLobby"
    description = "This plugin implements a lobby system for servers using the GameManagementAPI."
    main = "$group.gamelobby.Main"
    version = "1.0.0"
    apiVersion = "1.14" // 1.14+

    authors = listOf("c4vxl")
    website = "https://gma.c4vxl.de/"

    softDepend = listOf("GameManager")

    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib:2.1.10",
        "dev.jorel:commandapi-paper-shade:11.1.0",
        "dev.jorel:commandapi-kotlin-paper:11.1.0"
    )
}