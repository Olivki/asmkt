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

public sealed interface PrimitiveTypeDesc : ReturnableTypeDesc {
    override val simpleName: String
        get() = name

    /**
     * Returns the boxed version of this type.
     *
     * For example, if this type is [BooleanTypeDesc], then this method will return [ClassDesc.BOOLEAN].
     *
     * @see [ClassDesc.unboxOrNull]
     */
    public fun box(): ClassDesc

    override fun asString(): String = name

    public companion object {
        public fun copyOf(type: AsmType): PrimitiveTypeDesc = when (type.sort) {
            AsmType.VOID -> VoidTypeDesc
            AsmType.BOOLEAN -> BooleanTypeDesc
            AsmType.CHAR -> CharTypeDesc
            AsmType.BYTE -> ByteTypeDesc
            AsmType.SHORT -> ShortTypeDesc
            AsmType.INT -> IntTypeDesc
            AsmType.FLOAT -> FloatTypeDesc
            AsmType.LONG -> LongTypeDesc
            AsmType.DOUBLE -> DoubleTypeDesc
            else -> throw IllegalArgumentException("Type (${type.asString()}) is not a primitive type")
        }

        public fun fromDescriptor(descriptor: String): PrimitiveTypeDesc = when (descriptor) {
            "V" -> VoidTypeDesc
            "Z" -> BooleanTypeDesc
            "C" -> CharTypeDesc
            "B" -> ByteTypeDesc
            "S" -> ShortTypeDesc
            "I" -> IntTypeDesc
            "J" -> LongTypeDesc
            "F" -> FloatTypeDesc
            "D" -> DoubleTypeDesc
            else -> throw IllegalArgumentException("Unknown primitive type descriptor: '$descriptor'")
        }
    }
}

public sealed interface NonVoidPrimitiveTypeDesc : PrimitiveTypeDesc, FieldTypeDesc

/**
 * Represents the `void` primitive type.
 */
public data object VoidTypeDesc : PrimitiveTypeDesc {
    override val descriptor: String
        get() = "V"

    override val name: String
        get() = "void"

    override fun asAsmType(): AsmType = AsmType.VOID_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JVoid.TYPE

    override fun box(): ClassDesc = ClassDesc.VOID
}

/**
 * Represents the `boolean` primitive type.
 */
public data object BooleanTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "Z"

    override val name: String
        get() = "boolean"

    override fun asAsmType(): AsmType = AsmType.BOOLEAN_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JBoolean.TYPE

    override fun box(): ClassDesc = ClassDesc.BOOLEAN
}

/**
 * Represents the `char` primitive type.
 */
public data object CharTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "C"

    override val name: String
        get() = "char"

    override fun asAsmType(): AsmType = AsmType.CHAR_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JChar.TYPE

    override fun box(): ClassDesc = ClassDesc.CHAR
}

/**
 * Represents the `byte` primitive type.
 */
public data object ByteTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "B"

    override val name: String
        get() = "byte"

    override fun asAsmType(): AsmType = AsmType.BYTE_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JByte.TYPE

    override fun box(): ClassDesc = ClassDesc.BYTE
}

/**
 * Represents the `short` primitive type.
 */
public data object ShortTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "S"

    override val name: String
        get() = "short"

    override fun asAsmType(): AsmType = AsmType.SHORT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JShort.TYPE

    override fun box(): ClassDesc = ClassDesc.SHORT
}

/**
 * Represents the `int` primitive type.
 */
public data object IntTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "I"

    override val name: String
        get() = "int"

    override fun asAsmType(): AsmType = AsmType.INT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JInt.TYPE

    override fun box(): ClassDesc = ClassDesc.INT
}

/**
 * Represents the `long` primitive type.
 */
public data object LongTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "J"

    override val name: String
        get() = "long"

    override fun asAsmType(): AsmType = AsmType.LONG_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JLong.TYPE

    override fun box(): ClassDesc = ClassDesc.LONG
}

/**
 * Represents the `float` primitive type.
 */
public data object FloatTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "F"

    override val name: String
        get() = "float"

    override fun asAsmType(): AsmType = AsmType.FLOAT_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JFloat.TYPE

    override fun box(): ClassDesc = ClassDesc.FLOAT
}

/**
 * Represents the `double` primitive type.
 */
public data object DoubleTypeDesc : NonVoidPrimitiveTypeDesc {
    override val descriptor: String
        get() = "D"

    override val name: String
        get() = "double"

    override fun asAsmType(): AsmType = AsmType.DOUBLE_TYPE

    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = JDouble.TYPE

    override fun box(): ClassDesc = ClassDesc.DOUBLE
}