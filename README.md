## asmkt

![Maven Central](https://img.shields.io/maven-central/v/net.ormr.asmkt/asmkt?label=release&style=for-the-badge)

`asmkt` is a Kotlin library that provides a DSL and a set of utilities for generating bytecode using
the [ASM](https://asm.ow2.io/) library.

This library requires at least Java 17.

## Usage

The main entry point to basically everything in `asmkt` is the `BytecodeClass` class, which provides builder functions
for incrementally building up a JVM class. Easiest way to create a `BytecodeClass` instance is to use the `defineClass`
function.

Once the desired class has been built, you can retrieve the bytecode of it by calling the `toByteArray` function on
the `BytecodeClass` instance.

`asmkt` has *full* support for all JVM features introduced up to Java 19.

### `BytecodeMethod` vs `BytecodeBlock`

`asmkt` will essentially always return a `BytecodeMethod` instance when you create a new method. The `BytecodeMethod`
class is basically a higher level wrapper for a `BytecodeBlock` instance, which contains almost 1:1 mappings of the JVM
opcodes. One can access the underlying `BytecodeBlock` instance by accessing the `block` property or calling
the `useBlock` function on a `BytecodeMethod` instance.

### Hello World Example

Let's say we want to create bytecode that represents the following Java code:

```java
package hello.world;

public final class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

We could then write something like this:

```kotlin
val clz = defineClass(
    type = ReferenceType.fromInternal("hello/world/HelloWorld"),
    version = BytecodeVersion.JAVA_8,
    access = Modifier.PUBLIC + Modifier.FINAL,
) {
    defineMethod(
        name = "main",
        type = MethodType.ofVoid(ArrayType.STRING),
        access = Modifier.PUBLIC + Modifier.STATIC,
    ) {
        getStaticField(
            owner = ReferenceType<System>(),
            name = "out",
            type = ReferenceType<PrintStream>(),
        )
        pushString("Hello, World!")
        invokeVirtual(
            owner = ReferenceType<PrintStream>(),
            name = "println",
            type = MethodType.ofVoid(ReferenceType.STRING),
        )
        returnValue()
    }
}
```

We can then compile this `BytecodeClass` instance into JVM bytecode and dynamically load it, to see that it works.

First we'll need a simple `ClassLoader` implementation that allows us to load a class from a `ByteArray`:

```kotlin
class ByteArrayClassLoader(val bytes: ByteArray) : ClassLoader() {
    override fun findClass(name: String): Class<*> = defineClass(name, bytes, 0, bytes.size)
}
```

And then the actual code to compile, load, and invoke the `main` method of the `HelloWorld` class:

```kotlin
val bytes = clz.toByteArray() // compiles the class into bytecode
val loader = ByteArrayClassLoader(bytes)
val loadedClass = loader.loadClass(clz.className) // defines and loads our custom class
// retrieves the 'main' method and invokes it
loadedClass.getDeclaredMethod("main", Array<String>::class.java).invoke(null, arrayOf<String>())
```

Combining the above code should result in `Hello, World!` being printed to the console.

The above example is *very* simple, as does not delve into a lot of the other functionality that `asmkt` provides. As
there's *a lot* of functions provided, quickest way is probably to look through the source of the classes you're going
to be using, like `BytecodeClass` or `BytecodeMethod`.

### Production Examples

- [eventbus: Dynamic EventBus Dispatcher Class Generation](https://github.com/Olivki/eventbus/blob/master/core/src/main/kotlin/net/ormr/eventbus/factories/asm.kt)
  *(Uses version `0.0.9`, so modifier usage looks different)*

## Installation

```kotlin
  dependencies {
    implementation(group = "net.ormr.asmkt", name = "asmkt", version = "${RELEASE_VERSION}")
}
```
