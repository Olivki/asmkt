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

package net.ormr.asmkt

import net.ormr.asmkt.type.ReferenceType
import net.ormr.asmkt.type.TypeWithInternalName

/**
 * Represents a value that can be used in for an annotation property.
 */
public sealed interface AnnotationElementValue {
    public fun asString(): String

    /**
     * Represents a string value.
     *
     * @property [value] The string value.
     */
    public data class ForString(val value: String) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "\"$value\""
    }

    /**
     * Represents a boolean value.
     *
     * @property [value] The boolean value.
     */
    public data class ForBoolean(val value: Boolean) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.toString()
    }

    /**
     * Represents a char value.
     *
     * @property [value] The char value.
     */
    public data class ForChar(val value: Char) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "'$value'"
    }

    /**
     * Represents a byte value.
     *
     * @property [value] The byte value.
     */
    public data class ForByte(val value: Byte) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "${value}B"
    }

    /**
     * Represents a short value.
     *
     * @property [value] The short value.
     */
    public data class ForShort(val value: Short) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "${value}S"
    }

    /**
     * Represents an int value.
     *
     * @property [value] The int value.
     */
    public data class ForInt(val value: Int) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.toString()
    }

    /**
     * Represents a long value.
     *
     * @property [value] The long value.
     */
    public data class ForLong(val value: Long) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "${value}L"
    }

    /**
     * Represents a float value.
     *
     * @property [value] The float value.
     */
    public data class ForFloat(val value: Float) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "${value}F"
    }

    /**
     * Represents a double value.
     *
     * @property [value] The double value.
     */
    public data class ForDouble(val value: Double) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = "${value}D"
    }

    /**
     * Represents a class value.
     *
     * @property [value] The class value.
     */
    public data class ForClass(val value: TypeWithInternalName) : AnnotationElementValue, AnnotationElementArrayValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.asString()
    }

    /**
     * Represents an enum value.
     *
     * @property [type] The type of the enum.
     * @property [entryName] The name of the enum entry.
     */
    @Suppress("unused")
    public data class ForEnum<E : Enum<E>>(val type: ReferenceType, val entryName: String) : AnnotationElementValue,
        AnnotationElementArrayValue, AnnotationElementDefaultValue {
        override fun asString(): String = "${type.asString()}.$entryName"
    }

    /**
     * Represents an annotation element value.
     *
     * @property [value] The annotation element.
     */
    public data class ForAnnotation(val value: ChildAnnotationElement) : AnnotationElementValue,
        AnnotationElementArrayValue, AnnotationElementDefaultValue {
        override fun asString(): String = value.toString()
    }

    // -- ARRAYS -- \\
    /**
     * Represents an array of values.
     *
     * @property [value] The list of values.
     */
    public data class ForArray<T : AnnotationElementArrayValue>(val value: List<T>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { it.asString() }
    }

    /**
     * Represents an array of booleans.
     *
     * @property [value] The list of booleans.
     */
    public data class ForBooleanArray(val value: List<Boolean>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { it.toString() }
    }

    /**
     * Represents an array of chars.
     *
     * @property [value] The list of chars.
     */
    public data class ForCharArray(val value: List<Char>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "'$it'" }
    }

    /**
     * Represents an array of bytes.
     *
     * @property [value] The list of bytes.
     */
    public data class ForByteArray(val value: List<Byte>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "${it}B" }
    }

    /**
     * Represents an array of shorts.
     *
     * @property [value] The list of shorts.
     */
    public data class ForShortArray(val value: List<Short>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "${it}S" }
    }

    /**
     * Represents an array of ints.
     *
     * @property [value] The list of ints.
     */
    public data class ForIntArray(val value: List<Int>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]")
    }

    /**
     * Represents an array of longs.
     *
     * @property [value] The list of longs.
     */
    public data class ForLongArray(val value: List<Long>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "${it}L" }
    }

    /**
     * Represents an array of floats.
     *
     * @property [value] The list of floats.
     */
    public data class ForFloatArray(val value: List<Float>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "${it}F" }
    }

    /**
     * Represents an array of doubles.
     *
     * @property [value] The list of doubles.
     */
    public data class ForDoubleArray(val value: List<Double>) : AnnotationElementValue,
        AnnotationElementDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { "${it}D" }
    }
}

/**
 * Represents a value that can be used in a annotation array property value.
 */
public sealed interface AnnotationElementArrayValue : AnnotationElementValue

/**
 * Represents a value that can be used as the default for an annotation property.
 */
public sealed interface AnnotationElementDefaultValue : AnnotationElementValue