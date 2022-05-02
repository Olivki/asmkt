plugins {
    kotlin("jvm") version "1.6.21"
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "net.ormr.asmkt"
description = "Kotlin DSL wrapper for ASM"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(group = "org.ow2.asm", name = "asm-commons", version = "9.1")
    implementation(group = "moe.kanon.krautils", name = "krautils-core", version = "0.0.1")
}

kotlin {
    //explicitApi()
}

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("Olivki", "asmkt")
    licenseApacheV2()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "15"
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlin.Experimental",
                "-Xjvm-default=all"
            )
        }
    }
}