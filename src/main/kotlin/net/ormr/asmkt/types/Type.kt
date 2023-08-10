/*
 * Copyright 2020-2023 Oliver Berg
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

public sealed class Type {
    protected abstract val delegate: AsmType

    // TODO: documentation

    public abstract val size: Int

    public abstract val descriptor: String

    public fun getOpcode(opcode: Int): Int = delegate.getOpcode(opcode)

    public fun toAsmType(): AsmType = delegate

    final override fun equals(other: Any?): Boolean = delegate == other

    private val cachedHash: Int by lazy { delegate.hashCode() }

    final override fun hashCode(): Int = cachedHash

    abstract override fun toString(): String

    public companion object {
        public fun copyOf(type: AsmType): Type = when (type.sort) {
            AsmType.VOID, AsmType.BOOLEAN, AsmType.CHAR, AsmType.BYTE, AsmType.SHORT,
            AsmType.INT, AsmType.LONG, AsmType.FLOAT, AsmType.DOUBLE,
            -> PrimitiveType.copyOf(type)
            AsmType.ARRAY -> ArrayType.copyOf(type)
            AsmType.OBJECT -> ReferenceType.copyOf(type)
            AsmType.METHOD -> MethodType.copyOf(type)
            else -> throw UnsupportedOperationException("Unknown 'sort' value ${type.sort}.")
        }
    }
}