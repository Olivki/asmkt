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
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Pushes the instructions required to create a new array of type [type] with size [size] onto the stack.
 *
 * @param [type] the type of the array
 * @since [size] how large the array should be
 *
 * @return `this` *(for chaining)*
 *
 * @throws [IllegalArgumentException] if [type] is [void][PrimitiveType.Void]
 */
@AsmKtDsl
public fun BytecodeMethod.newArray(type: FieldType, size: Int): BytecodeMethod = apply {
    requireNotVoid(type)

    pushInt(size)
    newArray(type)
}

/**
 * Pushes the instruction for invoking the constructor of [owner] onto the stack.
 *
 * @param [owner] the object to invoke the constructor of
 * @param [type] the type of the constructor, must have a `returnType` of [void][PrimitiveType.Void]
 *
 * @return `this` *(for chaining)*
 *
 * @throws [IllegalArgumentException] if the [return type][MethodType.returnType] is not [void][PrimitiveType.Void]
 */
@AsmKtDsl
public fun BytecodeMethod.invokeConstructor(
    owner: ReferenceType,
    type: MethodType = MethodType.VOID,
): BytecodeMethod = apply {
    require(type.returnType is PrimitiveType.Void) { "return type of a constructor must be 'void', was '$type'." }
    invokeSpecial(owner, "<init>", type)
}

/**
 * Pushes the instruction for putting the `this` pointer onto the stack.
 *
 * @throws [IllegalStateException] if `this` block represents a static method
 */
@AsmKtDsl
public fun BytecodeMethod.loadThis(): BytecodeMethod = apply {
    check(!isStatic) { "Can't load a 'this' pointer in a static context." }
    loadLocal(0, parentType)
}

@AsmKtDsl
public fun BytecodeMethod.throwException(
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
public fun BytecodeMethod.box(type: PrimitiveType): BytecodeMethod = apply {
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

/**
 * Attempts to push the instruction for unboxing the top-most value on the stack to the given [type].
 *
 * Note that if `type` is [void][PrimitiveType.Void] the no instructions are pushed onto the stack. In the case that
 * `type` is a [ReferenceType] then the value is simply cast to `type`.
 *
 * @param [type] the type to unbox to, must not be an [ArrayType]
 *
 * @throws [IllegalArgumentException] if [type] is an [ArrayType]
 *
 * @see [box]
 */
@AsmKtDsl
public fun BytecodeMethod.unbox(type: FieldType): BytecodeMethod = apply {
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
 * Pushes the instructions needed to box the value currently at the top of the stack using the `valueOf` functions
 * located in the boxed variants for primitive types onto the stack.
 *
 * Note that for [void][PrimitiveType.Void] types, this will push `null` onto the stack, as we can't actually create
 * instances of the `Void` class.
 *
 * @param [type] the type to box
 */
@AsmKtDsl
public fun BytecodeMethod.pushValueOf(type: PrimitiveType): BytecodeMethod = apply {
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
 * Pushes the instructions for a `switch` statement onto the stack.
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
public inline fun BytecodeMethod.tableSwitch(
    keys: IntArray,
    generateCase: (key: Int, end: Label) -> Unit,
    generateDefaultCase: () -> Unit,
    useTable: Boolean = calculateKeyDensity(keys) >= 0.5F,
): BytecodeMethod {
    contract {
        callsInPlace(generateCase, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(generateDefaultCase, InvocationKind.EXACTLY_ONCE)
    }

    return apply {
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
}

@PublishedApi
internal fun calculateKeyDensity(keys: IntArray): Float = when {
    keys.isEmpty() -> 0.0F
    else -> keys.size.toFloat() / (keys[keys.size - 1] - keys[0] + 1)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Byte.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedByte(value: Byte): BytecodeMethod = apply {
    pushByte(value)
    pushValueOf(PrimitiveType.Byte)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Short.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedShort(value: Short): BytecodeMethod = apply {
    pushShort(value)
    pushValueOf(PrimitiveType.Short)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Integer.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedInt(value: Int): BytecodeMethod = apply {
    pushInt(value)
    pushValueOf(PrimitiveType.Int)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Long.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedLong(value: Long): BytecodeMethod = apply {
    pushLong(value)
    pushValueOf(PrimitiveType.Long)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Float.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedFloat(value: Float): BytecodeMethod = apply {
    pushFloat(value)
    pushValueOf(PrimitiveType.Float)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Double.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedDouble(value: Double): BytecodeMethod = apply {
    pushDouble(value)
    pushValueOf(PrimitiveType.Double)
}

/**
 * Pushes the `TRUE` constant from the [Boolean] class onto the stack, or the `FALSE` constant depending on the given
 * [value].
 *
 * @see [pushBoxedTrue]
 * @see [pushBoxedFalse]
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedBoolean(value: Boolean): BytecodeMethod = apply {
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
public fun BytecodeMethod.pushBoxedTrue(): BytecodeMethod = apply {
    getStaticField(ReferenceType.BOOLEAN, "TRUE", ReferenceType.BOOLEAN)
}

/**
 * Pushes the `FALSE` constant from the [Boolean] class onto the stack.
 *
 * @see [pushBoxedBoolean]
 */
@AsmKtDsl
public fun BytecodeMethod.pushBoxedFalse(): BytecodeMethod = apply {
    getStaticField(ReferenceType.BOOLEAN, "FALSE", ReferenceType.BOOLEAN)
}

@AsmKtDsl
public fun BytecodeMethod.pushBooleanValue(): BytecodeMethod = apply {
    checkCast(ReferenceType.BOOLEAN)
    invokeVirtual(ReferenceType.BOOLEAN, "booleanValue", MethodType.BOOLEAN)
}

@AsmKtDsl
public inline fun <reified A : Annotation> BytecodeMethod.defineParameterAnnotation(
    index: Int,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit = {},
) {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    defineParameterAnnotation(index, ReferenceType<A>(), isVisible, allowRepeats).apply(scope)
}

@AsmKtDsl
public inline fun BytecodeMethod.defineParameterAnnotation(
    index: Int,
    type: ReferenceType,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit,
) {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    defineParameterAnnotation(index, type, isVisible, allowRepeats).apply(scope)
}

/**
 * Scopes into the [block][BytecodeMethod.useBlock] this `BytecodeMethod` wraps around.
 */
@AsmKtDsl
public inline fun BytecodeMethod.useBlock(scope: BytecodeBlock.() -> Unit) {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    block.apply(scope)
}