plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "net.ormr.asmkt"
description = "Kotlin DSL wrapper for ASM"
version = "0.3.1"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.asm.commons)
    implementation(kotlin("reflect"))
}

kotlin {
    explicitApi()
    jvmToolchain(21)
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