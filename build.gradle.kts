import name.remal.gradle_plugins.dsl.extensions.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension

plugins {
    kotlin("jvm") version "1.4.31"
    id("name.remal.maven-publish-ossrh") version "1.2.2"
    id("name.remal.check-dependency-updates") version "1.2.2"
    `maven-publish`
}

group = "moe.kanon.asmkt"
description = "Kotlin DSL wrapper for ASM"
version = "0.0.4-SNAPSHOT"
val gitUrl = "github.com/Olivki/asmkt"

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

tasks {
    withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "15"
            freeCompilerArgs = listOf(
                "-Xuse-experimental=kotlin.Experimental",
                "-Xjvm-default=all"
            )
        }
    }
}

afterEvaluate {
    publishing.publications.withType<MavenPublication> {
        pom {
            name.set("${project.group}:${project.name}")
            description.set(project.description)
            url.set(gitUrl)

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("Olivki")
                    name.set("Oliver Berg")
                    email.set("oliver@berg.moe")
                }
            }

            scm {
                connection.set("scm:git:git://${gitUrl}.git")
                developerConnection.set("scm:git:ssh://${gitUrl}.git")
                url.set("https://$gitUrl")
            }
        }
    }

    // ${project.name} is what's used as the artifactId
    publishing.repositories.convention[RepositoryHandlerOssrhExtension::class.java].ossrh()
}