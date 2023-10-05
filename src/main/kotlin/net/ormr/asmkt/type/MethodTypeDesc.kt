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

import org.objectweb.asm.Type
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*

public class MethodTypeDesc private constructor(private val delegate: AsmType) : TypeDesc {
    override val descriptor: String = delegate.descriptor

    /**
     * The size of the [argument types][argumentTypes] and [return type][returnType] of the method.
     *
     * For more information on how the size of a type is calculated, see [ReturnableTypeDesc.size].
     */
    override val size: Int
        get() = delegate.argumentsAndReturnSizes

    /**
     * A list of all the argument types of the method.
     */
    public val argumentTypes: List<FieldTypeDesc> =
        Collections.unmodifiableList(delegate.argumentTypes.map(Type::toFieldTypeDesc))

    /**
     * The return type of the method.
     */
    public val returnType: ReturnableTypeDesc = delegate.returnType.toReturnableTypeDesc()

    override fun asAsmType(): AsmType = delegate

    override fun asString(): String =
        "(${argumentTypes.joinToString(", ") { it.asString() }}) -> ${returnType.asString()}}"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MethodTypeDesc -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "MethodTypeDesc(argumentTypes=$argumentTypes, returnType=$returnType)"

    public companion object {
        public fun copyOf(type: AsmType): MethodTypeDesc {
            require(type.isMethod) { "Type (${type.asString()}) is not a method type" }
            return MethodTypeDesc(type)
        }

        public fun of(returnType: ReturnableTypeDesc): MethodTypeDesc = fromDescriptor("()${returnType.descriptor}")

        public fun of(returnType: ReturnableTypeDesc, vararg argumentTypes: FieldTypeDesc): MethodTypeDesc =
            fromDescriptor(descriptorOf(returnType, argumentTypes.asIterable()))

        public fun of(returnType: ReturnableTypeDesc, argumentTypes: Iterable<FieldTypeDesc>): MethodTypeDesc =
            fromDescriptor(descriptorOf(returnType, argumentTypes))

        public fun fromDescriptor(descriptor: String): MethodTypeDesc = copyOf(AsmType.getMethodType(descriptor))

        public fun generic(
            arity: Int,
            finalArray: Boolean = false,
            returnType: FieldTypeDesc = ReferenceTypeDesc.OBJECT,
        ): MethodTypeDesc {
            require(arity >= 0) { "arity ($arity) < 0" }
            val size = if (finalArray) arity + 1 else arity
            val descriptor = buildString {
                append('(')
                repeat(size) {
                    if ((it + 1) == size && finalArray) {
                        append("[Ljava/lang/Object;")
                    } else {
                        append("Ljava/lang/Object;")
                    }
                }
                append(')')
                append(returnType.descriptor)
            }
            return fromDescriptor(descriptor)
        }

        private fun descriptorOf(
            returnType: ReturnableTypeDesc,
            typeParameters: Iterable<FieldTypeDesc>,
        ): String = buildString {
            typeParameters.joinTo(this, "", "(", ")") { it.descriptor }
            append(returnType.descriptor)
        }
    }
}

public fun MethodTypeDesc(returnType: ReturnableTypeDesc): MethodTypeDesc = MethodTypeDesc.of(returnType)

public fun MethodTypeDesc(returnType: ReturnableTypeDesc, vararg argumentTypes: FieldTypeDesc): MethodTypeDesc =
    MethodTypeDesc.of(returnType, argumentTypes.asIterable())

public fun MethodTypeDesc(returnType: ReturnableTypeDesc, argumentTypes: Iterable<FieldTypeDesc>): MethodTypeDesc =
    MethodTypeDesc.of(returnType, argumentTypes)

/**
 * Returns a type representing `this` method-type.
 */
public fun MethodType.toMethodTypeDesc(): MethodTypeDesc = MethodTypeDesc.fromDescriptor(toMethodDescriptorString())

/**
 * Returns a type representing the method-type of `this` method.
 */
public fun Method.toMethodTypeDesc(): MethodTypeDesc = MethodTypeDesc.fromDescriptor(AsmType.getMethodDescriptor(this))

/**
 * Returns a type representing the method-type of `this` constructor.
 *
 * The `return type` of a constructor will *always* be [void][VoidTypeDesc].
 */
public fun Constructor<*>.toMethodTypeDesc(): MethodTypeDesc =
    MethodTypeDesc.fromDescriptor(AsmType.getConstructorDescriptor(this))