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

package net.ormr.asmkt.types

import org.objectweb.asm.Type as AsmType

/**
 * Represents a type that can be used as the type of variable/parameter/return type, etc..
 *
 * @see [PrimitiveType]
 * @see [ReferenceType]
 * @see [ArrayType]
 */
public sealed class FieldType : Type() {
    final override val size: Int
        get() = delegate.size

    public abstract val className: String

    /**
     * Returns the simple name of `this` type, as it would have been given in the source code.
     *
     * The simple name of an array is the simple name of the [element type][ArrayType.elementType] with `[]` appended.
     */
    public abstract val simpleName: String

    /**
     * Returns `true` if `this` type can be used as the type of variable, otherwise `false`.
     */
    public abstract val isValidFieldType: Boolean

    // TODO: use MethodHandle.Lookup instead of a classloader for this?
    /**
     * Returns the class representation of `this` type.
     *
     * @throws [ClassNotFoundException] if no class could be found representing `this` type
     */
    @Throws(ClassNotFoundException::class)
    public abstract fun toClass(): Class<*>

    final override fun toString(): String = className

    public companion object {
        public fun copyOf(type: AsmType): FieldType = when (type.sort) {
            AsmType.VOID, AsmType.BOOLEAN, AsmType.CHAR, AsmType.BYTE, AsmType.SHORT, AsmType.INT, AsmType.LONG,
            AsmType.FLOAT, AsmType.DOUBLE,
            -> PrimitiveType.copyOf(type)
            AsmType.ARRAY -> ArrayType.copyOf(type)
            AsmType.OBJECT -> ReferenceType.copyOf(type)
            AsmType.METHOD -> throw IllegalArgumentException("A method type is not a definable type; '$type'.")
            else -> throw UnsupportedOperationException("Unknown 'sort' value ${type.sort}.")
        }

        public fun of(clz: Class<*>): FieldType = when {
            clz.isPrimitive -> PrimitiveType.of(clz)
            clz.isArray -> ArrayType.of(clz)
            else -> ReferenceType.of(clz)
        }
    }
}