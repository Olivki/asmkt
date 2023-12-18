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

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import java.lang.invoke.MethodType as JMethodType

public class MethodType private constructor(private val delegate: AsmType) : Type {
    override val descriptor: String = delegate.descriptor

    /**
     * The size of the [argument types][argumentTypes] and [return type][returnType] of the method.
     *
     * For more information on how the size of a type is calculated, see [ReturnableType.slotSize].
     */
    override val slotSize: Int
        get() = delegate.argumentsAndReturnSizes

    /**
     * A list of all the argument types of the method.
     */
    public val argumentTypes: List<FieldType> =
        Collections.unmodifiableList(delegate.argumentTypes.map(AsmType::toFieldType))

    /**
     * The return type of the method.
     */
    public val returnType: ReturnableType = delegate.returnType.toReturnableType()

    override fun asAsmType(): AsmType = delegate

    override fun asString(): String =
        "(${argumentTypes.joinToString(", ") { it.asString() }}) -> ${returnType.asString()}"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MethodType -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "MethodType(argumentTypes=$argumentTypes, returnType=$returnType)"

    public companion object {
        /**
         * Returns a [MethodType] that is wrapped around the given [type].
         *
         * @throws [IllegalArgumentException] if the given [type] is not a method type.
         */
        public fun copyOf(type: AsmType): MethodType {
            require(type.isMethod) { "Type (${type.asString()}) is not a method type" }
            return MethodType(type)
        }

        /**
         * Returns a [MethodType] representing the method-type of the given [type].
         */
        public fun from(type: JMethodType): MethodType = fromDescriptor(type.toMethodDescriptorString())

        /**
         * Returns a [MethodType] representing the method-type of the given [method].
         */
        public fun from(method: Method): MethodType = fromDescriptor(AsmType.getMethodDescriptor(method))

        /**
         * Returns a [MethodType] representing the method-type of the given [constructor].
         *
         * The `return type` of a constructor will *always* be [void][VoidType].
         */
        public fun from(constructor: Constructor<*>): MethodType =
            fromDescriptor(AsmType.getConstructorDescriptor(constructor))

        public fun of(returnType: ReturnableType): MethodType =
            fromDescriptor("()${returnType.descriptor}")

        public fun of(returnType: ReturnableType, vararg argumentTypes: FieldType): MethodType =
            fromDescriptor(descriptorOf(returnType, argumentTypes.asIterable()))

        public fun of(returnType: ReturnableType, argumentTypes: Iterable<FieldType>): MethodType =
            fromDescriptor(descriptorOf(returnType, argumentTypes))

        public fun fromDescriptor(descriptor: String): MethodType =
            copyOf(AsmType.getMethodType(descriptor))

        public fun generic(
            arity: Int,
            finalArray: Boolean = false,
            returnType: FieldType = ReferenceType.OBJECT,
        ): MethodType {
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
            returnType: ReturnableType,
            typeParameters: Iterable<FieldType>,
        ): String = buildString {
            typeParameters.joinTo(this, "", "(", ")") { it.descriptor }
            append(returnType.descriptor)
        }
    }
}

public fun MethodType(returnType: ReturnableType): MethodType = MethodType.of(returnType)

public fun MethodType(returnType: ReturnableType, vararg argumentTypes: FieldType): MethodType =
    MethodType.of(returnType, argumentTypes.asIterable())

public fun MethodType(returnType: ReturnableType, argumentTypes: Iterable<FieldType>): MethodType =
    MethodType.of(returnType, argumentTypes)

/**
 * Returns a type representing `this` method-type.
 */
public fun JMethodType.toMethodType(): MethodType = MethodType.from(this)

/**
 * Returns a type representing the method-type of `this` method.
 */
public fun Method.toMethodType(): MethodType = MethodType.from(this)

/**
 * Returns a type representing the method-type of `this` constructor.
 *
 * The `return type` of a constructor will *always* be [void][VoidType].
 */
public fun Constructor<*>.toMethodType(): MethodType = MethodType.from(this)