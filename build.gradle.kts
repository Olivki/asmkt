plugins {
    kotlin("jvm") version "1.9.20-RC"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "net.ormr.asmkt"
description = "Kotlin DSL wrapper for ASM"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.ow2.asm:asm-commons:9.6")
    implementation(kotlin("reflect"))
}

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("Olivki", "asmkt")
    licenseApacheV2()
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