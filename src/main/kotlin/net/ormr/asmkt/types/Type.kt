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

@file:JvmName("TypeUtils")
@file:Suppress("unused")

package net.ormr.asmkt.types

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import org.objectweb.asm.Type as AsmType

sealed class Type {
    companion object {
        fun copyOf(type: AsmType): Type = when (type.sort) {
            AsmType.VOID, AsmType.BOOLEAN, AsmType.CHAR, AsmType.BYTE, AsmType.SHORT,
            AsmType.INT, AsmType.LONG, AsmType.FLOAT, AsmType.DOUBLE,
            -> PrimitiveType.copyOf(type)
            AsmType.ARRAY -> ArrayType.copyOf(type)
            AsmType.OBJECT -> ReferenceType.copyOf(type)
            AsmType.METHOD -> MethodType.copyOf(type)
            else -> throw UnsupportedOperationException("Unknown 'sort' value ${type.sort}.")
        }
    }

    protected abstract val delegate: AsmType

    // TODO: documentation

    abstract val size: Int

    abstract val descriptor: String

    fun getOpcode(opcode: Int): Int = delegate.getOpcode(opcode)

    fun toAsmType(): AsmType = delegate

    final override fun equals(other: Any?): Boolean = delegate == other

    private val cachedHash: Int by lazy { delegate.hashCode() }

    final override fun hashCode(): Int = cachedHash

    abstract override fun toString(): String
}

/**
 * Returns a type representing the type of `this` class.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Class<*>.toReferenceType(): ReferenceType = ReferenceType.of(this)

/**
 * Returns a type representing the method-type of `this` method.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Method.toMethodType(): MethodType = MethodType.of(this)

/**
 * Returns a type representing the method-type of `this` constructor.
 *
 * The `return type` of a constructor will *always* be [void][PrimitiveType.Void].
 *
 * @see [AsmType.getType]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Constructor<*>.toMethodType(): MethodType = MethodType.of(this)

/**
 * Returns a type based on `this` type but with the type of the argument at the given [index] changed to [T].
 */
inline fun <reified T : Any> MethodType.changeArgument(index: Int): MethodType =
    this.changeArgument(index, ReferenceType<T>())

/**
 * Returns a type based on `this` type but with the [return type][MethodType.returnType] changed to [T].
 */
inline fun <reified T : Any> MethodType.changeReturn(): MethodType = changeReturn(ReferenceType<T>())