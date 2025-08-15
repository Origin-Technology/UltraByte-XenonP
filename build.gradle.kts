import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.net.URI

plugins {
    id("fabric-loom") version "1.9.2"
    id("maven-publish")
    kotlin("jvm")
    java
    id("com.gradleup.shadow") version "8.3.6"
    id("com.github.gmazzo.buildconfig") version "5.5.1"
    id("io.freefair.lombok") version "8.11"
}

val mod_version: String by project
val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_api_version: String by project

val library by configurations.creating

repositories {
    mavenCentral()
	maven("https://maven.meteordev.org/releases")
	maven("https://maven.meteordev.org")
    maven {
        url = uri("http://maven.origin.pw/repository/maven-inside")
        credentials{
            username = "origin"
            password = "4c3009a5e4"
            isAllowInsecureProtocol = true
        }
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:${minecraft_version}")
    "mappings"("net.fabricmc:yarn:${yarn_mappings}:v2")
	modImplementation("net.fabricmc:fabric-loader:${loader_version}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_api_version}")

    library("org.reflections:reflections:0.10.2")
    library("meteordevelopment:discord-ipc:1.1")
    library("tech.origin:Beacon:1.1")
    library("tech.origin:XenonAuth:0.0.1") {
        exclude("org.ow2.asm")
    }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.scala-lang:scala3-library_3:3.6.3")
}

configurations {
    implementation.get().extendsFrom(library)
}

tasks.processResources {
    inputs.property("version", mod_version)

    filesMatching("fabric.mod.json") {
        expand("version" to mod_version)
    }
}

loom {
	accessWidenerPath = file("src/main/resources/ultrabyte.accesswidener")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
        optIn = listOf("kotlin.RequiresOptIn", "kotlin.contracts.ExperimentalContracts")
        freeCompilerArgs = listOf(
            "-Xjvm-default=all-compatibility",
            "-Xlambdas=indy",
            "-Xcontext-receivers"
        )
    }
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}"}
	}
    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.shadowJar {
    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations = listOf(library)
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
    inputFile = tasks.shadowJar.get().archiveFile
}