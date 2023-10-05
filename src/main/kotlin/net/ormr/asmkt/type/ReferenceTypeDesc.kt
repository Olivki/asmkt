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

public class ReferenceTypeDesc private constructor(private val delegate: AsmType) : FieldTypeDesc,
    TypeDescWithInternalName {
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
     * For example, if this type is [ReferenceTypeDesc.BOOLEAN], then this method will return [BooleanTypeDesc].
     *
     * @see [PrimitiveTypeDesc.box]
     */
    public fun unboxOrNull(): PrimitiveTypeDesc? = when (this) {
        VOID -> VoidTypeDesc
        BOOLEAN -> BooleanTypeDesc
        CHAR -> CharTypeDesc
        BYTE -> ByteTypeDesc
        SHORT -> ShortTypeDesc
        INT -> IntTypeDesc
        LONG -> LongTypeDesc
        FLOAT -> FloatTypeDesc
        DOUBLE -> DoubleTypeDesc
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
        other !is ReferenceTypeDesc -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "ReferenceTypeDesc(name='$name')"

    public companion object {
        // -- PRIMITIVES -- \\
        public val VOID: ReferenceTypeDesc = constant("Ljava/lang/Void;")
        public val BOOLEAN: ReferenceTypeDesc = constant("Ljava/lang/Boolean;")
        public val CHAR: ReferenceTypeDesc = constant("Ljava/lang/Character;")
        public val BYTE: ReferenceTypeDesc = constant("Ljava/lang/Byte;")
        public val SHORT: ReferenceTypeDesc = constant("Ljava/lang/Short;")
        public val INT: ReferenceTypeDesc = constant("Ljava/lang/Integer;")
        public val LONG: ReferenceTypeDesc = constant("Ljava/lang/Long;")
        public val FLOAT: ReferenceTypeDesc = constant("Ljava/lang/Float;")
        public val DOUBLE: ReferenceTypeDesc = constant("Ljava/lang/Double;")

        // -- OTHERS -- \\
        public val OBJECT: ReferenceTypeDesc = constant("Ljava/lang/Object;")
        public val CLASS: ReferenceTypeDesc = constant("Ljava/lang/Class;")
        public val STRING: ReferenceTypeDesc = constant("Ljava/lang/String;")
        public val STRING_BUILDER: ReferenceTypeDesc = constant("Ljava/lang/StringBuilder;")
        public val OBJECTS: ReferenceTypeDesc = constant("Ljava/util/Objects;")
        public val NUMBER: ReferenceTypeDesc = constant("Ljava/lang/Number;")
        public val ENUM: ReferenceTypeDesc = constant("Ljava/lang/Enum;")
        public val RECORD: ReferenceTypeDesc = constant("Ljava/lang/Record;")
        public val METHOD_TYPE: ReferenceTypeDesc = constant("Ljava/lang/invoke/MethodType;")
        public val METHOD_HANDLES_LOOKUP: ReferenceTypeDesc = constant("Ljava/lang/invoke/MethodHandles\$Lookup;")
        public val CALL_SITE: ReferenceTypeDesc = constant("Ljava/lang/invoke/CallSite;")

        private val constants = hashMapOf<String, ReferenceTypeDesc>()

        private fun constant(descriptor: String): ReferenceTypeDesc {
            val type = ReferenceTypeDesc(AsmType.getType(descriptor))
            constants[descriptor] = type
            return type
        }

        public fun copyOf(type: AsmType): ReferenceTypeDesc {
            require(type.isObject) { "Type (${type.asString()}) is not an object type" }
            return constants[type.descriptor] ?: ReferenceTypeDesc(type)
        }

        public fun fromDescriptor(descriptor: String): ReferenceTypeDesc =
            constants[descriptor] ?: copyOf(AsmType.getType(descriptor))

        public fun fromInternal(internalName: String): ReferenceTypeDesc = fromDescriptor("L$internalName;")

        public fun of(clz: Class<*>): ReferenceTypeDesc = fromDescriptor(AsmType.getDescriptor(clz))
    }
}

public inline fun <reified T : Any> ReferenceTypeDesc(): ReferenceTypeDesc = ReferenceTypeDesc.of(T::class.java)