/*
 * Copyright 2023 Oliver Berg
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

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package net.ormr.asmkt.type

import net.ormr.asmkt.AsmKtReflection
import java.lang.Boolean as JBoolean
import java.lang.Byte as JByte
import java.lang.Character as JChar
import java.lang.Double as JDouble
import java.lang.Float as JFloat
import java.lang.Integer as JInt
import java.lang.Long as JLong
import java.lang.Short as JShort
import java.lang.Void as JVoid

/**
 * Represents a primitive JVM type.
 */
public sealed interface PrimitiveType : ReturnableType {
    override val simpleName: String
        get() = name

    /**
     * Returns the boxed version of this type.
     *
     * For example, if this type is [BooleanType], then this method will return [ReferenceType.BOOLEAN].
     *
     * @see [ReferenceType.unboxOrNull]
     */
    public fun box(): ReferenceType

    override fun asString(): String = name

    public companion object {
        /**
         * Returns a [PrimitiveType] that corresponds to the given [type], or throws an [IllegalArgumentException] if
         * the given [type] is not a primitive type.
         *
         * @throws [IllegalArgumentException] if the given [type] is not a primitive type.
         */
        public fun copyOf(type: AsmType): PrimitiveType = when (type.sort) {
            AsmType.VOID -> VoidType
            AsmType.BOOLEAN -> BooleanType
            AsmType.CHAR -> CharType
            AsmType.BYTE -> ByteType
            AsmType.SHORT -> ShortType
            AsmType.INT -> IntType
            AsmType.FLOAT -> FloatType
            AsmType.LONG -> LongType
            AsmType.DOUBLE -> DoubleType
            else -> throw IllegalArgumentException("Type (${type.asString()}) is not a primitive type")
        }

        /**
         * Returns a [PrimitiveType] that corresponds to the given [descriptor], or throws an [IllegalArgumentException]
         * if the given [descriptor] is not a primitive type descriptor.
         *
         * Valid primitive type descriptors are:
         * - `V` for `void`
         * - `Z` for `boolean`
         * - `C` for `char`
         * - `B` for `byte`
         * - `S` for `short`
         * - `I` for `int`
         * - `J` for `long`
         * - `F` for `float`
         * - `D` for `double`
         *
         * @param [descriptor] the descriptor that represents the primitive type, must be `"V"`, `"Z"`, `"C"`, `"B"`,
         * `"S"`, `"I"`, `"J"`, `"F"`, or `"D"`.
         *
         * @throws [IllegalArgumentException] if the given [descriptor] is not a primitive type descriptor.
         */
        public fun fromDescriptor(descriptor: String): PrimitiveType = when (descriptor) {
            "V" -> VoidType
            "Z" -> BooleanType
            "C" -> CharType
            "B" -> ByteType
            "S" -> ShortType
            "I" -> IntType
            "J" -> LongType
            "F" -> FloatType
            "D" -> DoubleType
            else -> throw IllegalArgumentException("Unknown primitive type descriptor ($descriptor)")
        }
    }
}

/**
 * Represents a primitive type that can be used as a field type.
 *
 * This is a marker interface, and is only used to differentiate between [PrimitiveType]s that can be used as field types
 * and those that can not *(e.g. [VoidType])*.
 */
public sealed interface PrimitiveFieldType : PrimitiveType, FieldType

/**
 * Represents the `void` primitive type.
 */
public data object VoidType : PrimitiveType {
    override val descriptor: String
        get() = "V"

    override val name: String
        get() = "void"

    override fun asAsmType(): AsmType = AsmType.VOID_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JVoid.TYPE

    override fun box(): ReferenceType = ReferenceType.VOID
}

/**
 * Represents the `boolean` primitive type.
 */
public data object BooleanType : PrimitiveFieldType {
    override val descriptor: String
        get() = "Z"

    override val name: String
        get() = "boolean"

    override fun asAsmType(): AsmType = AsmType.BOOLEAN_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JBoolean.TYPE

    override fun box(): ReferenceType = ReferenceType.BOOLEAN
}

/**
 * Represents the `char` primitive type.
 */
public data object CharType : PrimitiveFieldType {
    override val descriptor: String
        get() = "C"

    override val name: String
        get() = "char"

    override fun asAsmType(): AsmType = AsmType.CHAR_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JChar.TYPE

    override fun box(): ReferenceType = ReferenceType.CHAR
}

/**
 * Represents the `byte` primitive type.
 */
public data object ByteType : PrimitiveFieldType {
    override val descriptor: String
        get() = "B"

    override val name: String
        get() = "byte"

    override fun asAsmType(): AsmType = AsmType.BYTE_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JByte.TYPE

    override fun box(): ReferenceType = ReferenceType.BYTE
}

/**
 * Represents the `short` primitive type.
 */
public data object ShortType : PrimitiveFieldType {
    override val descriptor: String
        get() = "S"

    override val name: String
        get() = "short"

    override fun asAsmType(): AsmType = AsmType.SHORT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JShort.TYPE

    override fun box(): ReferenceType = ReferenceType.SHORT
}

/**
 * Represents the `int` primitive type.
 */
public data object IntType : PrimitiveFieldType {
    override val descriptor: String
        get() = "I"

    override val name: String
        get() = "int"

    override fun asAsmType(): AsmType = AsmType.INT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JInt.TYPE

    override fun box(): ReferenceType = ReferenceType.INT
}

/**
 * Represents the `long` primitive type.
 */
public data object LongType : PrimitiveFieldType {
    override val descriptor: String
        get() = "J"

    override val name: String
        get() = "long"

    override fun asAsmType(): AsmType = AsmType.LONG_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JLong.TYPE

    override fun box(): ReferenceType = ReferenceType.LONG
}

/**
 * Represents the `float` primitive type.
 */
public data object FloatType : PrimitiveFieldType {
    override val descriptor: String
        get() = "F"

    override val name: String
        get() = "float"

    override fun asAsmType(): AsmType = AsmType.FLOAT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JFloat.TYPE

    override fun box(): ReferenceType = ReferenceType.FLOAT
}

/**
 * Represents the `double` primitive type.
 */
public data object DoubleType : PrimitiveFieldType {
    override val descriptor: String
        get() = "D"

    override val name: String
        get() = "double"

    override fun asAsmType(): AsmType = AsmType.DOUBLE_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JDouble.TYPE

    override fun box(): ReferenceType = ReferenceType.DOUBLE
}