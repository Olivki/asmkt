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

/**
 * Type-alias for [org.objectweb.asm.Type].
 */
public typealias AsmType = org.objectweb.asm.Type

/**
 * Returns `true` if this type is a primitive type, otherwise `false`.
 */
public val AsmType.isPrimitive: Boolean
    get() = sort in AsmType.VOID..AsmType.DOUBLE

/**
 * Returns `true` if this type is a primitive type and *not* `void`, otherwise `false`.
 */
public val AsmType.isPrimitiveAndNotVoid: Boolean
    get() = sort in AsmType.BOOLEAN..AsmType.DOUBLE

/**
 * Returns `true` if this type is an object type, otherwise `false`.
 */
public val AsmType.isObject: Boolean
    get() = sort == AsmType.OBJECT

/**
 * Returns `true` if this type is an array type, otherwise `false`.
 */
public val AsmType.isArray: Boolean
    get() = sort == AsmType.ARRAY

/**
 * Returns `true` if this type is a method type, otherwise `false`.
 */
public val AsmType.isMethod: Boolean
    get() = sort == AsmType.METHOD

public fun AsmType.toType(): Type = when {
    isPrimitive -> toPrimitiveType()
    isObject -> toReferenceType()
    isArray -> toArrayType()
    isMethod -> toMethodType()
    else -> throw IllegalArgumentException("Type (${this.asString()}) is not supported")
}

public fun AsmType.toReturnableType(): ReturnableType = when {
    isPrimitive -> toPrimitiveType()
    isObject -> toReferenceType()
    isArray -> toArrayType()
    else -> throw IllegalArgumentException("Type (${this.asString()}) is not a returnable type")
}

public fun AsmType.toFieldType(): FieldType = when {
    isPrimitiveAndNotVoid -> toPrimitiveType() as FieldType
    isObject -> toReferenceType()
    isArray -> toArrayType()
    else -> throw IllegalArgumentException("Type (${this.asString()}) is not a field type")
}

public fun AsmType.toPrimitiveType(): PrimitiveType = PrimitiveType.copyOf(this)

public fun AsmType.toReferenceType(): ReferenceType = ReferenceType.copyOf(this)

public fun AsmType.toArrayType(): ArrayType = ArrayType.copyOf(this)

public fun AsmType.toMethodType(): MethodType = MethodType.copyOf(this)

private val sortNames =
    arrayOf("void", "boolean", "char", "byte", "short", "int", "float", "long", "double", "array", "object", "method")

internal fun AsmType.asString(): String {
    val sort = sort
    return "${sortNames.getOrNull(sort) ?: "unknown"}#$sort :: $descriptor"
}