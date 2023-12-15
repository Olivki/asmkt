## asmkt

![Maven Central](https://img.shields.io/maven-central/v/net.ormr.asmkt/asmkt?label=release&style=for-the-badge)

`asmkt` is a Kotlin library that provides a DSL and a set of utilities for generating bytecode using
the [ASM](https://asm.ow2.io/) library.

This library requires at least Java 8.

## Installation

```kotlin
dependencies {
    implementation(group = "net.ormr.asmkt", name = "asmkt", version = "${RELEASE_VERSION}")
}
```

## Notes

Currently the following features aren't supported:

- Modules
- Record components