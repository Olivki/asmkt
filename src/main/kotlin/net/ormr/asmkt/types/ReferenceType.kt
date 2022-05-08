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

import org.objectweb.asm.Type

/**
 * Represents a type of an `Object` child, i.e; `java.lang.String`, `java.lang.Byte`, `java.lang.Double`, etc..
 */
public class ReferenceType private constructor(override val delegate: Type) : FieldType(), TypeWithInternalName {
    public companion object {
        private val cachedTypes: MutableMap<String, ReferenceType> = hashMapOf()

        @JvmField
        public val VOID: ReferenceType = createConstant("Ljava/lang/Void;")

        @JvmField
        public val BOOLEAN: ReferenceType = createConstant("Ljava/lang/Boolean;")

        @JvmField
        public val CHAR: ReferenceType = createConstant("Ljava/lang/Character;")

        @JvmField
        public val BYTE: ReferenceType = createConstant("Ljava/lang/Byte;")

        @JvmField
        public val SHORT: ReferenceType = createConstant("Ljava/lang/Short;")

        @JvmField
        public val INT: ReferenceType = createConstant("Ljava/lang/Integer;")

        @JvmField
        public val LONG: ReferenceType = createConstant("Ljava/lang/Long;")

        @JvmField
        public val FLOAT: ReferenceType = createConstant("Ljava/lang/Float;")

        @JvmField
        public val DOUBLE: ReferenceType = createConstant("Ljava/lang/Double;")

        @JvmField
        public val OBJECT: ReferenceType = createConstant("Ljava/lang/Object;")

        @JvmField
        public val CLASS: ReferenceType = createConstant("Ljava/lang/Class;")

        @JvmField
        public val STRING: ReferenceType = createConstant("Ljava/lang/String;")

        @JvmField
        public val STRING_BUILDER: ReferenceType = createConstant("Ljava/lang/StringBuilder;")

        @JvmField
        public val OBJECTS: ReferenceType = createConstant("Ljava/util/Objects;")

        @JvmField
        public val NUMBER: ReferenceType = createConstant("Ljava/lang/Number;")

        @JvmField
        public val ENUM: ReferenceType = createConstant("Ljava/lang/Enum;")

        @JvmField
        public val RECORD: ReferenceType = createConstant("Ljava/lang/Record;")

        @JvmField
        public val CONSTANT_BOOTSTRAPS: ReferenceType = createConstant("Ljava/lang/invoke/ConstantBootstraps;")

        @JvmField
        public val STRING_CONCAT_FACTORY: ReferenceType = createConstant("Ljava/lang/invoke/StringConcatFactory;")

        @JvmField
        public val METHOD_TYPE: ReferenceType = createConstant("Ljava/lang/invoke/MethodType;")

        @JvmField
        public val METHOD_HANDLES_LOOKUP: ReferenceType = createConstant("Ljava/lang/invoke/MethodHandles\$Lookup;")

        @JvmField
        public val CALL_SITE: ReferenceType = createConstant("Ljava/lang/invoke/CallSite;")

        private fun createConstant(descriptor: String): ReferenceType {
            val type = ReferenceType(Type.getType(descriptor))
            cachedTypes[descriptor] = type
            return type
        }

        public fun copyOf(type: Type): ReferenceType {
            requireSort(type, Type.OBJECT)
            return cachedTypes.getOrPut(type.descriptor) { ReferenceType(type) }
        }

        public fun fromDescriptor(descriptor: String): ReferenceType = when (descriptor) {
            in cachedTypes -> cachedTypes.getValue(descriptor)
            else -> copyOf(Type.getType(descriptor))
        }

        public fun fromInternal(internalName: String): ReferenceType = fromDescriptor("L$internalName;")

        public fun of(clz: Class<*>): ReferenceType = fromDescriptor(Type.getDescriptor(clz))
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
    public val isBoxedPrimitive: Boolean
        get() = toPrimitive() != null

    /**
     * Returns the primitive type of `this` type if it's a boxed type, or itself if it's already a primitive, or
     * `null` if there exists no primitive type for `this` type.
     */
    public fun toPrimitive(): PrimitiveType? = when (this) {
        VOID -> PrimitiveType.Void
        BOOLEAN -> PrimitiveType.Boolean
        CHAR -> PrimitiveType.Char
        BYTE -> PrimitiveType.Byte
        SHORT -> PrimitiveType.Short
        INT -> PrimitiveType.Int
        LONG -> PrimitiveType.Long
        FLOAT -> PrimitiveType.Float
        DOUBLE -> PrimitiveType.Double
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
    public fun toClass(loader: ClassLoader): Class<*> = Class.forName(className, false, loader)
}

public inline fun <reified T : Any> ReferenceType(): ReferenceType = ReferenceType.of(T::class.java)

/**
 * Returns a type representing the type of `this` class.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Class<*>.toReferenceType(): ReferenceType = ReferenceType.of(this)