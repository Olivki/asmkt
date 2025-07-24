## asmkt

![Maven Central](https://img.shields.io/maven-central/v/net.ormr.asmkt/asmkt?label=release&style=for-the-badge)

`asmkt` is a Kotlin library that provides a DSL and a set of utilities for generating bytecode using
the [ASM](https://asm.ow2.io/) library.

This library requires at least Java 8.

The library is *only* intended for generating JVM classes as of right now, parsing JVM classes is *not* supported.

Heavily inspired by the new `ClassFile` API being worked on for the JVM. Motivation is to have something that looks
similar for generating JVM bytecode that can run on older JVM versions.

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

## Examples

For the below examples we'll be using this basic `ClassLoader` implementation:

```kotlin
class ClassFileLoader : ClassLoader() {
    fun loadClassFile(classFile: ClassFile): Class<*> =
        defineClass(classFile.type.name, classFile.bytes, 0, classFile.bytes.size)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> load(classFile: ClassFile): Class<T> = ClassFileLoader().loadClassFile(classFile) as Class<T>
    }
}
```

Note that this implementation of a `ClassLoader` is *not* the best, as it's only made for the purpose of quickly running
our compiled classes.

### Hello, World!

Let's say we want to generate the equivalent of this Java code:

```java
package foo.bar;

public final class HelloWorld {
    public static void main(final String[] args) {
        System.out.println("Hello, World!");
    }
}
```

We could write something like this:

```kotlin
val element = ClassElement(
    // Sets the minimum JDK version required to load the class to JDK 8
    version = ClassFileVersion.RELEASE_8,
    // Defines a class located in the package 'foo.bar' with the name 'HelloWorld' 
    type = ReferenceType("foo/bar/HelloWorld"),
    // Sets the modifiers of the class to 'public' and 'final'
    flags = AccessFlag.PUBLIC + AccessFlag.FINAL,
) {
    // public static void main(final String[] args)
    method(
        name = "main",
        // A method with return type of 'void', that accepts 1 argument of type 'String[]'
        type = MethodType(VoidType, ArrayType.STRING),
        flags = AccessFlag.PUBLIC + AccessFlag.STATIC,
    ) {
        // Sets the name of the parameter at index 0 to 'args'
        // We can optionally also pass in a block here if we want to add annotations to the parameter
        parameter(index = 0, name = "args", flags = AccessFlag.FINAL)
        // Scopes us into the higher level instruction builder, 'withCodeChunk' can be used to access
        // a more low level instruction builder DSL
        withBody {
            // Pushes a 'getstatic' instruction for the 'System.out' field onto the stack
            getStaticField(
                owner = ReferenceType<System>(),
                name = "out",
                type = ReferenceType<PrintStream>(),
            )
            // Pushes a string constant onto the stack
            pushString("Hello, World!")
            // Pushes a 'invokevirtual' instruction onto the stack
            // this will pop the top-most value on the stack
            // as the pointer to the instance to invoke on which is our 'System.out' field
            // and then it will pop our string constant off the stack
            // as we gave it a MethodType expecting 1 argument
            invokeVirtual(
                // The class that the method belongs to
                owner = ReferenceType<PrintStream>(),
                name = "println",
                type = MethodType(VoidType, ReferenceType.STRING),
            )
            // return an appropriately typed value
            // all methods need to end with a `RETURN` instruction
            // or some other non-branching instruction, even 'void' methods
            returnValue()
        }
    }
}
```

`element` will now contain a `ClassElement` that has all the elements required to generate JVM bytecode similar to that
we would get from the Java code example.

To actually run this code we need to do some "plumbing":

```kotlin
// Compiles 'element' to a 'ClassFile' with the default compiler
val classFile = ClassElementCompiler.compileToClassFile(element)
// Loads the compiled 'element' into the runtime with our  custom ClassLoader
val loadedClass = ClassFileLoader.load<Any>(classFile)
// Retrieves the 'main' method and invokes it via reflection
loadedClass.getDeclaredMethod("main", Array<String>::class.java).invoke(null, arrayOf<String>())
// out: Hello, World!
```

The `ClassElementCompiler` is how one turns a `ClassElement` into actual JVM bytecode. It comes with two
functions, `compileToBytes` and `compileToClassFile`, a `ClassFile` is just a wrapper around JVM bytecode that contains
the `ClassFileVersion` and the `ReferenceType` of the compiled class, along with the actual compiled JVM bytecode.

### Generating an interface

The "kind" of a class is determined by the `ClassKind` enum, which we pass in when we create the builder.

If we wanted to say, create an interface that looks something like this Java code:

```java
package foo.bar;

public interface Foo {
    void bar(String fooBar);
}
```

We could do something like this:

```kotlin
ClassElement(
    version = ClassFileVersion.RELEASE_8,
    type = ReferenceType("foo/bar/Foo"),
    flags = AccessFlag.PUBLIC,
    kind = ClassKind.INTERFACE,
) {
    method(
        name = "bar",
        flags = AccessFlag.PUBLIC + AccessFlag.ABSTRACT,
        type = MethodType(VoidType, ReferenceType.STRING),
    ) {
        parameter(index = 0, name = "fooBar")
        // Abstract methods are *not* allowed to have any instructions
    }
}
```

### `if`s, implementing interfaces and instances

A quick and dirty example showcasing the `if` builders, how to implement interfaces, and default constructors.

We want to generate something similar to this Java code:

```java
package foo.bar;

public class IfTestImpl implements IfTest {
    public final void test(String name) {
        if (name.equals("Dave")) {
            System.out.println("Hello, Dave.");
        } else {
            System.out.println("Hello, unknown.");
        }
    }
}
```

Where `IfTest` is an existing interface that looks like this:

```java
package foo.bar;

public interface IfTest {
    void test(String name);
}
```

Note that we're *not* going to be generating the JVM bytecode for the `IfTest` interface itself. If you want to see how
to generate an interface, see the previous chapter.

The following code will generate a similar result:

```kotlin
val element = ClassElement(
    version = ClassFileVersion.RELEASE_8,
    type = ReferenceType("foo/bar/IfTestImpl"),
    flags = AccessFlag.PUBLIC,
    kind = ClassKind.CLASS,
    interfaces = listOf(ReferenceType<IfTest>()),
) {
    // Generate a default no arguments constructor
    // If we didn't generate one, we would not be able to properly create an instance of 'IfTestImpl'
    defaultConstructor()
    method(
        name = "test",
        flags = AccessFlag.PUBLIC + AccessFlag.FINAL,
        type = MethodType(VoidType, ReferenceType.STRING),
    ) {
        parameter(index = 0, name = "name")
        withBody {
            // Index = 1 here because the local at Index = 0 is the 'this' pointer
            loadLocal(index = 1, type = ReferenceType.STRING)
            pushString("Dave")
            invokeVirtual(
                owner = ReferenceType.STRING,
                name = "equals",
                type = MethodType(BooleanType, ReferenceType.OBJECT),
            )
            ifThenElse(
                thenBuilder = {
                    getStaticField(
                        owner = ReferenceType<System>(),
                        name = "out",
                        type = ReferenceType<PrintStream>(),
                    )
                    pushString("Hello, Dave.")
                    invokeVirtual(
                        owner = ReferenceType<PrintStream>(),
                        name = "println",
                        type = MethodType(VoidType, ReferenceType.STRING),
                    )
                },
                elseBuilder = {
                    getStaticField(
                        owner = ReferenceType<System>(),
                        name = "out",
                        type = ReferenceType<PrintStream>(),
                    )
                    pushString("Hello, unknown.")
                    invokeVirtual(
                        owner = ReferenceType<PrintStream>(),
                        name = "println",
                        type = MethodType(VoidType, ReferenceType.STRING),
                    )
                },
            )
            returnValue()
        }
    }
}
```

And then the required "plumbing" to test if our code runs like it should:

```kotlin
val classFile = ClassElementCompiler.compileToClassFile(element)
// Load the classFile as an instance of 'IfTest'
// This is so we can easily call the 'test' method
val loadedClass = ClassFileLoader.load<IfTest>(classFile)
// Create a new instance of the newly loaded 'IfTestImpl' class
val instance = loadedClass.newInstance()
instance.test("Dave") // out: Hello, Dave.
instance.test("Not Dave") // out: Hello, unknown.
```