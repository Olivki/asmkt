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

import net.ormr.asmkt.type.MethodTypeDesc.Companion.fromDescriptor

public fun MethodTypeDesc.prependArguments(vararg newArgumentTypes: FieldTypeDesc): MethodTypeDesc =
    prependArguments(newArgumentTypes.asIterable())

public fun MethodTypeDesc.prependArguments(newArgumentTypes: Iterable<FieldTypeDesc>): MethodTypeDesc {
    if (newArgumentTypes.none()) {
        return this
    }
    val newDescriptor = buildString {
        append('(')
        newArgumentTypes.joinTo(this, "") { it.descriptor }
        argumentTypes.joinTo(this, "") { it.descriptor }
        append(')')
        append(returnType.descriptor)
    }
    return fromDescriptor(newDescriptor)
}

public fun MethodTypeDesc.appendArguments(newArgumentTypes: Iterable<FieldTypeDesc>): MethodTypeDesc {
    if (newArgumentTypes.none()) {
        return this
    }
    val newDescriptor = buildString {
        append('(')
        argumentTypes.joinTo(this, "") { it.descriptor }
        newArgumentTypes.joinTo(this, "") { it.descriptor }
        append(')')
        append(returnType.descriptor)
    }
    return fromDescriptor(newDescriptor)
}

public fun MethodTypeDesc.appendArguments(vararg newArgumentTypes: FieldTypeDesc): MethodTypeDesc =
    appendArguments(newArgumentTypes.asIterable())

/**
 * Returns a type based on `this` type but with the type of the argument at the given [index] changed to [newType].
 *
 * If the type at `index` is the same the given `newType` then `this` instance is returned.
 *
 * @throws [IllegalArgumentException] if `index` is negative, or if `index` is larger than the available
 * [argumentTypes][MethodTypeDesc.argumentTypes]
 */
public fun MethodTypeDesc.withArgument(index: Int, newType: FieldTypeDesc): MethodTypeDesc {
    require(index >= 0) { "index ($index) < 0" }
    require(index < argumentTypes.size) { "index ($index) is out of argument bounds (${argumentTypes.size})" }
    return when (newType) {
        argumentTypes[index] -> this
        else -> {
            val newDescriptor = buildString {
                append('(')

                for ((i, type) in argumentTypes.withIndex()) {
                    append(if (i == index) newType.descriptor else type.descriptor)
                }

                append(')')
                append(returnType.descriptor)
            }

            fromDescriptor(newDescriptor)
        }
    }
}

/**
 * Returns a type based on `this` type but with the argument at the given [index] removed.
 *
 * @throws [IllegalArgumentException] if `index` is negative, or if `index` is larger than the available
 * [argumentTypes][MethodTypeDesc.argumentTypes]
 */
public fun MethodTypeDesc.dropArgument(index: Int): MethodTypeDesc {
    require(index >= 0) { "index ($index) < 0" }
    require(index < argumentTypes.size) { "index ($index) is out of argument bounds (${argumentTypes.size})" }
    val newDescriptor = buildString {
        append('(')

        for ((i, type) in argumentTypes.withIndex()) {
            if (i != index) {
                append(type.descriptor)
            }
        }

        append(')')
        append(returnType.descriptor)
    }
    return fromDescriptor(newDescriptor)
}

/**
 * Returns a type based on `this` type but with the given [arguments] inserted at the given [index].
 *
 * @throws [IllegalArgumentException] if `index` is negative, or if `index` is larger than the available number of
 * [argumentTypes][MethodTypeDesc.argumentTypes]
 */
public fun MethodTypeDesc.insertArguments(index: Int, arguments: Iterable<FieldTypeDesc>): MethodTypeDesc {
    require(index >= 0) { "index ($index) < 0" }
    require(index < argumentTypes.size) { "index ($index) is out of argument bounds (${argumentTypes.size})" }
    val newDescriptor = buildString {
        append('(')

        for ((i, type) in argumentTypes.withIndex()) {
            if (i == index) {
                arguments.joinTo(this, "") { it.descriptor }
            }
            append(type.descriptor)
        }

        append(')')
        append(returnType.descriptor)
    }
    return fromDescriptor(newDescriptor)
}

/**
 * Returns a type based on `this` type but with the [return type][MethodTypeDesc.returnType] changed to the given [newType].
 *
 * If the current `returnType` is the same as `newType` then `this` instance is returned.
 */
public fun MethodTypeDesc.withReturn(newType: FieldTypeDesc): MethodTypeDesc = when (returnType) {
    newType -> this
    else -> {
        val newDescriptor = buildString {
            argumentTypes.joinTo(this, "", "(", ")") { it.descriptor }
            append(newType.descriptor)
        }

        fromDescriptor(newDescriptor)
    }
}