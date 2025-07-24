plugins {
    kotlin("jvm") version "1.9.20-RC"
}

group = "net.ormr.asmkt"
description = "Kotlin DSL wrapper for ASM"
version = "0.3.1"

repositories {
    mavenCentral()
}

dependencies {
    api("org.ow2.asm:asm-commons:9.6")
    implementation(kotlin("reflect"))
}

kotlin {
    explicitApi()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += "-opt-in=kotlin.contracts.ExperimentalContracts"
            freeCompilerArgs += "-Xcontext-receivers"
            freeCompilerArgs += "-Xjvm-default=all"
        }
    }
}