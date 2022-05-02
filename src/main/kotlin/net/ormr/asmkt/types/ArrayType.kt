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

package net.ormr.asmkt.types

import java.lang.reflect.Array
import org.objectweb.asm.Type as AsmType

/**
 * Represents an array type containing a [FieldType], i.e; `java.lang.String[]`, `int[]`, `java.lang.Byte[][]`.
 */
public class ArrayType private constructor(override val delegate: AsmType) : FieldType(), TypeWithInternalName {
    public companion object {
        private val cachedTypes: MutableMap<String, ArrayType> = hashMapOf()

        // -- PRIMITIVE ARRAYS -- \\
        @JvmField
        public val VOID: ArrayType = createConstant("[V")

        @JvmField
        public val BOOLEAN: ArrayType = createConstant("[Z")

        @JvmField
        public val CHAR: ArrayType = createConstant("[C")

        @JvmField
        public val BYTE: ArrayType = createConstant("[B")

        @JvmField
        public val SHORT: ArrayType = createConstant("[S")

        @JvmField
        public val INT: ArrayType = createConstant("[I")

        @JvmField
        public val LONG: ArrayType = createConstant("[J")

        @JvmField
        public val FLOAT: ArrayType = createConstant("[F")

        @JvmField
        public val DOUBLE: ArrayType = createConstant("[D")

        @JvmField
        public val VOID_WRAPPER: ArrayType = createConstant("[Ljava/lang/Void;")

        @JvmField
        public val BOOLEAN_WRAPPER: ArrayType = createConstant("[Ljava/lang/Boolean;")

        @JvmField
        public val CHAR_WRAPPER: ArrayType = createConstant("[Ljava/lang/Character;")

        @JvmField
        public val BYTE_WRAPPER: ArrayType = createConstant("[Ljava/lang/Byte;")

        @JvmField
        public val SHORT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Short;")

        @JvmField
        public val INT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Integer;")

        @JvmField
        public val LONG_WRAPPER: ArrayType = createConstant("[Ljava/lang/Long;")

        @JvmField
        public val FLOAT_WRAPPER: ArrayType = createConstant("[Ljava/lang/Float;")

        @JvmField
        public val DOUBLE_WRAPPER: ArrayType = createConstant("[Ljava/lang/Double;")

        @JvmField
        public val OBJECT: ArrayType = createConstant("[Ljava/lang/Object;")

        @JvmField
        public val STRING: ArrayType = createConstant("[Ljava/lang/String;")

        @JvmField
        public val NUMBER: ArrayType = createConstant("[Ljava/lang/Number;")

        private fun createConstant(descriptor: String): ArrayType {
            val type = ArrayType(AsmType.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        public fun copyOf(type: AsmType): ArrayType {
            requireSort(type, AsmType.ARRAY)
            return cachedTypes.getOrPut(type.descriptor) { ArrayType(type) }
        }

        public fun fromDescriptor(descriptor: String): ArrayType = when (descriptor) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> copyOf(AsmType.getType(descriptor))
        }

        // TODO: do some stuff to properly cache this one too?
        public fun fromInternal(internalName: String): ArrayType =
            copyOf(AsmType.getObjectType(internalName))

        public fun of(clz: Class<*>): ArrayType = when {
            clz.isArray -> copyOf(AsmType.getType(clz))
            else -> copyOf(AsmType.getType("[${AsmType.getDescriptor(clz)}"))
        }

        public fun of(clz: Class<*>, dimensions: Int): ArrayType {
            require(dimensions >= 1) { "'dimensions' must be 1 or greater, was $dimensions." }
            val descriptor = buildString {
                repeat(dimensions) { append('[') }
                append(AsmType.getDescriptor(clz))
            }

            return fromDescriptor(descriptor)
        }

        public fun of(type: FieldType): ArrayType =
            fromDescriptor("[${type.descriptor}")

        public fun of(type: FieldType, dimensions: Int): ArrayType {
            require(dimensions >= 1) { "'dimensions' must be 1 or greater, was $dimensions." }
            val descriptor = buildString {
                repeat(dimensions) { append('[') }
                append(type.descriptor)
            }

            return fromDescriptor(descriptor)
        }
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
    public val dimensions: Int = delegate.dimensions

    /**
     * Returns `true` if `this` array type only has `1` [dimension][dimensions], otherwise `false`.
     */
    public val isShallow: Boolean
        get() = dimensions == 1

    /**
     * The type of the elements of `this` array type.
     */
    public val elementType: FieldType = FieldType.copyOf(delegate.elementType)

    override val isValidFieldType: Boolean
        get() = true

    // TODO: does this work like it should?
    /**
     * Returns a class representation of `this` array type.
     *
     * @throws [ClassNotFoundException] if the class for [elementType] could not be found
     */
    @Throws(ClassNotFoundException::class)
    override fun toClass(): Class<*> = Array.newInstance(elementType.toClass(), 0).javaClass

    /**
     * Returns a class representation of `this` array type, loaded using the given [loader].
     *
     * Note that `loader` is only used if [elementType] is a [ReferenceType] or an [ArrayType].
     *
     * @throws [ClassNotFoundException] if the class for [elementType] could not be found
     */
    @Throws(ClassNotFoundException::class)
    public fun toClass(loader: ClassLoader): Class<*> {
        val clz = when (elementType) {
            is PrimitiveType -> elementType.toClass()
            is ReferenceType -> elementType.toClass(loader)
            is ArrayType -> elementType.toClass(loader)
        }

        return Array.newInstance(clz, 0).javaClass
    }
}

public inline fun <reified T : Any> ArrayType(): ArrayType = ArrayType.of(T::class.java)

public inline fun <reified T : Any> ArrayType(dimensions: Int): ArrayType = ArrayType.of(T::class.java, dimensions)