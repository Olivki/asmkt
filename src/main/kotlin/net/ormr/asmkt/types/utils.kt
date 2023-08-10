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

import java.lang.reflect.Constructor
import java.lang.reflect.Method

internal val Class<*>.isPrimitiveWrapper: Boolean
    get() = when (this) {
        Void::class.javaObjectType -> true
        Boolean::class.javaObjectType -> true
        Char::class.javaObjectType -> true
        Byte::class.javaObjectType -> true
        Short::class.javaObjectType -> true
        Int::class.javaObjectType -> true
        Long::class.javaObjectType -> true
        Float::class.javaObjectType -> true
        Double::class.javaObjectType -> true
        else -> false
    }

internal val Class<*>.primitiveClass: Class<*>
    get() = when (this) {
        Void::class.javaObjectType -> Void.TYPE
        Boolean::class.javaObjectType -> java.lang.Boolean.TYPE
        Char::class.javaObjectType -> Character.TYPE
        Byte::class.javaObjectType -> java.lang.Byte.TYPE
        Short::class.javaObjectType -> java.lang.Short.TYPE
        Int::class.javaObjectType -> Integer.TYPE
        Long::class.javaObjectType -> java.lang.Long.TYPE
        Float::class.javaObjectType -> java.lang.Float.TYPE
        Double::class.javaObjectType -> java.lang.Double.TYPE
        else -> throw UnsupportedOperationException("No primitive exists for $this.")
    }

internal fun Method.toDescriptorString(): String = AsmType.getMethodDescriptor(this)

internal fun Constructor<*>.toDescriptorString(): String = AsmType.getConstructorDescriptor(this)

internal val sortNames: List<String> =
    listOf("void", "boolean", "char", "byte", "short", "int", "float", "long", "double", "array", "object", "method")

internal fun requireSort(type: AsmType, requiredSort: Int) {
    require(type.sort == requiredSort) { "'type' must be a ${sortNames[requiredSort]} type, was a ${sortNames[type.sort]} type." }
}

internal fun requireNotVoid(type: Type, name: String = "type") {
    require(type !is PrimitiveType.Void) { "'$name' must not be 'void'" }
}

internal fun buildMethodDescriptor(
    returnType: FieldType,
    typeParameters: Array<out FieldType>,
): String = buildString {
    typeParameters.joinTo(this, "", "(", ")") { it.descriptor }
    append(returnType.descriptor)
}