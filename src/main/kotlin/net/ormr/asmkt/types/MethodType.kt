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

@file:Suppress("unused")

package net.ormr.asmkt.types

import java.lang.invoke.CallSite
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import org.objectweb.asm.Type as AsmType
import java.lang.invoke.MethodType as JMethodType

/**
 * Represents the type of method.
 */
class MethodType private constructor(override val delegate: AsmType) : Type() {
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

        fun ofVoid(vararg argumentTypes: FieldType): MethodType = VOID.appendArguments(argumentTypes.asIterable())

        fun ofBoolean(vararg argumentTypes: FieldType): MethodType =
            BOOLEAN.appendArguments(argumentTypes.asIterable())

        fun ofChar(vararg argumentTypes: FieldType): MethodType = CHAR.appendArguments(argumentTypes.asIterable())

        fun ofByte(vararg argumentTypes: FieldType): MethodType = BYTE.appendArguments(argumentTypes.asIterable())

        fun ofShort(vararg argumentTypes: FieldType): MethodType = SHORT.appendArguments(argumentTypes.asIterable())

        fun ofInt(vararg argumentTypes: FieldType): MethodType = INT.appendArguments(argumentTypes.asIterable())

        fun ofLong(vararg argumentTypes: FieldType): MethodType = LONG.appendArguments(argumentTypes.asIterable())

        fun ofFloat(vararg argumentTypes: FieldType): MethodType = FLOAT.appendArguments(argumentTypes.asIterable())

        fun ofDouble(vararg argumentTypes: FieldType): MethodType =
            DOUBLE.appendArguments(argumentTypes.asIterable())

        fun ofVoidWrapper(vararg argumentTypes: FieldType): MethodType =
            VOID_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofBooleanWrapper(vararg argumentTypes: FieldType): MethodType =
            BOOLEAN_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofCharWrapper(vararg argumentTypes: FieldType): MethodType =
            CHAR_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofByteWrapper(vararg argumentTypes: FieldType): MethodType =
            BYTE_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofShortWrapper(vararg argumentTypes: FieldType): MethodType =
            SHORT_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofIntWrapper(vararg argumentTypes: FieldType): MethodType =
            INT_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofLongWrapper(vararg argumentTypes: FieldType): MethodType =
            LONG_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofFloatWrapper(vararg argumentTypes: FieldType): MethodType =
            FLOAT_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofDoubleWrapper(vararg argumentTypes: FieldType): MethodType =
            DOUBLE_WRAPPER.appendArguments(argumentTypes.asIterable())

        fun ofObject(vararg argumentTypes: FieldType): MethodType =
            OBJECT.appendArguments(argumentTypes.asIterable())

        fun ofString(vararg argumentTypes: FieldType): MethodType =
            STRING.appendArguments(argumentTypes.asIterable())

        fun ofStringBuilder(vararg argumentTypes: FieldType): MethodType =
            STRING_BUILDER.appendArguments(argumentTypes.asIterable())

        fun ofNumber(vararg argumentTypes: FieldType): MethodType =
            NUMBER.appendArguments(argumentTypes.asIterable())

        // -- FACTORY FUNCTIONS -- \\
        private fun createConstant(descriptor: String): MethodType {
            val type = MethodType(AsmType.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        fun copyOf(type: AsmType): MethodType {
            requireSort(type, AsmType.METHOD)
            return when (val descriptor = type.descriptor) {
                in cachedTypes -> cachedTypes.getValue(descriptor)
                else -> MethodType(type)
            }
        }

        fun of(method: Method): MethodType = when (val descriptor = method.toDescriptorString()) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> MethodType(AsmType.getType(method))
        }

        fun of(constructor: Constructor<*>): MethodType = when (val descriptor = constructor.toDescriptorString()) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> MethodType(AsmType.getType(constructor))
        }

        fun of(methodType: JMethodType): MethodType = fromDescriptor(methodType.toMethodDescriptorString())

        fun fromDescriptor(descriptor: String): MethodType = copyOf(AsmType.getMethodType(descriptor))

        // TODO: documentation
        fun forBootstrap(returnType: FieldType = ReferenceType<CallSite>()): MethodType = of(
            returnType,
            ReferenceType.METHOD_HANDLES_LOOKUP,
            ReferenceType.STRING,
            ReferenceType.METHOD_TYPE,
            ArrayType.OBJECT,
        )

        fun of(
            returnType: FieldType,
            vararg typeParameters: FieldType,
        ): MethodType = fromDescriptor(buildMethodDescriptor(returnType, typeParameters))

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
    val argumentTypes: List<FieldType> =
        Collections.unmodifiableList(delegate.argumentTypes.map { FieldType.copyOf(it) })

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

fun MethodType(
    returnType: FieldType,
    vararg typeParameters: FieldType,
): MethodType = MethodType.fromDescriptor(buildMethodDescriptor(returnType, typeParameters))