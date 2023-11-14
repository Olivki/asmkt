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
public sealed interface AnnotationValue {
    public fun asString(): String

    /**
     * Represents a string value.
     *
     * @property [value] The string value.
     */
    public data class ForString(val value: String) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "\"$value\""
    }

    /**
     * Represents a boolean value.
     *
     * @property [value] The boolean value.
     */
    public data class ForBoolean(val value: Boolean) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = value.toString()
    }

    /**
     * Represents a char value.
     *
     * @property [value] The char value.
     */
    public data class ForChar(val value: Char) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "'$value'"
    }

    /**
     * Represents a byte value.
     *
     * @property [value] The byte value.
     */
    public data class ForByte(val value: Byte) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${value}B"
    }

    /**
     * Represents a short value.
     *
     * @property [value] The short value.
     */
    public data class ForShort(val value: Short) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${value}S"
    }

    /**
     * Represents an int value.
     *
     * @property [value] The int value.
     */
    public data class ForInt(val value: Int) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = value.toString()
    }

    /**
     * Represents a long value.
     *
     * @property [value] The long value.
     */
    public data class ForLong(val value: Long) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${value}L"
    }

    /**
     * Represents a float value.
     *
     * @property [value] The float value.
     */
    public data class ForFloat(val value: Float) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${value}F"
    }

    /**
     * Represents a double value.
     *
     * @property [value] The double value.
     */
    public data class ForDouble(val value: Double) : AnnotationValue, AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${value}D"
    }

    /**
     * Represents a class value.
     *
     * @property [value] The class value.
     */
    public data class ForClass(val value: TypeWithInternalName) : AnnotationValue, AnnotationArrayValue,
        AnnotationDefaultValue {
        override fun asString(): String = value.asString()
    }

    /**
     * Represents an enum value.
     *
     * @property [type] The type of the enum.
     * @property [entryName] The name of the enum entry.
     */
    public data class ForEnum<E : Enum<E>>(val type: ReferenceType, val entryName: String) : AnnotationValue,
        AnnotationArrayValue, AnnotationDefaultValue {
        override fun asString(): String = "${type.asString()}.$entryName"
    }

    /**
     * Represents an array of values.
     *
     * @property [value] The array of values.
     */
    public data class ForArray<T : AnnotationArrayValue>(val value: List<T>) : AnnotationValue, AnnotationDefaultValue {
        override fun asString(): String = value.joinToString(prefix = "[", postfix = "]") { it.asString() }
    }

    /**
     * Represents an annotation builder value.
     *
     * @property [value] The annotation builder.
     */
    public data class ForBuilder(val value: ElementAnnotationBuilder) : AnnotationValue, AnnotationArrayValue,
        AnnotationDefaultValue {
        override fun asString(): String = value.toString()
    }

    /**
     * Represents an annotation array builder value.
     *
     * @property [value] The annotation array builder.
     */
    public data class ForArrayBuilder<T : AnnotationArrayValue>(val value: AnnotationArrayBuilder<T>) :
        AnnotationValue {
        override fun asString(): String = value.toString()
    }
}

/**
 * Represents a value that can be used in a annotation array property value.
 */
public sealed interface AnnotationArrayValue : AnnotationValue

/**
 * Represents a value that can be used as the default for an annotation property.
 */
public sealed interface AnnotationDefaultValue : AnnotationValue