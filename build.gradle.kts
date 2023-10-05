plugins {
    kotlin("jvm") version "1.9.10"
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
}

kotlin {
    explicitApi()
}

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("Olivki", "asmkt")
    licenseApacheV2()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.contracts.ExperimentalContracts",
            )
        }
    }
}