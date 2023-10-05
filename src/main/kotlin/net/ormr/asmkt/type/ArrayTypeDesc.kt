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

public class ArrayTypeDesc private constructor(private val delegate: AsmType) : FieldTypeDesc,
    TypeDescWithInternalName {
    override val descriptor: String = delegate.descriptor

    override val name: String = delegate.className

    override val simpleName: String
        get() = name.substringAfterLast('.')

    override val internalName: String = delegate.internalName

    /**
     * The type of the elements of the array.
     */
    public val elementType: FieldTypeDesc = delegate.elementType.toFieldTypeDesc()

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
            is ReferenceTypeDesc -> elementType.getOrLoadClass(loader)
            else -> elementType.getOrLoadClass()
        }
        return ArrayReflection.newInstance(loadedClass, dimensions).javaClass
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ArrayTypeDesc -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "ArrayTypeDesc(elementType=$elementType, dimensions=$dimensions)"

    public companion object {
        // -- PRIMITIVES -- \\
        public val VOID: ArrayTypeDesc = constant("[V")
        public val BOOLEAN: ArrayTypeDesc = constant("[Z")
        public val CHAR: ArrayTypeDesc = constant("[C")
        public val BYTE: ArrayTypeDesc = constant("[B")
        public val SHORT: ArrayTypeDesc = constant("[S")
        public val INT: ArrayTypeDesc = constant("[I")
        public val LONG: ArrayTypeDesc = constant("[J")
        public val FLOAT: ArrayTypeDesc = constant("[F")
        public val DOUBLE: ArrayTypeDesc = constant("[D")

        // -- PRIMITIVE WRAPPERS -- \\
        public val VOID_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Void;")
        public val BOOLEAN_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Boolean;")
        public val CHAR_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Character;")
        public val BYTE_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Byte;")
        public val SHORT_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Short;")
        public val INT_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Integer;")
        public val LONG_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Long;")
        public val FLOAT_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Float;")
        public val DOUBLE_WRAPPER: ArrayTypeDesc = constant("[Ljava/lang/Double;")

        // -- OTHERS -- \\
        public val OBJECT: ArrayTypeDesc = constant("[Ljava/lang/Object;")
        public val STRING: ArrayTypeDesc = constant("[Ljava/lang/String;")
        public val NUMBER: ArrayTypeDesc = constant("[Ljava/lang/Number;")

        private val constants = hashMapOf<String, ArrayTypeDesc>()

        private fun constant(descriptor: String): ArrayTypeDesc {
            val type = ArrayTypeDesc(AsmType.getType(descriptor))
            constants[descriptor] = type
            return type
        }

        public fun copyOf(type: AsmType): ArrayTypeDesc {
            require(type.isArray) { "Type (${type.asString()}) is not an array type" }
            return constants[type.descriptor] ?: ArrayTypeDesc(type)
        }

        public fun fromDescriptor(descriptor: String): ArrayTypeDesc =
            constants[descriptor] ?: copyOf(AsmType.getType(descriptor))

        public fun fromInternal(internalName: String): ArrayTypeDesc =
            copyOf(AsmType.getObjectType(internalName))

        public fun of(clz: Class<*>, dimensions: Int): ArrayTypeDesc {
            require(dimensions > 0) { "Dimensions ($dimensions) < 0" }
            val descriptor = "${"[".repeat(dimensions)}${AsmType.getDescriptor(clz)}"
            return fromDescriptor(descriptor)
        }

        public fun of(clz: Class<*>): ArrayTypeDesc = when {
            clz.isArray -> copyOf(AsmType.getType(clz))
            else -> copyOf(AsmType.getType("[${AsmType.getDescriptor(clz)}"))
        }

        public fun of(type: FieldTypeDesc, dimensions: Int): ArrayTypeDesc {
            require(dimensions > 0) { "Dimensions ($dimensions) < 0" }
            val descriptor = "${"[".repeat(dimensions)}${type.descriptor}"
            return fromDescriptor(descriptor)
        }

        public fun of(type: FieldTypeDesc): ArrayTypeDesc = fromDescriptor("[${type.descriptor}")
    }
}

public fun FieldTypeDesc.toArrayType(): ArrayTypeDesc = ArrayTypeDesc.of(this)

public fun FieldTypeDesc.toArrayType(dimensions: Int): ArrayTypeDesc = ArrayTypeDesc.of(this, dimensions)

public fun ArrayTypeDesc(elementType: FieldTypeDesc, dimensions: Int): ArrayTypeDesc =
    ArrayTypeDesc.of(elementType, dimensions)

public fun ArrayTypeDesc(elementType: FieldTypeDesc): ArrayTypeDesc = ArrayTypeDesc.of(elementType)

public inline fun <reified T : Any> ArrayTypeDesc(dimensions: Int): ArrayTypeDesc =
    ArrayTypeDesc.of(T::class.java, dimensions)

public inline fun <reified T : Any> ArrayTypeDesc(): ArrayTypeDesc = ArrayTypeDesc.of(T::class.java)