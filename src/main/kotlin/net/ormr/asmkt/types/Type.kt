/*
 * Copyright 2020-2022 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("TypeUtils")
@file:Suppress("unused")

package net.ormr.asmkt.types

import java.lang.invoke.CallSite
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.Collections.unmodifiableList
import org.objectweb.asm.Type as BackingType
import java.lang.invoke.MethodType as JMethodType
import java.lang.reflect.Array as ArrayReflection

sealed class Type {
    companion object {
        @JvmStatic
        fun copyOf(type: BackingType): Type = when (type.sort) {
            BackingType.VOID, BackingType.BOOLEAN, BackingType.CHAR, BackingType.BYTE, BackingType.SHORT,
            BackingType.INT, BackingType.LONG, BackingType.FLOAT, BackingType.DOUBLE,
            -> PrimitiveType.copyOf(type)
            BackingType.ARRAY -> ArrayType.copyOf(type)
            BackingType.OBJECT -> ReferenceType.copyOf(type)
            BackingType.METHOD -> MethodType.copyOf(type)
            else -> throw UnsupportedOperationException("Unknown 'sort' value ${type.sort}.")
        }
    }

    protected abstract val delegate: BackingType

    // TODO: documentation

    abstract val size: Int

    abstract val descriptor: String

    fun getOpcode(opcode: Int): Int = delegate.getOpcode(opcode)

    fun toAsmType(): BackingType = delegate

    final override fun equals(other: Any?): Boolean = delegate == other

    private val cachedHash: Int by lazy { delegate.hashCode() }

    final override fun hashCode(): Int = cachedHash

    abstract override fun toString(): String
}

/**
 * Represents the type of a method.
 */
class MethodType private constructor(override val delegate: BackingType) : Type() {
    companion object {
        private val cachedTypes: MutableMap<String, MethodType> = hashMapOf()

        // -- PRIMITIVES -- \\
        @JvmField
        val VOID: MethodType = createConstant("()V")

        @JvmField
        val BOOLEAN: MethodType = createConstant("()Z")

        @JvmField
        val CHAR: MethodType = createConstant("()C")

        @JvmField
        val BYTE: MethodType = createConstant("()B")

        @JvmField
        val SHORT: MethodType = createConstant("()S")

        @JvmField
        val INT: MethodType = createConstant("()I")

        @JvmField
        val LONG: MethodType = createConstant("()J")

        @JvmField
        val FLOAT: MethodType = createConstant("()F")

        @JvmField
        val DOUBLE: MethodType = createConstant("()D")

        @JvmField
        val VOID_WRAPPER: MethodType = createConstant("()Ljava/lang/Void;")

        @JvmField
        val BOOLEAN_WRAPPER: MethodType = createConstant("()Ljava/lang/Boolean;")

        @JvmField
        val CHAR_WRAPPER: MethodType = createConstant("()Ljava/lang/Character;")

        @JvmField
        val BYTE_WRAPPER: MethodType = createConstant("()Ljava/lang/Byte;")

        @JvmField
        val SHORT_WRAPPER: MethodType = createConstant("()Ljava/lang/Short;")

        @JvmField
        val INT_WRAPPER: MethodType = createConstant("()Ljava/lang/Integer;")

        @JvmField
        val LONG_WRAPPER: MethodType = createConstant("()Ljava/lang/Long;")

        @JvmField
        val FLOAT_WRAPPER: MethodType = createConstant("()Ljava/lang/Float;")

        @JvmField
        val DOUBLE_WRAPPER: MethodType = createConstant("()Ljava/lang/Double;")

        @JvmField
        val OBJECT: MethodType = createConstant("()Ljava/lang/Object;")

        @JvmField
        val STRING: MethodType = createConstant("()Ljava/lang/String;")

        @JvmField
        val STRING_BUILDER: MethodType = createConstant("()Ljava/lang/StringBuilder;")

        @JvmField
        val NUMBER: MethodType = createConstant("()Ljava/lang/Number;")

        @JvmStatic
        fun ofVoid(vararg argumentTypes: FieldType): MethodType = VOID.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofBoolean(vararg argumentTypes: FieldType): MethodType =
            BOOLEAN.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofChar(vararg argumentTypes: FieldType): MethodType = CHAR.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofByte(vararg argumentTypes: FieldType): MethodType = BYTE.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofShort(vararg argumentTypes: FieldType): MethodType = SHORT.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofInt(vararg argumentTypes: FieldType): MethodType = INT.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofLong(vararg argumentTypes: FieldType): MethodType = LONG.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofFloat(vararg argumentTypes: FieldType): MethodType = FLOAT.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofDouble(vararg argumentTypes: FieldType): MethodType =
            DOUBLE.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofVoidWrapper(vararg argumentTypes: FieldType): MethodType =
            VOID_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofBooleanWrapper(vararg argumentTypes: FieldType): MethodType =
            BOOLEAN_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofCharWrapper(vararg argumentTypes: FieldType): MethodType =
            CHAR_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofByteWrapper(vararg argumentTypes: FieldType): MethodType =
            BYTE_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofShortWrapper(vararg argumentTypes: FieldType): MethodType =
            SHORT_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofIntWrapper(vararg argumentTypes: FieldType): MethodType =
            INT_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofLongWrapper(vararg argumentTypes: FieldType): MethodType =
            LONG_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofFloatWrapper(vararg argumentTypes: FieldType): MethodType =
            FLOAT_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofDoubleWrapper(vararg argumentTypes: FieldType): MethodType =
            DOUBLE_WRAPPER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofObject(vararg argumentTypes: FieldType): MethodType =
            OBJECT.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofString(vararg argumentTypes: FieldType): MethodType =
            STRING.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofStringBuilder(vararg argumentTypes: FieldType): MethodType =
            STRING_BUILDER.appendArguments(argumentTypes.asIterable())

        @JvmStatic
        fun ofNumber(vararg argumentTypes: FieldType): MethodType =
            NUMBER.appendArguments(argumentTypes.asIterable())

        // -- FACTORY FUNCTIONS -- \\
        private fun createConstant(descriptor: String): MethodType {
            val type = MethodType(BackingType.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        @JvmStatic
        fun copyOf(type: BackingType): MethodType {
            requireSort(type, BackingType.METHOD)
            return MethodType(type)
        }

        @JvmStatic
        fun of(method: Method): MethodType =
            MethodType(BackingType.getType(method))

        @JvmStatic
        fun of(constructor: Constructor<*>): MethodType =
            MethodType(BackingType.getType(constructor))

        @JvmStatic
        fun of(methodType: JMethodType): MethodType =
            fromDescriptor(methodType.toMethodDescriptorString())

        @JvmStatic
        fun fromDescriptor(descriptor: String): MethodType =
            copyOf(BackingType.getMethodType(descriptor))

        // TODO: documentation
        @JvmStatic
        @JvmOverloads
        fun forBootstrap(returnType: FieldType = ReferenceType<CallSite>()): MethodType = of(
            returnType,
            ReferenceType<MethodHandles.Lookup>(),
            ReferenceType.STRING,
            ReferenceType<MethodType>(),
            ArrayType.OBJECT
        )

        @JvmStatic
        fun of(
            returnType: FieldType,
            vararg typeParameters: FieldType,
        ): MethodType = fromDescriptor(buildDescriptor(returnType, typeParameters))

        @JvmStatic
        @JvmOverloads
        fun createGeneric(
            arity: Int,
            finalArray: Boolean = false,
            returnType: FieldType = ReferenceType.OBJECT,
        ): MethodType {
            require(arity >= 0) { "'arity' must not be negative." }
            val size = if (finalArray) arity + 1 else arity
            val descriptor = buildString {
                append('(')
                repeat(size) {
                    if ((it + 1) == size && finalArray) {
                        append("[Ljava/lang/Object;")
                    } else {
                        append("Ljava/lang/Object;")
                    }
                }
                append(')')
                append(returnType.descriptor)
            }

            return fromDescriptor(descriptor)
        }

        @JvmSynthetic
        operator fun invoke(
            returnType: FieldType,
            vararg typeParameters: FieldType,
        ): MethodType = fromDescriptor(buildDescriptor(returnType, typeParameters))

        private fun buildDescriptor(
            returnType: FieldType,
            typeParameters: Array<out FieldType>,
        ): String = buildString {
            typeParameters.joinTo(this, "", "(", ")") { it.descriptor }
            append(returnType.descriptor)
        }
    }

    override val descriptor: String = delegate.descriptor

    override val size: Int
        get() = delegate.argumentsAndReturnSizes

    /**
     * The return type of `this` method type.
     */
    val returnType: FieldType = FieldType.copyOf(delegate.returnType)

    /**
     * An unmodifiable list of the types of the arguments of `this` method type.
     */
    val argumentTypes: List<FieldType> = unmodifiableList(delegate.argumentTypes.map { FieldType.copyOf(it) })

    fun prependArguments(newArgumentTypes: Iterable<FieldType>): MethodType {
        if (newArgumentTypes.none()) {
            return this
        }

        val newDescriptor = buildString {
            append('(')
            newArgumentTypes.joinTo(this, "") { it.descriptor }
            argumentTypes.joinTo(this, "") { it.descriptor }
            append(')')
            append(returnType.descriptor)
        }

        return fromDescriptor(newDescriptor)
    }

    fun prependArguments(vararg newArgumentTypes: FieldType): MethodType =
        prependArguments(newArgumentTypes.asIterable())

    fun appendArguments(newArgumentTypes: Iterable<FieldType>): MethodType {
        if (newArgumentTypes.none()) {
            return this
        }

        val newDescriptor = buildString {
            append('(')
            argumentTypes.joinTo(this, "") { it.descriptor }
            newArgumentTypes.joinTo(this, "") { it.descriptor }
            append(')')
            append(returnType.descriptor)
        }

        return fromDescriptor(newDescriptor)
    }

    fun appendArguments(vararg newArgumentTypes: FieldType): MethodType =
        appendArguments(newArgumentTypes.asIterable())

    /**
     * Returns a type based on `this` type but with the type of the argument at the given [index] changed to [newType].
     *
     * If the type at `index` is the same the given `newType` then `this` instance is returned.
     *
     * @throws [IllegalArgumentException] if `index` is negative, or if `index` is larger than the available
     * [argumentTypes]
     */
    fun changeArgument(index: Int, newType: FieldType): MethodType {
        require(index >= 0) { "'index' must not be negative." }
        require(index < argumentTypes.size) { "'index' is larger than available arguments; $index > ${argumentTypes.size}." }

        return when (newType) {
            argumentTypes[index] -> this
            else -> {
                val newDescriptor = buildString {
                    append('(')

                    for ((i, type) in argumentTypes.withIndex()) {
                        append(if (i == index) newType.descriptor else type.descriptor)
                    }

                    append(')')
                    append(returnType.descriptor)
                }

                fromDescriptor(newDescriptor)
            }
        }
    }

    /**
     * Returns a type based on `this` type but with the [return type][returnType] changed to the given [newType].
     *
     * If the current `returnType` is the same as `newType` then `this` instance is returned.
     */
    fun changeReturn(newType: FieldType): MethodType {
        return when (returnType) {
            newType -> this
            else -> {
                val newDescriptor = buildString {
                    argumentTypes.joinTo(this, "", "(", ")")
                    append(newType.descriptor)
                }

                fromDescriptor(newDescriptor)
            }
        }
    }

    /**
     * Returns a [JMethodType] representing the same method type as `this` type, loaded using the given [loader].
     *
     * @throws [UnsupportedOperationException] if `this` type is not a method type
     *
     * @see [JMethodType.fromMethodDescriptorString]
     */
    @JvmOverloads
    fun toMethodType(loader: ClassLoader? = null): JMethodType =
        JMethodType.fromMethodDescriptorString(descriptor, loader)

    override fun toString(): String = "(${argumentTypes.joinToString()}) -> $returnType"
}

/**
 * Represents a type that can be used as the type of a variable/parameter/return type, etc..
 *
 * @see [PrimitiveType]
 * @see [ReferenceType]
 * @see [ArrayType]
 */
sealed class FieldType : Type() {
    companion object {
        @JvmStatic
        fun copyOf(type: BackingType): FieldType = when (type.sort) {
            BackingType.VOID, BackingType.BOOLEAN, BackingType.CHAR, BackingType.BYTE, BackingType.SHORT,
            BackingType.INT, BackingType.LONG, BackingType.FLOAT, BackingType.DOUBLE,
            -> PrimitiveType.copyOf(type)
            BackingType.ARRAY -> ArrayType.copyOf(type)
            BackingType.OBJECT -> ReferenceType.copyOf(type)
            BackingType.METHOD -> throw IllegalArgumentException("A method type is not a definable type; '$type'.")
            else -> throw UnsupportedOperationException("Unknown 'sort' value ${type.sort}.")
        }

        @JvmStatic
        fun of(clz: Class<*>): FieldType = when {
            clz.isPrimitive -> PrimitiveType.of(clz)
            clz.isArray -> ArrayType.of(clz)
            else -> ReferenceType.of(clz)
        }

        @JvmSynthetic
        inline operator fun <reified T : Any> invoke(): ReferenceType = ReferenceType.of(T::class.java)
    }

    final override val size: Int
        get() = delegate.size

    abstract val className: String

    /**
     * Returns the simple name of `this` type, as it would have been given in the source code.
     *
     * The simple name of an array is the simple name of the [element type][ArrayType.elementType] with `[]` appended.
     */
    abstract val simpleName: String

    /**
     * Returns `true` if `this` type can be used as the type of a variable, otherwise `false`.
     */
    abstract val isValidFieldType: Boolean

    // TODO: use MethodHandle.Lookup instead of a classloader for this?
    /**
     * Returns the class representation of `this` type.
     *
     * @throws [ClassNotFoundException] if no class could be found representing `this` type
     */
    @Throws(ClassNotFoundException::class)
    abstract fun toClass(): Class<*>

    final override fun toString(): String = className
}

/**
 * Represents a primitive type, i.e; `byte`, `void`, `double`, etc..
 */
sealed class PrimitiveType : FieldType() {
    companion object {
        @JvmField
        val VOID: PrimitiveType = PrimitiveVoid

        @JvmField
        val BOOLEAN: PrimitiveType = PrimitiveBoolean

        @JvmField
        val CHAR: PrimitiveType = PrimitiveChar

        @JvmField
        val BYTE: PrimitiveType = PrimitiveByte

        @JvmField
        val SHORT: PrimitiveType = PrimitiveShort

        @JvmField
        val INT: PrimitiveType = PrimitiveInt

        @JvmField
        val LONG: PrimitiveType = PrimitiveLong

        @JvmField
        val FLOAT: PrimitiveType = PrimitiveFloat

        @JvmField
        val DOUBLE: PrimitiveType = PrimitiveDouble

        /**
         * Returns a list of all the known primitive types.
         */
        @JvmStatic
        val primitives: List<PrimitiveType>
            get() = unmodifiableList(listOf(VOID, BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE))

        @JvmStatic
        fun copyOf(type: BackingType): PrimitiveType = when (type.sort) {
            BackingType.VOID -> VOID
            BackingType.BOOLEAN -> BOOLEAN
            BackingType.CHAR -> CHAR
            BackingType.BYTE -> BYTE
            BackingType.SHORT -> SHORT
            BackingType.INT -> INT
            BackingType.LONG -> LONG
            BackingType.FLOAT -> FLOAT
            BackingType.DOUBLE -> DOUBLE
            else -> throw IllegalArgumentException("'type' must be a primitive type, was a ${sortNames[type.sort]} type.")
        }

        @JvmStatic
        fun fromDescriptor(descriptor: String): PrimitiveType = when (descriptor) {
            "V" -> VOID
            "Z" -> BOOLEAN
            "C" -> CHAR
            "B" -> BYTE
            "S" -> SHORT
            "I" -> INT
            "J" -> LONG
            "F" -> FLOAT
            "D" -> DOUBLE
            else -> throw IllegalArgumentException("'descriptor' ($descriptor) is not a valid primitive type descriptor.")
        }

        // TODO: documentation
        @JvmStatic
        fun of(clz: Class<*>): PrimitiveType {
            require(clz.isPrimitive || clz.isPrimitiveWrapper) { "'clz' must be a primitive or a primitive wrapper, was '${clz.name}'." }
            return when {
                clz.isPrimitive -> copyOf(BackingType.getType(clz))
                clz.isPrimitiveWrapper -> fromDescriptor(BackingType.getDescriptor(clz.primitiveClass))
                else -> throw IllegalStateException("Exhaustive 'when' was not exhaustive, for $clz.")
            }
        }

        private val Class<*>.isPrimitiveWrapper: Boolean
            get() = when (this) {
                Void::class.javaObjectType -> true
                Boolean::class.javaObjectType -> true
                Char::class.javaObjectType -> true
                Byte::class.javaObjectType -> true
                Short::class.javaObjectType -> true
                Int::class.javaObjectType -> true
                Long::class.javaObjectType -> true
                Float::class.javaObjectType -> true
                Double::class.javaObjectType -> true
                else -> false
            }

        private val Class<*>.primitiveClass: Class<*>
            get() = when (this) {
                Void::class.javaObjectType -> Void.TYPE
                Boolean::class.javaObjectType -> java.lang.Boolean.TYPE
                Char::class.javaObjectType -> Character.TYPE
                Byte::class.javaObjectType -> java.lang.Byte.TYPE
                Short::class.javaObjectType -> java.lang.Short.TYPE
                Int::class.javaObjectType -> Integer.TYPE
                Long::class.javaObjectType -> java.lang.Long.TYPE
                Float::class.javaObjectType -> java.lang.Float.TYPE
                Double::class.javaObjectType -> java.lang.Double.TYPE
                else -> throw UnsupportedOperationException("No primitive exists for $this.")
            }

        @JvmSynthetic
        inline operator fun <reified T : Any> invoke(): PrimitiveType = of(T::class.java)
    }

    abstract override val descriptor: String

    abstract override val delegate: BackingType

    override val simpleName: String
        get() = className

    abstract override val isValidFieldType: Boolean

    /**
     * Returns the boxed [object type][ReferenceType] of `this` primitive type.
     */
    abstract fun toBoxed(): ReferenceType

    /**
     * Returns the value stored in the `TYPE` field of the boxed type of the primitive type that `this` type represents.
     *
     * This implementation does *not* throw any [ClassNotFoundException]s as no class lookup is performed.
     */
    abstract override fun toClass(): Class<*>
}

/**
 * Represents the primitive `void` type.
 */
object PrimitiveVoid : PrimitiveType() {
    override val descriptor: String
        get() = "V"

    override val className: String
        get() = "void"

    override val delegate: BackingType
        get() = BackingType.VOID_TYPE

    override val isValidFieldType: Boolean
        get() = false

    override fun toBoxed(): ReferenceType = ReferenceType.VOID

    override fun toClass(): Class<*> = Void.TYPE
}

/**
 * Represents the primitive `boolean` type.
 */
object PrimitiveBoolean : PrimitiveType() {
    override val descriptor: String
        get() = "Z"

    override val className: String
        get() = "boolean"

    override val delegate: BackingType
        get() = BackingType.BOOLEAN_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.BOOLEAN

    override fun toClass(): Class<*> = java.lang.Boolean.TYPE
}

/**
 * Represents the primitive `char` type.
 */
object PrimitiveChar : PrimitiveType() {
    override val descriptor: String
        get() = "C"

    override val className: String
        get() = "char"

    override val delegate: BackingType
        get() = BackingType.CHAR_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.CHAR

    override fun toClass(): Class<*> = Character.TYPE
}

/**
 * Represents the primitive `byte` type.
 */
object PrimitiveByte : PrimitiveType() {
    override val descriptor: String
        get() = "B"

    override val className: String
        get() = "byte"

    override val delegate: BackingType
        get() = BackingType.BYTE_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.BYTE

    override fun toClass(): Class<*> = java.lang.Byte.TYPE
}

/**
 * Represents the primitive `short` type.
 */
object PrimitiveShort : PrimitiveType() {
    override val descriptor: String
        get() = "S"

    override val className: String
        get() = "short"

    override val delegate: BackingType
        get() = BackingType.SHORT_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.SHORT

    override fun toClass(): Class<*> = java.lang.Short.TYPE
}

/**
 * Represents the primitive `int` type.
 */
object PrimitiveInt : PrimitiveType() {
    override val descriptor: String
        get() = "I"

    override val className: String
        get() = "int"

    override val delegate: BackingType
        get() = BackingType.INT_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.INT

    override fun toClass(): Class<*> = Integer.TYPE
}

/**
 * Represents the primitive `long` type.
 */
object PrimitiveLong : PrimitiveType() {
    override val descriptor: String
        get() = "J"

    override val className: String
        get() = "long"

    override val delegate: BackingType
        get() = BackingType.LONG_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.LONG

    override fun toClass(): Class<*> = java.lang.Long.TYPE
}

/**
 * Represents the primitive `float` type.
 */
object PrimitiveFloat : PrimitiveType() {
    override val descriptor: String
        get() = "F"

    override val className: String
        get() = "float"

    override val delegate: BackingType
        get() = BackingType.FLOAT_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.FLOAT

    override fun toClass(): Class<*> = java.lang.Float.TYPE
}

/**
 * Represents the primitive `double` type.
 */
object PrimitiveDouble : PrimitiveType() {
    override val descriptor: String
        get() = "F"

    override val className: String
        get() = "double"

    override val delegate: BackingType
        get() = BackingType.DOUBLE_TYPE

    override val isValidFieldType: Boolean
        get() = true

    override fun toBoxed(): ReferenceType = ReferenceType.DOUBLE

    override fun toClass(): Class<*> = java.lang.Double.TYPE
}

/**
 * Represents a type of an `Object` child, i.e; `java.lang.String`, `java.lang.Byte`, `java.lang.Double`, etc..
 */
class ReferenceType private constructor(override val delegate: BackingType) : FieldType(), TypeWithInternalName {
    companion object {
        private val cachedTypes: MutableMap<String, ReferenceType> = hashMapOf()

        @JvmField
        val VOID: ReferenceType = createConstant("Ljava/lang/Void;")

        @JvmField
        val BOOLEAN: ReferenceType = createConstant("Ljava/lang/Boolean;")

        @JvmField
        val CHAR: ReferenceType = createConstant("Ljava/lang/Character;")

        @JvmField
        val BYTE: ReferenceType = createConstant("Ljava/lang/Byte;")

        @JvmField
        val SHORT: ReferenceType = createConstant("Ljava/lang/Short;")

        @JvmField
        val INT: ReferenceType = createConstant("Ljava/lang/Integer;")

        @JvmField
        val LONG: ReferenceType = createConstant("Ljava/lang/Long;")

        @JvmField
        val FLOAT: ReferenceType = createConstant("Ljava/lang/Float;")

        @JvmField
        val DOUBLE: ReferenceType = createConstant("Ljava/lang/Double;")

        @JvmField
        val OBJECT: ReferenceType = createConstant("Ljava/lang/Object;")

        @JvmField
        val CLASS: ReferenceType = createConstant("Ljava/lang/Class;")

        @JvmField
        val STRING: ReferenceType = createConstant("Ljava/lang/String;")

        @JvmField
        val STRING_BUILDER: ReferenceType = createConstant("Ljava/lang/StringBuilder;")

        @JvmField
        val OBJECTS: ReferenceType = createConstant("Ljava/util/Objects;")

        @JvmField
        val NUMBER: ReferenceType = createConstant("Ljava/lang/Number;")

        @JvmField
        val CONSTANT_BOOTSTRAPS: ReferenceType = createConstant("Ljava/lang/invoke/ConstantBootstraps;")

        @JvmField
        val STRING_CONCAT_FACTORY: ReferenceType = createConstant("Ljava/lang/invoke/StringConcatFactory;")

        @JvmField
        val METHOD_HANDLES_LOOKUP: ReferenceType = createConstant("Ljava/lang/invoke/MethodHandles\$Lookup;")

        private fun createConstant(descriptor: String): ReferenceType {
            val type = ReferenceType(BackingType.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        @JvmStatic
        fun copyOf(type: BackingType): ReferenceType {
            requireSort(type, BackingType.OBJECT)
            return cachedTypes.getOrPut(type.descriptor) { ReferenceType(type) }
        }

        @JvmStatic
        fun fromDescriptor(descriptor: String): ReferenceType = when (descriptor) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> copyOf(BackingType.getType(descriptor))
        }

        @JvmStatic
        fun fromInternal(internalName: String): ReferenceType = fromDescriptor("L$internalName;")

        @JvmStatic
        fun of(clz: Class<*>): ReferenceType = fromDescriptor(BackingType.getDescriptor(clz))

        @JvmSynthetic
        inline operator fun <reified T : Any> invoke(): ReferenceType = of(T::class.java)
    }

    override val descriptor: String = delegate.descriptor

    override val className: String = delegate.className

    override val simpleName: String
        get() = className.substringAfterLast('.')

    /**
     * Returns the internal name of the class corresponding to `this` type.
     *
     * The internal name of a type is its fully qualified name *(as returned by [className])* with `.` replaced with
     * `/`.
     */
    override val internalName: String = delegate.internalName

    override val isValidFieldType: Boolean
        get() = true

    /**
     * Returns `true` if `this` object type is the boxed variant of a [primitive type][PrimitiveType], otherwise
     * `false`.
     */
    val isBoxedPrimitive: Boolean
        get() = toPrimitive() != null

    /**
     * Returns the primitive type of `this` type if it's a boxed type, or itself if it's already a primitive, or
     * `null` if there exists no primitive type for `this` type.
     */
    fun toPrimitive(): PrimitiveType? = when (this) {
        VOID -> PrimitiveType.VOID
        BOOLEAN -> PrimitiveType.BOOLEAN
        CHAR -> PrimitiveType.CHAR
        BYTE -> PrimitiveType.BYTE
        SHORT -> PrimitiveType.SHORT
        INT -> PrimitiveType.INT
        LONG -> PrimitiveType.LONG
        FLOAT -> PrimitiveType.FLOAT
        DOUBLE -> PrimitiveType.DOUBLE
        else -> null
    }

    /**
     * Returns the class corresponding to the [className] of `this` type.
     *
     * @throws [ClassNotFoundException] if no class could be found for the [className] of `this` type
     *
     * @see [Class.forName]
     */
    @Throws(ClassNotFoundException::class)
    override fun toClass(): Class<*> = Class.forName(className)

    /**
     * Returns the class corresponding to the [className] of `this` type, loaded with the given [loader].
     *
     * @throws [ClassNotFoundException] if no class could be found for the [className] of `this` type
     *
     * @see [Class.forName]
     */
    @Throws(ClassNotFoundException::class)
    fun toClass(loader: ClassLoader): Class<*> = Class.forName(className, false, loader)
}

/**
 * Represents an array type containing a [FieldType], i.e; `java.lang.String[]`, `int[]`, `java.lang.Byte[][]`.
 */
class ArrayType private constructor(override val delegate: BackingType) : FieldType(), TypeWithInternalName {
    companion object {
        private val cachedTypes: MutableMap<String, ArrayType> = hashMapOf()

        // -- PRIMITIVE ARRAYS -- \\
        @JvmField
        val VOID: ArrayType = createConstant("[V")

        @JvmField
        val BOOLEAN: ArrayType = createConstant("[Z")

        @JvmField
        val CHAR: ArrayType = createConstant("[C")

        @JvmField
        val BYTE: ArrayType = createConstant("[B")

        @JvmField
        val SHORT: ArrayType = createConstant("[S")

        @JvmField
        val INT: ArrayType = createConstant("[I")

        @JvmField
        val LONG: ArrayType = createConstant("[J")

        @JvmField
        val FLOAT: ArrayType = createConstant("[F")

        @JvmField
        val DOUBLE: ArrayType = createConstant("[D")

        @JvmField
        val VOID_WRAPPER: ArrayType = createConstant("[Ljava/lang/Void;")

        @JvmField
        val BOOLEAN_WRAPPER: ArrayType = createConstant("[Ljava/lang/Boolean;")

        @JvmField
        val CHAR_WRAPPER: ArrayType = createConstant("[Ljava/lang/Character;")

        @JvmField
        val BYTE_WRAPPER: ArrayType = createConstant("[Ljava/lang/Byte;")

        @JvmField
        val SHORT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Short;")

        @JvmField
        val INT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Integer;")

        @JvmField
        val LONG_WRAPPER: ArrayType = createConstant("[Ljava/lang/Long;")

        @JvmField
        val FLOAT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Float;")

        @JvmField
        val DOUBLE_WRAPPER: ArrayType = createConstant("[Ljava/lang/Double;")

        @JvmField
        val OBJECT: ArrayType = createConstant("[Ljava/lang/Object;")

        @JvmField
        val STRING: ArrayType = createConstant("[Ljava/lang/String;")

        @JvmField
        val NUMBER: ArrayType = createConstant("[Ljava/lang/Number;")

        private fun createConstant(descriptor: String): ArrayType {
            val type = ArrayType(BackingType.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        @JvmStatic
        fun copyOf(type: BackingType): ArrayType {
            requireSort(type, BackingType.ARRAY)
            return cachedTypes.getOrPut(type.descriptor) { ArrayType(type) }
        }

        @JvmStatic
        fun fromDescriptor(descriptor: String): ArrayType = when (descriptor) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> copyOf(BackingType.getType(descriptor))
        }

        // TODO: do some stuff to properly cache this one too?
        @JvmStatic
        fun fromInternal(internalName: String): ArrayType =
            copyOf(BackingType.getObjectType(internalName))

        @JvmStatic
        fun of(clz: Class<*>): ArrayType = when {
            clz.isArray -> copyOf(BackingType.getType(clz))
            else -> copyOf(BackingType.getType("[${BackingType.getDescriptor(clz)}"))
        }

        @JvmStatic
        fun of(clz: Class<*>, dimensions: Int): ArrayType {
            require(dimensions >= 1) { "'dimensions' must be 1 or greater, was $dimensions." }
            val descriptor = buildString {
                repeat(dimensions) { append('[') }
                append(BackingType.getDescriptor(clz))
            }

            return fromDescriptor(descriptor)
        }

        @JvmStatic
        fun of(type: FieldType): ArrayType =
            fromDescriptor("[${type.descriptor}")

        @JvmStatic
        fun of(type: FieldType, dimensions: Int): ArrayType {
            require(dimensions >= 1) { "'dimensions' must be 1 or greater, was $dimensions." }
            val descriptor = buildString {
                repeat(dimensions) { append('[') }
                append(type.descriptor)
            }

            return fromDescriptor(descriptor)
        }

        @JvmSynthetic
        inline operator fun <reified T : Any> invoke(): ArrayType = of(T::class.java)

        @JvmSynthetic
        inline operator fun <reified T : Any> invoke(dimensions: Int): ArrayType = of(T::class.java, dimensions)
    }

    override val descriptor: String = delegate.descriptor

    override val className: String = delegate.className

    override val simpleName: String
        get() = elementType.className.substringAfterLast('.')

    /**
     * Returns the internal name of the class corresponding to `this` type.
     *
     * The internal name of a type is its fully qualified name *(as returned by [className])* with `.` replaced with
     * `/`.
     */
    override val internalName: String = delegate.internalName

    /**
     * The amount of dimensions `this` array type has.
     */
    val dimensions: Int = delegate.dimensions

    /**
     * Returns `true` if `this` array type only has `1` [dimension][dimensions], otherwise `false`.
     */
    val isShallow: Boolean
        get() = dimensions == 1

    /**
     * The type of the elements of `this` array type.
     */
    val elementType: FieldType = FieldType.copyOf(delegate.elementType)

    override val isValidFieldType: Boolean
        get() = true

    // TODO: does this work like it should?
    /**
     * Returns a class representation of `this` array type.
     *
     * @throws [ClassNotFoundException] if the class for [elementType] could not be found
     */
    @Throws(ClassNotFoundException::class)
    override fun toClass(): Class<*> = ArrayReflection.newInstance(elementType.toClass(), 0).javaClass

    /**
     * Returns a class representation of `this` array type, loaded using the given [loader].
     *
     * Note that `loader` is only used if [elementType] is a [ReferenceType] or an [ArrayType].
     *
     * @throws [ClassNotFoundException] if the class for [elementType] could not be found
     */
    @Throws(ClassNotFoundException::class)
    fun toClass(loader: ClassLoader): Class<*> {
        val clz = when (elementType) {
            is PrimitiveType -> elementType.toClass()
            is ReferenceType -> elementType.toClass(loader)
            is ArrayType -> elementType.toClass(loader)
        }

        return ArrayReflection.newInstance(clz, 0).javaClass
    }
}

private val sortNames: List<String> =
    listOf("void", "boolean", "char", "byte", "short", "int", "float", "long", "double", "array", "object", "method")

private fun requireSort(type: BackingType, requiredSort: Int) {
    require(type.sort == requiredSort) { "'type' must be a ${sortNames[requiredSort]} type, was a ${sortNames[type.sort]} type." }
}

/**
 * Returns a type representing the type of `this` class.
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun Class<*>.toReferenceType(): ReferenceType = ReferenceType.of(this)

/**
 * Returns a type representing the method-type of `this` method.
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun Method.toMethodType(): MethodType = MethodType.of(this)

/**
 * Returns a type representing the method-type of `this` constructor.
 *
 * The `return type` of a constructor will *always* be [void][PrimitiveVoid].
 *
 * @see [BackingType.getType]
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun Constructor<*>.toMethodType(): MethodType = MethodType.of(this)

/**
 * Returns a type based on `this` type but with the type of the argument at the given [index] changed to [T].
 */
@JvmSynthetic
inline fun <reified T : Any> MethodType.changeArgument(index: Int): MethodType =
    this.changeArgument(index, FieldType<T>())

/**
 * Returns a type based on `this` type but with the [return type][MethodType.returnType] changed to [T].
 */
@JvmSynthetic
inline fun <reified T : Any> MethodType.changeReturn(): MethodType = changeReturn(FieldType<T>())