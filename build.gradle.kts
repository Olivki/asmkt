plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenDeployer)
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
    jvmToolchain(8)
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

deployer {
    content {
        kotlinComponents {
            emptyDocs()
            kotlinSources()
        }
    }

    projectInfo {
        url.set("https://github.com/Olivki/asmkt")
        license(apache2)
        developer("Olivki", "oliver@berg.moe")

        scm {
            fromGithub("Olivki", "asmkt")
        }
    }

    localSpec()

    centralPortalSpec {
        allowMavenCentralSync = false

        auth {
            user.set(secret("CENTRAL_PORTAL_USER"))
            password.set(secret("CENTRAL_PORTAL_PASSWORD"))
        }

        signing {
            key.set(secret("OSSRH_GPG_SECRET_KEY"))
            password.set(secret("OSSRH_GPG_SECRET_KEY_PASSWORD"))
        }
    }
}