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
import net.ormr.asmkt.ClassFileVersion
import kotlin.reflect.KClass

/**
 * Represents a reference type.
 *
 * A reference type is essentially any type that is not a primitive type, and is represented by a class.
 */
public class ReferenceType private constructor(private val delegate: AsmType) : FieldType, TypeWithInternalName {
    override val descriptor: String = delegate.descriptor

    /**
     * The fully qualified class name of the type.
     */
    override val name: String = delegate.className

    override val simpleName: String
        get() = name.substringAfterLast('.')

    /**
     * Returns the internal name of the class corresponding to the type.
     *
     * The internal name of a type is its fully qualified name *(as returned by [name])* with `.` replaced with
     * `/`.
     */
    override val internalName: String = delegate.internalName

    /**
     * Returns the unboxed type of this type, or `null` if this type is not a boxed type.
     *
     * For example, if this type is [ReferenceType.BOOLEAN], then this method will return [BooleanType].
     *
     * @see [PrimitiveType.box]
     */
    public fun unboxOrNull(): PrimitiveType? = when (this) {
        VOID -> VoidType
        BOOLEAN -> BooleanType
        CHAR -> CharType
        BYTE -> ByteType
        SHORT -> ShortType
        INT -> IntType
        LONG -> LongType
        FLOAT -> FloatType
        DOUBLE -> DoubleType
        else -> null
    }

    override fun asAsmType(): AsmType = delegate

    override fun asString(): String = internalName

    /**
     * Returns the class corresponding to the [name] of this type.
     *
     * @see [Class.forName]
     */
    @AsmKtReflection
    override fun getOrLoadClass(): Class<*> = Class.forName(name)

    /**
     * Returns the class corresponding to the [name] of this type, loaded with the given [loader].
     *
     * @see [Class.forName]
     */
    @AsmKtReflection
    public fun getOrLoadClass(loader: ClassLoader): Class<*> = Class.forName(name, true, loader)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ReferenceType -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "ReferenceType(name='$name')"

    public companion object {
        // needs to be declared before the constants, or we'll get a NPE
        private val constants = hashMapOf<String, ReferenceType>()

        // -- PRIMITIVES -- \\
        /**
         * The `java.lang.Void` type.
         *
         * Descriptor: `Ljava/lang/Void;`
         *
         * @see [VoidType]
         */
        public val VOID: ReferenceType = create("Ljava/lang/Void;")

        /**
         * The `java.lang.Boolean` type.
         *
         * Descriptor: `Ljava/lang/Boolean;`
         *
         * @see [BooleanType]
         */
        public val BOOLEAN: ReferenceType = create("Ljava/lang/Boolean;")

        /**
         * The `java.lang.Character` type.
         *
         * Descriptor: `Ljava/lang/Character;`
         *
         * @see [CharType]
         */
        public val CHAR: ReferenceType = create("Ljava/lang/Character;")

        /**
         * The `java.lang.Byte` type.
         *
         * Descriptor: `Ljava/lang/Byte;`
         *
         * @see [ByteType]
         */
        public val BYTE: ReferenceType = create("Ljava/lang/Byte;")

        /**
         * The `java.lang.Short` type.
         *
         * Descriptor: `Ljava/lang/Short;`
         *
         * @see [ShortType]
         */
        public val SHORT: ReferenceType = create("Ljava/lang/Short;")

        /**
         * The `java.lang.Integer` type.
         *
         * Descriptor: `Ljava/lang/Integer;`
         *
         * @see [IntType]
         */
        public val INT: ReferenceType = create("Ljava/lang/Integer;")

        /**
         * The `java.lang.Long` type.
         *
         * Descriptor: `Ljava/lang/Long;`
         *
         * @see [LongType]
         */
        public val LONG: ReferenceType = create("Ljava/lang/Long;")

        /**
         * The `java.lang.Float` type.
         *
         * Descriptor: `Ljava/lang/Float;`
         *
         * @see [FloatType]
         */
        public val FLOAT: ReferenceType = create("Ljava/lang/Float;")

        /**
         * The `java.lang.Double` type.
         *
         * Descriptor: `Ljava/lang/Double;`
         *
         * @see [DoubleType]
         */
        public val DOUBLE: ReferenceType = create("Ljava/lang/Double;")

        // -- OTHERS -- \\
        /**
         * The `java.lang.Object` type.
         *
         * Descriptor: `Ljava/lang/Object;`
         */
        public val OBJECT: ReferenceType = create("Ljava/lang/Object;")

        /**
         * The `java.lang.Class` type.
         *
         * Descriptor: `Ljava/lang/Class;`
         */
        public val CLASS: ReferenceType = create("Ljava/lang/Class;")

        /**
         * The `java.lang.String` type.
         *
         * Descriptor: `Ljava/lang/String;`
         */
        public val STRING: ReferenceType = create("Ljava/lang/String;")

        /**
         * The `java.lang.Throwable` type.
         *
         * Descriptor: `Ljava/lang/Throwable;`
         */
        public val STRING_BUILDER: ReferenceType = create("Ljava/lang/StringBuilder;")

        /**
         * The `java.lang.Objects` type.
         *
         * Descriptor: `Ljava/lang/Objects;`
         *
         * Note that this type is only available on [Java 7][ClassFileVersion.RELEASE_7] and above.
         */
        public val OBJECTS: ReferenceType = create("Ljava/util/Objects;")

        /**
         * The `java.lang.Number` type.
         *
         * Descriptor: `Ljava/lang/Number;`
         */
        public val NUMBER: ReferenceType = create("Ljava/lang/Number;")

        /**
         * The `java.lang.Enum` type.
         *
         * Descriptor: `Ljava/lang/Enum;`
         *
         * Note that this type is only available on [Java 5][ClassFileVersion.RELEASE_5] and above.
         */
        public val ENUM: ReferenceType = create("Ljava/lang/Enum;")

        /**
         * The `java.lang.Record` type.
         *
         * Descriptor: `Ljava/lang/Record;`
         *
         * Note that this type is only available on [Java 14][ClassFileVersion.RELEASE_14] and above.
         */
        public val RECORD: ReferenceType = create("Ljava/lang/Record;")

        /**
         * The `java.lang.Module` type.
         *
         * Descriptor: `Ljava/lang/Module;`
         *
         * Note that this type is only available on [Java 7][ClassFileVersion.RELEASE_9] and above.
         */
        public val METHOD_TYPE: ReferenceType = create("Ljava/lang/invoke/MethodType;")

        /**
         * The `java.lang.MethodHandle` type.
         *
         * Descriptor: `Ljava/lang/invoke/MethodHandle;`
         *
         * Note that this type is only available on [Java 7][ClassFileVersion.RELEASE_7] and above.
         */
        public val METHOD_HANDLES_LOOKUP: ReferenceType = create("Ljava/lang/invoke/MethodHandles\$Lookup;")

        /**
         * The `java.lang.MethodHandle` type.
         *
         * Descriptor: `Ljava/lang/invoke/MethodHandle;`
         *
         * Note that this type is only available on [Java 7][ClassFileVersion.RELEASE_7] and above.
         */
        public val CALL_SITE: ReferenceType = create("Ljava/lang/invoke/CallSite;")

        private fun create(descriptor: String): ReferenceType {
            val delegate = AsmType.getType(descriptor)
            val type = ReferenceType(delegate)
            constants[descriptor] = type
            return type
        }

        /**
         * Returns a [ReferenceType] wrapped around the given [type].
         *
         * @throws [IllegalArgumentException] if the given [type] is not an object type
         */
        public fun copyOf(type: AsmType): ReferenceType {
            require(type.isObject) { "Type (${type.asString()}) is not an object type" }
            return constants[type.descriptor] ?: ReferenceType(type)
        }

        /**
         * Returns a [ReferenceType] based on the given [descriptor].
         *
         * The descriptor for a reference type is the fully qualified name of the type, with `.` replaced with `/` and
         * surrounded by `L` and `;`. For example, the descriptor for `java.lang.String` is `Ljava/lang/String;`.
         *
         * Note that no validation is done on the given [descriptor], so it is up to the caller to ensure that the given
         * descriptor is correct.
         *
         * @param [descriptor] the descriptor of the type
         *
         * @throws [IllegalArgumentException] if the given [descriptor] doesn't represent a reference type
         */
        public fun fromDescriptor(descriptor: String): ReferenceType =
            constants[descriptor] ?: copyOf(AsmType.getType(descriptor))

        /**
         * Returns a [ReferenceType] based on the given [internalName].
         *
         * The internal name for a reference type is the fully qualified name of the type, with `.` replaced with `/`.
         * For example, the internal name for `java.lang.String` is `java/lang/String`.
         *
         * Note that no validation is done on the given [internalName], so it is up to the caller to ensure that the
         * given name is correct.
         *
         * @param [internalName] the internal name of the type
         */
        public fun fromInternalName(internalName: String): ReferenceType = copyOf(AsmType.getObjectType(internalName))

        /**
         * Returns a [ReferenceType] based on the given [clz].
         *
         * @param [clz] the class to get the [ReferenceType] for
         *
         * @throws [IllegalArgumentException] if the given [clz] is not a reference type
         */
        public fun fromClass(clz: Class<*>): ReferenceType = fromDescriptor(AsmType.getDescriptor(clz))
    }
}

/**
 * Returns a [ReferenceType] based on the given [internalName].
 *
 * The internal name for a reference type is the fully qualified name of the type, with `.` replaced with `/`.
 * For example, the internal name for `java.lang.String` is `java/lang/String`.
 *
 * Note that no validation is done on the given [internalName], so it is up to the caller to ensure that the given name
 * is correct.
 *
 * @param [internalName] the internal name of the type
 *
 * @see [ReferenceType.fromInternalName]
 */
public fun ReferenceType(internalName: String): ReferenceType = ReferenceType.fromInternalName(internalName)

/**
 * Returns a [ReferenceType] based on the given [T] type.
 *
 * @param [T] the type to get the [ReferenceType] for
 *
 * @see [ReferenceType.fromClass]
 */
public inline fun <reified T : Any> ReferenceType(): ReferenceType = ReferenceType.fromClass(T::class.java)

/**
 * Returns a [ReferenceType] based on `this` class.
 *
 * @throws [IllegalArgumentException] if `this` class is not a reference type
 */
public fun Class<*>.toReferenceType(): ReferenceType = ReferenceType.fromClass(this)

/**
 * Returns a [ReferenceType] based on `this` class.
 *
 * @throws [IllegalArgumentException] if `this` class is not a reference type
 */
public fun KClass<*>.toReferenceType(): ReferenceType = ReferenceType.fromClass(java)