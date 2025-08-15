pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }

    val fkotlin_version: String by settings

    plugins {
        id("fabric-loom") version "1.5-SNAPSHOT"
        id("org.jetbrains.kotlin.jvm") version fkotlin_version.split("+kotlin.")[1]
    }
}
