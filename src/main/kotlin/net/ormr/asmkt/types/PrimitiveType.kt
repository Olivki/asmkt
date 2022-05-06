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

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "unused")

package net.ormr.asmkt.types

import org.objectweb.asm.Type
import java.util.*
import java.lang.Boolean as JBoolean
import java.lang.Byte as JByte
import java.lang.Character as JChar
import java.lang.Double as JDouble
import java.lang.Float as JFloat
import java.lang.Integer as JInt
import java.lang.Long as JLong
import java.lang.Short as JShort
import java.lang.Void as JVoid
import kotlin.Boolean as KBoolean

/**
 * Represents a primitive type, i.e; `byte`, `void`, `double`, etc..
 */
public sealed class PrimitiveType : FieldType() {
    public companion object {
        /**
         * Returns a list of all the known primitive types.
         */
        public val primitives: List<PrimitiveType>
            get() = Collections.unmodifiableList(listOf(Void, Boolean, Char, Byte, Short, Int, Long, Float, Double))

        public fun copyOf(type: Type): PrimitiveType = when (type.sort) {
            Type.VOID -> Void
            Type.BOOLEAN -> Boolean
            Type.CHAR -> Char
            Type.BYTE -> Byte
            Type.SHORT -> Short
            Type.INT -> Int
            Type.LONG -> Long
            Type.FLOAT -> Float
            Type.DOUBLE -> Double
            else -> throw IllegalArgumentException("'type' must be a primitive type, was a ${sortNames[type.sort]} type.")
        }

        public fun fromDescriptor(descriptor: String): PrimitiveType = when (descriptor) {
            "V" -> Void
            "Z" -> Boolean
            "C" -> Char
            "B" -> Byte
            "S" -> Short
            "I" -> Int
            "J" -> Long
            "F" -> Float
            "D" -> Double
            else -> throw IllegalArgumentException("'descriptor' ($descriptor) is not a valid primitive type descriptor.")
        }

        // TODO: documentation
        public fun of(clz: Class<*>): PrimitiveType {
            require(clz.isPrimitive || clz.isPrimitiveWrapper) { "'clz' must be a primitive or a primitive wrapper, was '${clz.name}'." }
            return when {
                clz.isPrimitive -> copyOf(Type.getType(clz))
                clz.isPrimitiveWrapper -> fromDescriptor(Type.getDescriptor(clz.primitiveClass))
                else -> throw IllegalStateException("Exhaustive 'when' was not exhaustive, for $clz.")
            }
        }
    }

    abstract override val descriptor: String

    abstract override val delegate: Type

    override val simpleName: String
        get() = className

    abstract override val isValidFieldType: KBoolean

    /**
     * Returns the boxed [object type][ReferenceType] of `this` primitive type.
     */
    public abstract fun toBoxed(): ReferenceType

    /**
     * Returns the value stored in the `TYPE` field of the boxed type of the primitive type that `this` type represents.
     *
     * This implementation does *not* throw any [ClassNotFoundException]s as no class lookup is performed.
     */
    abstract override fun toClass(): Class<*>

    /**
     * Represents the primitive `void` type.
     */
    public object Void : PrimitiveType() {
        override val descriptor: String
            get() = "V"

        override val className: String
            get() = "void"

        override val delegate: Type
            get() = Type.VOID_TYPE

        override val isValidFieldType: KBoolean
            get() = false

        override fun toBoxed(): ReferenceType = ReferenceType.VOID

        override fun toClass(): Class<*> = JVoid.TYPE
    }

    /**
     * Represents the primitive `boolean` type.
     */
    public object Boolean : PrimitiveType() {
        override val descriptor: String
            get() = "Z"

        override val className: String
            get() = "boolean"

        override val delegate: Type
            get() = Type.BOOLEAN_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.BOOLEAN

        override fun toClass(): Class<*> = JBoolean.TYPE
    }

    /**
     * Represents the primitive `char` type.
     */
    public object Char : PrimitiveType() {
        override val descriptor: String
            get() = "C"

        override val className: String
            get() = "char"

        override val delegate: Type
            get() = Type.CHAR_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.CHAR

        override fun toClass(): Class<*> = JChar.TYPE
    }

    /**
     * Represents the primitive `byte` type.
     */
    public object Byte : PrimitiveType() {
        override val descriptor: String
            get() = "B"

        override val className: String
            get() = "byte"

        override val delegate: Type
            get() = Type.BYTE_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.BYTE

        override fun toClass(): Class<*> = JByte.TYPE
    }

    /**
     * Represents the primitive `short` type.
     */
    public object Short : PrimitiveType() {
        override val descriptor: String
            get() = "S"

        override val className: String
            get() = "short"

        override val delegate: Type
            get() = Type.SHORT_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.SHORT

        override fun toClass(): Class<*> = JShort.TYPE
    }

    /**
     * Represents the primitive `int` type.
     */
    public object Int : PrimitiveType() {
        override val descriptor: String
            get() = "I"

        override val className: String
            get() = "int"

        override val delegate: Type
            get() = Type.INT_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.INT

        override fun toClass(): Class<*> = JInt.TYPE
    }

    /**
     * Represents the primitive `long` type.
     */
    public object Long : PrimitiveType() {
        override val descriptor: String
            get() = "J"

        override val className: String
            get() = "long"

        override val delegate: Type
            get() = Type.LONG_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.LONG

        override fun toClass(): Class<*> = JLong.TYPE
    }

    /**
     * Represents the primitive `float` type.
     */
    public object Float : PrimitiveType() {
        override val descriptor: String
            get() = "F"

        override val className: String
            get() = "float"

        override val delegate: Type
            get() = Type.FLOAT_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.FLOAT

        override fun toClass(): Class<*> = JFloat.TYPE
    }

    /**
     * Represents the primitive `double` type.
     */
    public object Double : PrimitiveType() {
        override val descriptor: String
            get() = "D"

        override val className: String
            get() = "double"

        override val delegate: Type
            get() = Type.DOUBLE_TYPE

        override val isValidFieldType: KBoolean
            get() = true

        override fun toBoxed(): ReferenceType = ReferenceType.DOUBLE

        override fun toClass(): Class<*> = JDouble.TYPE
    }
}

public inline fun <reified T : Any> PrimitiveType(): PrimitiveType = PrimitiveType.of(T::class.java)