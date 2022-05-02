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

package net.ormr.asmkt

import net.ormr.asmkt.types.*
import org.objectweb.asm.Label

/**
 * Creates the instruction for invoking the constructor of [owner].
 *
 * @param [owner] the object to invoke the constructor of
 * @param [type] the type of the constructor, must have a `returnType` of [void][PrimitiveType.Void]
 *
 * @return `this` *(for chaining)*
 *
 * @throws [IllegalArgumentException] if the [return type][MethodType.returnType] is not [void][PrimitiveType.Void]
 */
@AsmKtDsl
fun BytecodeMethod.invokeConstructor(
    owner: ReferenceType,
    type: MethodType = MethodType.VOID,
): BytecodeMethod = apply {
    require(type.returnType is PrimitiveType.Void) { "return type of a constructor must be 'void', was '$type'." }
    invokeSpecial(owner, "<init>", type)
}

/**
 * Creates the instruction for putting the `this` pointer onto the stack.
 *
 * @throws [IllegalStateException] if `this` block represents a static method
 */
@AsmKtDsl
fun BytecodeMethod.loadThis(): BytecodeMethod = apply {
    check(!isStatic) { "Can't load a 'this' pointer in a static context." }
    loadLocal(0, parentType)
}

@AsmKtDsl
fun BytecodeMethod.throwException(
    exception: ReferenceType,
    message: String? = null,
): BytecodeMethod = apply {
    val constructorType = when {
        message != null -> MethodType.ofVoid(ReferenceType.STRING)
        else -> MethodType.VOID
    }

    new(exception)
    dup()

    if (message != null) {
        push(message)
    }

    invokeConstructor(exception, constructorType)
    throwException()
}

@AsmKtDsl
fun BytecodeMethod.box(type: PrimitiveType): BytecodeMethod = apply {
    if (type is PrimitiveType.Void) {
        // a boxed 'void' would actually be the 'Void' object, but we can't create instances of that
        // so 'null' is the best we'll get
        push(null)
    } else {
        val boxedType = type.toBoxed()
        new(boxedType)

        if (type.size == 2) {
            // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
            dupx2()
            dupx2()
            pop()
        } else {
            // p -> po -> opo -> oop -> o
            dupx1()
            swap()
        }

        invokeConstructor(boxedType, MethodType.ofVoid(type))
    }
}

@AsmKtDsl
fun BytecodeMethod.unbox(type: FieldType): BytecodeMethod = apply {
    require(type !is ArrayType) { "'type' must not be an array type." }

    // we can't unbox a 'void' primitive in any manner
    if (type is PrimitiveType.Void) {
        return this
    }

    val boxedType = when (type) {
        PrimitiveType.Char -> ReferenceType.CHAR
        PrimitiveType.Boolean -> ReferenceType.BOOLEAN
        else -> ReferenceType.NUMBER
    }

    val unboxMethodType: MethodType? = when (type) {
        PrimitiveType.Boolean -> MethodType.BOOLEAN
        PrimitiveType.Char -> MethodType.CHAR
        PrimitiveType.Byte, PrimitiveType.Short, PrimitiveType.Int -> MethodType.INT
        PrimitiveType.Long -> MethodType.LONG
        PrimitiveType.Float -> MethodType.FLOAT
        PrimitiveType.Double -> MethodType.DOUBLE
        else -> null
    }

    if (unboxMethodType == null) {
        checkCast(type as ReferenceType)
    } else {
        checkCast(boxedType)
        invokeVirtual(boxedType, "${type.simpleName}Value", unboxMethodType)
    }
}

/**
 * Creates the instructions needed to box the value currently at the top of the stack using the `valueOf` functions
 * located in the boxed variants for primitive types.
 *
 * @param [type] the type to box
 *
 * @throws [IllegalArgumentException] if [type] is a method-type
 */
@AsmKtDsl
fun BytecodeMethod.valueOf(type: PrimitiveType): BytecodeMethod = apply {
    if (type is PrimitiveType.Void) {
        // a boxed 'void' would actually be the 'Void' object, but we can't create instances of that
        // so 'null' is the best we'll get
        push(null)
    } else {
        val boxedType = type.toBoxed()
        invokeStatic(boxedType, "valueOf", MethodType.of(boxedType, type))
    }
}

/**
 * Creates the instructions for a `switch` statement.
 *
 * @param [keys] the `switch` keys
 * @param [generateCase] the generator for each case
 * @param [generateDefaultCase] the generator for the `default` case
 * @param [useTable] `true` to use the [tableswitch][BytecodeMethod.tableSwitch] instruction, or `false` to use the
 * [lookupswitch][BytecodeMethod.lookUpSwitch] instruction
 *
 * @throws [IllegalArgumentException] if [keys] is not sorted in an ascending order
 */
@AsmKtDsl
inline fun BytecodeMethod.tableSwitch(
    keys: IntArray,
    generateCase: (key: Int, end: Label) -> Unit,
    generateDefaultCase: () -> Unit,
    useTable: Boolean = calculateKeyDensity(keys) >= 0.5F,
): BytecodeMethod = apply {
    for (i in 1 until keys.size) {
        require(keys[i] >= keys[i - 1]) { "keys must be sorted in ascending order" }
    }

    val defaultLabel = Label()
    val endLabel = Label()

    if (keys.isNotEmpty()) {
        val numKeys = keys.size

        if (useTable) {
            val min = keys[0]
            val max = keys[numKeys - 1]
            val range = max - min + 1
            val labels = Array(range) { defaultLabel }

            for (i in 0 until numKeys) {
                labels[keys[i] - min] = Label()
            }

            tableSwitch(min, max, defaultLabel, labels)

            for (i in 0 until range) {
                val label = labels[i]

                if (label != defaultLabel) {
                    mark(label)
                    generateCase(i + min, endLabel)
                }
            }
        } else {
            val labels = Array(numKeys) { Label() }

            lookUpSwitch(defaultLabel, keys, labels)

            for (i in 0 until numKeys) {
                mark(labels[i])
                generateCase(keys[i], endLabel)
            }
        }
    }

    mark(defaultLabel)
    generateDefaultCase()
    mark(endLabel)
}

@PublishedApi
internal fun calculateKeyDensity(keys: IntArray): Float = when {
    keys.isEmpty() -> 0.0F
    else -> keys.size.toFloat() / (keys[keys.size - 1] - keys[0] + 1)
}

// TODO: push a constant dynamic invoking the 'getStaticField' constant bootstrap instead?
/**
 * Pushes the `TRUE` constant from the [Boolean] class onto the stack, or the `FALSE` constant depending on the given
 * [value].
 *
 * @see [pushBoxedTrue]
 * @see [pushBoxedFalse]
 */
@AsmKtDsl
fun BytecodeMethod.pushBoxedBoolean(value: Boolean): BytecodeMethod = apply {
    if (value) {
        pushBoxedTrue()
    } else {
        pushBoxedFalse()
    }
}

/**
 * Pushes the `TRUE` constant from the [Boolean] class onto the stack.
 *
 * @see [pushBoxedBoolean]
 */
@AsmKtDsl
fun BytecodeMethod.pushBoxedTrue(): BytecodeMethod = apply {
    getStatic(ReferenceType.BOOLEAN, "TRUE", ReferenceType.BOOLEAN)
}

/**
 * Pushes the `FALSE` constant from the [Boolean] class onto the stack.
 *
 * @see [pushBoxedBoolean]
 */
@AsmKtDsl
fun BytecodeMethod.pushBoxedFalse(): BytecodeMethod = apply {
    getStatic(ReferenceType.BOOLEAN, "FALSE", ReferenceType.BOOLEAN)
}

@AsmKtDsl
fun BytecodeMethod.pushBooleanValue(): BytecodeMethod = apply {
    checkCast(ReferenceType.BOOLEAN)
    invokeVirtual(ReferenceType.BOOLEAN, "booleanValue", MethodType.BOOLEAN)
}

@AsmKtDsl
inline fun <reified A : Annotation> BytecodeMethod.defineParameterAnnotation(
    index: Int,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit = {},
) {
    defineParameterAnnotation(index, ReferenceType<A>(), isVisible, allowRepeats).apply(scope)
}

@AsmKtDsl
inline fun BytecodeMethod.defineParameterAnnotation(
    index: Int,
    type: ReferenceType,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit,
) {
    defineParameterAnnotation(index, type, isVisible, allowRepeats).apply(scope)
}