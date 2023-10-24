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

package net.ormr.asmkt.type

import net.ormr.asmkt.AsmKtReflection
import java.lang.reflect.Array as ArrayReflection

/**
 * Represents an array type.
 */
public class ArrayType private constructor(private val delegate: AsmType) : FieldType,
    TypeWithInternalName {
    override val descriptor: String = delegate.descriptor

    override val name: String = delegate.className

    override val simpleName: String
        get() = name.substringAfterLast('.')

    override val internalName: String = delegate.internalName

    /**
     * The type of the elements of the array.
     */
    public val elementType: FieldType = delegate.elementType.toFieldType()

    /**
     * The amount of dimensions the array has.
     */
    public val dimensions: Int = delegate.dimensions

    override fun asAsmType(): AsmType = delegate

    override fun asString(): String = "${elementType.asString()}${"[]".repeat(dimensions)}"

    /**
     * Returns the class corresponding to the [name] of this type.
     *
     * @see [Class.forName]
     */
    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> =
        ArrayReflection.newInstance(elementType.getOrLoadClass(), dimensions).javaClass

    /**
     * Returns the class corresponding to the [name] of this type, loaded with the given [loader].
     *
     * @see [Class.forName]
     */
    @AsmKtReflection
    public fun getOrLoadClass(loader: ClassLoader): Class<*> {
        val loadedClass = when (elementType) {
            is ReferenceType -> elementType.getOrLoadClass(loader)
            else -> elementType.getOrLoadClass()
        }
        return ArrayReflection.newInstance(loadedClass, dimensions).javaClass
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ArrayType -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "ArrayTypeDesc(elementType=$elementType, dimensions=$dimensions)"

    public companion object {
        // needs to be declared before the constants, or we'll get a NPE
        private val constants = hashMapOf<String, ArrayType>()

        // -- PRIMITIVES -- \\
        /**
         * An array of `boolean` types.
         *
         * Descriptor: `[Z`
         */
        public val BOOLEAN: ArrayType = create("[Z")

        /**
         * An array of `char` types.
         *
         * Descriptor: `[C`
         */
        public val CHAR: ArrayType = create("[C")

        /**
         * An array of `byte` types.
         *
         * Descriptor: `[B`
         */
        public val BYTE: ArrayType = create("[B")

        /**
         * An array of `short` types.
         *
         * Descriptor: `[S`
         */
        public val SHORT: ArrayType = create("[S")

        /**
         * An array of `int` types.
         *
         * Descriptor: `[I`
         */
        public val INT: ArrayType = create("[I")

        /**
         * An array of `long` types.
         *
         * Descriptor: `[J`
         */
        public val LONG: ArrayType = create("[J")

        /**
         * An array of `float` types.
         *
         * Descriptor: `[F`
         */
        public val FLOAT: ArrayType = create("[F")

        /**
         * An array of `double` types.
         *
         * Descriptor: `[D`
         */
        public val DOUBLE: ArrayType = create("[D")

        // -- PRIMITIVE WRAPPERS -- \\
        /**
         * An array of `java.lang.Boolean` types.
         *
         * Descriptor: `[Ljava/lang/Boolean;`
         */
        public val BOOLEAN_WRAPPER: ArrayType = create("[Ljava/lang/Boolean;")

        /**
         * An array of `java.lang.Character` types.
         *
         * Descriptor: `[Ljava/lang/Character;`
         */
        public val CHAR_WRAPPER: ArrayType = create("[Ljava/lang/Character;")

        /**
         * An array of `java.lang.Byte` types.
         *
         * Descriptor: `[Ljava/lang/Byte;`
         */
        public val BYTE_WRAPPER: ArrayType = create("[Ljava/lang/Byte;")

        /**
         * An array of `java.lang.Short` types.
         *
         * Descriptor: `[Ljava/lang/Short;`
         */
        public val SHORT_WRAPPER: ArrayType = create("[Ljava/lang/Short;")

        /**
         * An array of `java.lang.Integer` types.
         *
         * Descriptor: `[Ljava/lang/Integer;`
         */
        public val INT_WRAPPER: ArrayType = create("[Ljava/lang/Integer;")

        /**
         * An array of `java.lang.Long` types.
         *
         * Descriptor: `[Ljava/lang/Long;`
         */
        public val LONG_WRAPPER: ArrayType = create("[Ljava/lang/Long;")

        /**
         * An array of `java.lang.Float` types.
         *
         * Descriptor: `[Ljava/lang/Float;`
         */
        public val FLOAT_WRAPPER: ArrayType = create("[Ljava/lang/Float;")

        /**
         * An array of `java.lang.Double` types.
         *
         * Descriptor: `[Ljava/lang/Double;`
         */
        public val DOUBLE_WRAPPER: ArrayType = create("[Ljava/lang/Double;")

        // -- OTHERS -- \\
        /**
         * An array of `java.lang.Object` types.
         *
         * Descriptor: `[Ljava/lang/Object;`
         */
        public val OBJECT: ArrayType = create("[Ljava/lang/Object;")

        /**
         * An array of `java.lang.String` types.
         *
         * Descriptor: `[Ljava/lang/String;`
         */
        public val STRING: ArrayType = create("[Ljava/lang/String;")

        /**
         * An array of `java.lang.Number` types.
         *
         * Descriptor: `[Ljava/lang/Number;`
         */
        public val NUMBER: ArrayType = create("[Ljava/lang/Number;")

        private fun create(descriptor: String): ArrayType {
            val type = ArrayType(AsmType.getType(descriptor))
            constants[descriptor] = type
            return type
        }

        /**
         * Returns an [ArrayType] wrapped around the given [type].
         *
         * @throws [IllegalArgumentException] if the given [type] is not an array type
         */
        public fun copyOf(type: AsmType): ArrayType {
            require(type.isArray) { "Type (${type.asString()}) is not an array type" }
            return constants[type.descriptor] ?: ArrayType(type)
        }

        public fun fromDescriptor(descriptor: String): ArrayType =
            constants[descriptor] ?: copyOf(AsmType.getType(descriptor))

        public fun fromInternal(internalName: String): ArrayType =
            copyOf(AsmType.getObjectType(internalName))

        public fun fromClass(clz: Class<*>, dimensions: Int = 1): ArrayType {
            require(dimensions > 0) { "Dimensions ($dimensions) < 0" }
            val descriptor = "${"[".repeat(dimensions)}${AsmType.getDescriptor(clz)}"
            return fromDescriptor(descriptor)
        }

        public fun of(type: FieldType, dimensions: Int = 1): ArrayType {
            require(dimensions > 0) { "Dimensions ($dimensions) < 0" }
            val descriptor = "${"[".repeat(dimensions)}${type.descriptor}"
            return fromDescriptor(descriptor)
        }
    }
}

public fun FieldType.toArrayType(dimensions: Int = 1): ArrayType = ArrayType.of(this, dimensions)

public fun ArrayType(elementType: FieldType, dimensions: Int = 1): ArrayType =
    ArrayType.of(elementType, dimensions)

public inline fun <reified T : Any> ArrayType(dimensions: Int = 1): ArrayType =
    ArrayType.fromClass(T::class.java, dimensions)