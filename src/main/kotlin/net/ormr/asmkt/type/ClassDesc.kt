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

public class ClassDesc private constructor(private val delegate: AsmType) : FieldTypeDesc, TypeDescWithInternalName {
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
     * For example, if this type is [ClassDesc.BOOLEAN], then this method will return [BooleanTypeDesc].
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
        other !is ClassDesc -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "ReferenceTypeDesc(name='$name')"

    public companion object {
        // needs to be declared before the constants, or we'll get a NPE
        private val constants = hashMapOf<String, ClassDesc>()

        // -- PRIMITIVES -- \\
        public val VOID: ClassDesc = create("Ljava/lang/Void;")
        public val BOOLEAN: ClassDesc = create("Ljava/lang/Boolean;")
        public val CHAR: ClassDesc = create("Ljava/lang/Character;")
        public val BYTE: ClassDesc = create("Ljava/lang/Byte;")
        public val SHORT: ClassDesc = create("Ljava/lang/Short;")
        public val INT: ClassDesc = create("Ljava/lang/Integer;")
        public val LONG: ClassDesc = create("Ljava/lang/Long;")
        public val FLOAT: ClassDesc = create("Ljava/lang/Float;")
        public val DOUBLE: ClassDesc = create("Ljava/lang/Double;")

        // -- OTHERS -- \\
        public val OBJECT: ClassDesc = create("Ljava/lang/Object;")
        public val CLASS: ClassDesc = create("Ljava/lang/Class;")
        public val STRING: ClassDesc = create("Ljava/lang/String;")
        public val STRING_BUILDER: ClassDesc = create("Ljava/lang/StringBuilder;")
        public val OBJECTS: ClassDesc = create("Ljava/util/Objects;")
        public val NUMBER: ClassDesc = create("Ljava/lang/Number;")
        public val ENUM: ClassDesc = create("Ljava/lang/Enum;")
        public val RECORD: ClassDesc = create("Ljava/lang/Record;")
        public val METHOD_TYPE: ClassDesc = create("Ljava/lang/invoke/MethodType;")
        public val METHOD_HANDLES_LOOKUP: ClassDesc = create("Ljava/lang/invoke/MethodHandles\$Lookup;")
        public val CALL_SITE: ClassDesc = create("Ljava/lang/invoke/CallSite;")

        private fun create(descriptor: String): ClassDesc {
            val delegate = AsmType.getType(descriptor)
            val type = ClassDesc(delegate)
            constants[descriptor] = type
            return type
        }

        public fun copyOf(type: AsmType): ClassDesc {
            require(type.isObject) { "Type (${type.asString()}) is not an object type" }
            return constants[type.descriptor] ?: ClassDesc(type)
        }

        public fun fromDescriptor(descriptor: String): ClassDesc =
            constants[descriptor] ?: copyOf(AsmType.getType(descriptor))

        public fun fromInternal(internalName: String): ClassDesc = copyOf(AsmType.getObjectType(internalName))

        public fun fromClass(clz: Class<*>): ClassDesc = fromDescriptor(AsmType.getDescriptor(clz))
    }
}

// TODO: document that the internal name is like 'java/lang/String' and not 'java.lang.String'
public fun ClassDesc(internalName: String): ClassDesc = ClassDesc.fromInternal(internalName)

public inline fun <reified T : Any> ClassDesc(): ClassDesc = ClassDesc.fromClass(T::class.java)