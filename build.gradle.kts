import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "com.github.m5rian"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.kord", "kord-core", "0.11.1")
    implementation("ch.qos.logback", "logback-classic", "1.4.11")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

tasks.withType<Jar> {
    manifest { attributes["Main-Class"] = "MainKt" }
}

application {
    mainClass.set("MainKt")
}