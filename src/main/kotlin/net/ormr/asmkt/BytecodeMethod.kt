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

@file:Suppress("unused", "SpellCheckingInspection")

package net.ormr.asmkt

import krautils.collections.mapToTypedArray
import net.ormr.asmkt.BytecodeMethod.ComparisonMode.*
import net.ormr.asmkt.types.*
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.lang.invoke.MethodHandles
import org.objectweb.asm.Type as AsmType

/**
 * Represents a method containing various instructions.
 *
 * See  [Chapter 6. The Java Virtual Machine Instruction Set](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html)
 * for documentation regarding all the instructions available in this class.
 */
@AsmKt
data class BytecodeMethod internal constructor(
    val name: String,
    override val access: Int,
    val type: net.ormr.asmkt.types.MethodType,
    val signature: String?,
    val exceptions: List<net.ormr.asmkt.types.ReferenceType>,
    val parent: BytecodeClass,
    val block: BytecodeBlock = BytecodeBlock(),
) : AccessibleBytecode, AnnotatableBytecode, AnnotatableTypeBytecode {
    private companion object {
        private val primitiveClassHandle: Handle by lazy {
            constantBootstrapsHandleOf(
                "primitiveClass",
                net.ormr.asmkt.types.MethodType.of(
                    net.ormr.asmkt.types.ReferenceType.CLASS,
                    net.ormr.asmkt.types.ReferenceType<MethodHandles.Lookup>(),
                    net.ormr.asmkt.types.ReferenceType.STRING,
                    net.ormr.asmkt.types.ReferenceType.CLASS
                )
            )
        }
    }

    /**
     * Returns `true` if `this` method is [synchronized][Modifiers.SYNCHRONIZED], otherwise `false`.
     */
    val isSynchronized: Boolean
        get() = access and Modifiers.SYNCHRONIZED != 0

    /**
     * Returns `true` if `this` method is [bridge][Modifiers.BRIDGE], otherwise `false`.
     */
    val isBridge: Boolean
        get() = access and Modifiers.BRIDGE != 0

    /**
     * Returns `true` if `this` method is [varargs][Modifiers.VARARGS], otherwise `false`.
     */
    val isVarargs: Boolean
        get() = access and Modifiers.VARARGS != 0

    /**
     * Returns `true` if `this` method is [native][Modifiers.NATIVE], otherwise `false`.
     */
    val isNative: Boolean
        get() = access and Modifiers.NATIVE != 0

    /**
     * Returns `true` if `this` method is [strict][Modifiers.STRICT], otherwise `false`.
     */
    val isStrict: Boolean
        get() = access and Modifiers.STRICT != 0

    /**
     * Returns the return type of `this` method.
     */
    val returnType: net.ormr.asmkt.types.FieldType
        get() = type.returnType

    /**
     * Returns a list containing the types of the parameters of `this` method.
     */
    val parameterTypes: List<net.ormr.asmkt.types.FieldType>
        get() = type.argumentTypes

    /**
     * Returns how many parameters `this` method has.
     */
    val arity: Int
        get() = parameterTypes.size

    /**
     * Returns the [type][BytecodeClass.type] of `this` methods [parent].
     */
    val parentType: net.ormr.asmkt.types.ReferenceType
        get() = parent.type


    // TODO: document valid types
    var defaultAnnotationValue: Any? = null
        set(value) {
            require(isValidAnnotationValue(value)) { "Value '$value' is not a valid annotation value (${value?.javaClass?.name})" }
            field = value
        }

    private fun isValidAnnotationValue(value: Any?): Boolean = when (value) {
        null -> true
        is Boolean, is Byte, is Short, is Int, is Long, is Float, is Double, is String, is AsmType, is net.ormr.asmkt.types.FieldType -> true
        is AnnotationNode, is BytecodeAnnotation -> true
        is List<*> -> value.all(this::isValidAnnotationValue)
        is Array<*> -> TODO("or an two elements String array (for enumeration values)")
        else -> false
    }

    // method annotations
    private val visibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    // method type annotations
    private val visibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    // try catch blocks
    private val tryCatchBlocks: MutableList<TryCatchBlockNode> = mutableListOf()

    // local variables
    private val localVariableNodes: MutableList<LocalVariableNode> = mutableListOf()

    // parameters
    private val parameterNodes: MutableList<ParameterNode> = mutableListOf()
    private val visibleParameterAnnotations: MutableMap<Int, MutableList<BytecodeAnnotation>> = hashMapOf()
    private val invisibleParameterAnnotations: MutableMap<Int, MutableList<BytecodeAnnotation>> = hashMapOf()

    // type parameters
    private val visibleTypeParameterAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleTypeParameterAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    @get:JvmName("returns")
    val returns: Boolean
        get() = block.returns

    /**
     * Returns `true` if no instructions have been added to `this` block, otherwise `false`.
     */
    fun isEmpty(): Boolean = tryCatchBlocks.isEmpty() && localVariableNodes.isEmpty() && parameterNodes.isEmpty()
        && visibleAnnotations.isEmpty() && block.isEmpty()

    /**
     * Returns `true` if any instructions have been added to `this` block, otherwise `false`.
     */
    fun isNotEmpty(): Boolean = !(isEmpty())

    @JvmSynthetic
    internal fun toComponentString(): String = buildString {
        append(name)
        parameterTypes.joinTo(this, prefix = "(", postfix = ")")
        append(" -> ")
        append(returnType)
    }

    // see https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html for documentation on all the different
    // instructions

    // -- CONST INSTRUCTIONS -- \\
    private fun isValidLdcValue(value: Any): Boolean =
        value is String || value is Int || value is Long || value is Float || value is Double || value is Type
            || value is AsmType || value is Handle || value is ConstantDynamic

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * This function only accepts a very limited subset of values and it will also *directly* push `value` onto the
     * stack without any optimizations done, most of the time [push] is most likely preferred as it accepts more
     * types of `value` and it also creates optimized instructions depending on the type of `value`.
     *
     * @param [value] the value to create the `LDC` instruction for, must be one of the following types; [String],
     * [Int], [Long], [Float], [Double], [Type], [Handle] or [ConstantDynamic]
     *
     * @throws [IllegalArgumentException] if [value] is not one of the allowed types
     *
     * @see [push]
     */
    @AsmKt
    fun ldc(value: Any): BytecodeMethod = apply {
        require(isValidLdcValue(value)) { "Can't push value '$value' (${value.javaClass.name}) onto the stack." }
        block.ldc(value)
    }

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * Unlike [ldc] this function accepts a wider variety of types for `value` and it also outputs optimized bytecode
     * where possible.
     *
     * For example, `aconst(3)` pushes the `ICONST_3` instruction onto the stack, and `aconst(true)` pushes the
     * `ICONST_1` instruction onto the stack.
     *
     * @param [value] the value to generate the instruction for, must be one of the following types; `null`, [Boolean],
     * [Char], [String], [Byte], [Short], [Int], [Long], [Float], [Double], [Type], [Handle] or [ConstantDynamic]
     *
     * @throws [IllegalArgumentException] if [value] is not one of the allowed types
     *
     * @see [ldc]
     * @see [pushBoolean]
     * @see [pushString]
     * @see [pushByte]
     * @see [pushChar]
     * @see [pushShort]
     * @see [pushInt]
     * @see [pushLong]
     * @see [pushFloat]
     * @see [pushDouble]
     * @see [pushType]
     * @see [pushHandle]
     * @see [pushConstantDynamic]
     */
    @AsmKt
    fun push(value: Any?): BytecodeMethod = apply {
        when (value) {
            null -> block.aconst_null()
            is Boolean -> pushBoolean(value)
            is Char -> pushInt(value.toInt())
            is String -> pushString(value)
            is net.ormr.asmkt.types.FieldType -> pushType(value)
            is Handle -> pushHandle(value)
            is ConstantDynamic -> pushConstantDynamic(value)
            is Byte -> pushByte(value)
            is Short -> pushShort(value)
            is Int -> pushInt(value)
            is Long -> pushLong(value)
            is Float -> pushFloat(value)
            is Double -> pushDouble(value)
            else -> throw IllegalArgumentException("Can't push value '$value' (${value.javaClass.name}) onto the stack.")
        }
    }

    /**
     * Pushes the `ICONST_1` instruction onto the stack if [value] is `true`, or the `ICONST_0` instruction if `value`
     * is `false`.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushBoolean(value: Boolean): BytecodeMethod = apply {
        if (value) {
            block.iconst_1()
        } else {
            block.iconst_0()
        }
    }

    /**
     * Pushes an `LDC` instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushString(value: String): BytecodeMethod = apply {
        block.ldc(value)
    }

    /**
     * Pushes a `BIPUSH` instruction for the given [value] onto the stack.
     *
     * If `value` is either `-1`, `0`, `1`, `2`, `3`, `4` or `5` then a `ICONST_X` instruction will be pushed onto
     * the stack instead.
     *
     * @return `this` *(for chaining)*
     *
     * @see [pushInt]
     */
    @AsmKt
    fun pushByte(value: Byte): BytecodeMethod = pushInt(value.toInt())

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     *
     * @see [pushInt]
     */
    @AsmKt
    fun pushChar(value: Char): BytecodeMethod = pushInt(value.toInt())

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     *
     * @see [pushInt]
     */
    @AsmKt
    fun pushShort(value: Short): BytecodeMethod = pushInt(value.toInt())

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * The following list lists the special behvaviour of this function depending on the actual value of `value`, if
     * one entry passes then the rest do not get executed, meaning that if this function was invoked with a `byte` of
     * value `3` then a `ICONST_3` instruction would be pushed onto the stack rather than a `BIPUSH` instruction with
     * an `operand` of `3`.
     *
     * - If `value` is either `-1`, `0`, `1`, `2`, `3`, `4` or `5` then a `ICONST_X` instruction will be pushed onto
     * the stack instead.
     * - If `value` fits within the size requirements of a `byte` then a `BIPUSH` instruction will be pushed onto the
     * stack instead.
     * - If `value` fits within the size requirements of a `short` then a `SIPUSH` instruction will be pushed onto the
     * stack instead.
     *
     * If none of the above requirements are satisifed then a `LDC` instruction for `value` will be pushed onto the
     * stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushInt(value: Int): BytecodeMethod = apply {
        when {
            value >= -1 && value <= 5 -> block.addInstruction(ICONST_0 + value)
            value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE -> block.bipush(value)
            value >= Short.MIN_VALUE && value <= Short.MAX_VALUE -> block.sipush(value)
            else -> block.ldc(value)
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either `0L` or `1L` then a `LCONST_X` instruction will be pushed onto the
     * stack instead.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushLong(value: Long): BytecodeMethod = apply {
        when (value) {
            0L -> block.lconst_0()
            1L -> block.lconst_1()
            // TODO: should we push the value as an 'int' otherwise?
            else -> block.ldc(value)
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either *exactly* `+0.0F`, `1.0F` or `2.0F` then a `FCONST_X` instruction will be pushed onto the
     * stack instead.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushFloat(value: Float): BytecodeMethod = apply {
        val bits = value.toBits()
        if (bits == 0 || bits == 0x3F800000 || bits == 0x40000000) { // 0..2
            block.addInstruction(FCONST_0 + value.toInt())
        } else {
            block.ldc(value)
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either *exactly* `+0.0` or `1.0` then a `DCONST_X` instruction will be pushed onto the stack
     * instead.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushDouble(value: Double): BytecodeMethod = apply {
        val bits = value.toBits()

        if (bits == 0L || bits == 0x3FF0000000000000L) { // +0.0 and 1.0
            block.addInstruction(DCONST_0 + value.toInt())
        } else {
            block.ldc(value)
        }
    }

    /**
     * Pushes a `LDC` instruction to retrieve the `class` instance for the given [value] onto the stack.
     *
     * If `value` is [a primitive][PrimitiveType] then a `GETSTATIC` instruction pointing to the `TYPE` field located
     * in the [wrapper][PrimitiveType.toBoxed] class for `value` is pushed onto the stack instead.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushType(value: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        if (value is net.ormr.asmkt.types.PrimitiveType) {
            pushConstantDynamic(value.descriptor, net.ormr.asmkt.types.ReferenceType.CLASS, primitiveClassHandle)
        } else {
            ldc(value.toAsmType())
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushHandle(value: Handle): BytecodeMethod = apply {
        ldc(value)
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun pushConstantDynamic(value: ConstantDynamic): BytecodeMethod = apply {
        ldc(value)
    }

    // TODO: documentation
    @AsmKt
    fun pushConstantDynamic(
        name: String,
        type: net.ormr.asmkt.types.FieldType,
        bootStrapMethod: Handle,
        vararg bootStrapMethodArguments: Any,
    ): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'" }
        val arguments = bootStrapMethodArguments.replaceTypes()
        pushConstantDynamic(ConstantDynamic(name, type.descriptor, bootStrapMethod, *arguments))
    }

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKt
    fun loadLocal(index: Int, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addVarInstruction(type.getOpcode(ILOAD), index)
    }

    @AsmKt
    fun arrayLoad(type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IALOAD))
    }

    // -- STORE INSTRUCTIONS -- \\
    @AsmKt
    fun storeLocal(index: Int, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addVarInstruction(type.getOpcode(ISTORE), index)
    }

    @AsmKt
    fun arrayStore(type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IASTORE))
    }

    // -- INT INSTRUCTIONS -- \\
    // TODO: find a better name?
    @AsmKt
    fun ret(operand: Int): BytecodeMethod = apply {
        block.ret(operand)
    }

    // -- ARRAY INSTRUCTIONS -- \\
    /**
     * Adds an instruction to generate and push a new array of the given [type] onto the stack.
     *
     * @param [type] the type of the desired array
     *
     * @return `this` *(for chaining)*
     *
     * @throws [IllegalArgumentException] if [type] is not [a primitive][Type.isDefinablePrimitive] or [object][Type.isObject]
     * type
     */
    @AsmKt
    fun newArray(type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }

        if (type is TypeWithInternalName) {
            block.anewarray(type.internalName)
            return this
        }

        val arrayType = when (type) {
            is net.ormr.asmkt.types.PrimitiveBoolean -> T_BOOLEAN
            is net.ormr.asmkt.types.PrimitiveChar -> T_CHAR
            is net.ormr.asmkt.types.PrimitiveByte -> T_BYTE
            is net.ormr.asmkt.types.PrimitiveShort -> T_SHORT
            is net.ormr.asmkt.types.PrimitiveInt -> T_INT
            is net.ormr.asmkt.types.PrimitiveFloat -> T_FLOAT
            is net.ormr.asmkt.types.PrimitiveLong -> T_LONG
            is net.ormr.asmkt.types.PrimitiveDouble -> T_DOUBLE
            else -> throw IllegalStateException("Exhaustive switch was not exhaustive for '$type'.")
        }

        block.newarray(arrayType)
    }

    @AsmKt
    fun newMultiArray(type: net.ormr.asmkt.types.FieldType, dimensions: Int): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.multianewarray(type.descriptor, dimensions)
    }

    @AsmKt
    fun arrayLength(): BytecodeMethod = apply {
        block.arraylength()
    }

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    @AsmKt
    fun invokeStatic(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        block.invokestatic(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun invokeSpecial(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        block.invokespecial(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun invokeVirtual(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        block.invokevirtual(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun invokeInterface(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        block.invokeinterface(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun invokeDynamic(
        name: String,
        descriptor: net.ormr.asmkt.types.MethodType,
        bootstrapMethod: Handle,
        vararg bootstrapMethodArguments: Any,
    ): BytecodeMethod = apply {
        val arguments = bootstrapMethodArguments.replaceTypes()
        block.invokedynamic(name, descriptor.descriptor, bootstrapMethod, arguments)
    }

    // local invokes
    @AsmKt
    fun invokeLocalStatic(name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        invokeStatic(parentType, name, type)
    }

    @AsmKt
    fun invokeLocalSpecial(name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        invokeSpecial(parentType, name, type)
    }

    @AsmKt
    fun invokeLocalVirtual(name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        invokeVirtual(parentType, name, type)
    }

    @AsmKt
    fun invokeLocalInterface(name: String, type: net.ormr.asmkt.types.MethodType): BytecodeMethod = apply {
        invokeInterface(parentType, name, type)
    }

    // -- RETURN INSTRUCTIONS -- \\
    /**
     * Generates the appropriate return instruction based on the [returnType] of `this` method.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun returnValue(): BytecodeMethod = apply {
        block.addInstruction(returnType.getOpcode(IRETURN))
    }

    // -- FIELD INSTRUCTIONS -- \\
    // TODO: 'getStaticField'?
    @AsmKt
    fun getStatic(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'descriptor' must not be 'void'." }
        block.getstatic(owner.internalName, name, type.descriptor)
    }

    // TODO: 'putStaticField'?
    @AsmKt
    fun putStatic(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'descriptor' must not be 'void'." }
        block.putstatic(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun getField(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'descriptor' must not be 'void'." }
        block.getfield(owner.internalName, name, type.descriptor)
    }

    @AsmKt
    fun putField(owner: net.ormr.asmkt.types.ReferenceType, name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'descriptor' must not be 'void'." }
        block.putfield(owner.internalName, name, type.descriptor)
    }

    // local fields
    @AsmKt
    fun getLocalStatic(name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        getStatic(parentType, name, type)
    }

    @AsmKt
    fun putLocalStatic(name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        putStatic(parentType, name, type)
    }

    @AsmKt
    fun getLocalField(name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        getField(parentType, name, type)
    }

    @AsmKt
    fun putLocalField(name: String, type: net.ormr.asmkt.types.FieldType): BytecodeMethod = apply {
        putField(parentType, name, type)
    }

    // -- TYPE INSTRUCTIONS -- \\
    @AsmKt
    @JvmName("newInstance")
    fun new(type: net.ormr.asmkt.types.ReferenceType): BytecodeMethod = apply {
        block.new(type.internalName)
    }

    @AsmKt
    fun instanceOf(type: net.ormr.asmkt.types.ReferenceType): BytecodeMethod = apply {
        block.instanceof(type.internalName)
    }

    @AsmKt
    fun checkCast(type: net.ormr.asmkt.types.ReferenceType): BytecodeMethod = apply {
        // there is no point in casting something to 'Object' as everything that isn't a primitive will be extending
        // 'Object' anyways, and we only accept 'ReferenceType' values here anyways, so 'type' can't be a primitive
        if (type != OBJECT) {
            block.checkcast(type.internalName)
        }
    }

    // -- JUMP INSTRUCTIONS -- \\
    @AsmKt
    @JvmName("goTo")
    fun goto(label: Label): BytecodeMethod = apply {
        block.goto(label)
    }

    @AsmKt
    fun ifNonNull(label: Label): BytecodeMethod = apply {
        block.ifnonnull(label)
    }

    @AsmKt
    fun ifNull(label: Label): BytecodeMethod = apply {
        block.ifnull(label)
    }

    // TODO: document that 'ifEqual' and 'ifNotEqual' here actually uses the 'ifCmp' function and not the 'ifeq' and
    //       'ifne' functions as 'ifFalse' and 'ifTrue' uses those instead

    @AsmKt
    fun ifFalse(label: Label): BytecodeMethod = apply {
        block.ifeq(label)
    }

    @AsmKt
    fun ifTrue(label: Label): BytecodeMethod = apply {
        block.ifne(label)
    }

    @AsmKt
    fun ifEqual(type: net.ormr.asmkt.types.FieldType, label: Label): BytecodeMethod = apply {
        ifCmp(type, EQUAL, label)
    }

    @AsmKt
    fun ifNotEqual(type: net.ormr.asmkt.types.FieldType, label: Label): BytecodeMethod = apply {
        ifCmp(type, NOT_EQUAL, label)
    }

    @AsmKt
    fun ifGreater(type: net.ormr.asmkt.types.PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, GREATER, label)
    }

    @AsmKt
    fun ifGreaterOrEqual(type: net.ormr.asmkt.types.PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, GREATER_OR_EQUAL, label)
    }

    @AsmKt
    fun ifLess(type: net.ormr.asmkt.types.PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, LESS, label)
    }

    @AsmKt
    fun ifLessOrEqual(type: net.ormr.asmkt.types.PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, LESS_OR_EQUAL, label)
    }

    @AsmKt
    fun ifObjectsEqual(type: net.ormr.asmkt.types.FieldType, label: Label): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'" }

        invokeStatic(net.ormr.asmkt.types.ReferenceType.OBJECTS, "equals", net.ormr.asmkt.types.MethodType.ofBoolean(OBJECT, OBJECT))
        TODO("implement label usage")
    }

    @AsmKt
    fun ifObjectsNotEqual(type: net.ormr.asmkt.types.FieldType, label: Label): BytecodeMethod = apply {
        ifObjectsEqual(type, label)
        not()
    }

    // TODO: document that the below comparison functions make no attempt to check that the value / type actually
    //       implements 'Comparable' and should therefore not be used before verifying that first

    // a.compareTo(b) > 0
    @AsmKt
    fun ifObjectGreater(label: Label): BytecodeMethod = TODO("ifObjectGreater function")

    // a.compareTo(b) >= 0
    @AsmKt
    fun ifObjectGreaterOrEqual(label: Label): BytecodeMethod = TODO("ifObjectGreaterOrEqual function")

    // a.compareTo(b) < 0
    @AsmKt
    fun ifObjectLess(label: Label): BytecodeMethod = TODO("ifObjectLess function")

    // a.compareTo(b) <= 0
    @AsmKt
    fun ifObjectLessOrEqual(label: Label): BytecodeMethod = TODO("ifObjectLessOrEqual functions")

    private enum class ComparisonMode(val code: Int, val intCode: Int) {
        EQUAL(IFEQ, IF_ICMPEQ),
        NOT_EQUAL(IFNE, IF_ICMPNE),
        GREATER(IFGT, IF_ICMPGT),
        GREATER_OR_EQUAL(IFGE, IF_ICMPGE),
        LESS(IFLT, IF_ICMPLT),
        LESS_OR_EQUAL(IFLE, IF_ICMPLE);
    }

    @AsmKt
    private fun ifCmp(type: net.ormr.asmkt.types.FieldType, mode: ComparisonMode, label: Label): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'" }

        when (type) {
            is net.ormr.asmkt.types.PrimitiveLong -> {
                block.addInstruction(LCMP)
                block.addJumpInstruction(mode.code, label)
            }
            is net.ormr.asmkt.types.PrimitiveDouble -> {
                val instruction = if (mode == GREATER_OR_EQUAL || mode == GREATER) DCMPL else DCMPG
                block.addInstruction(instruction)
                block.addJumpInstruction(mode.code, label)
            }
            is net.ormr.asmkt.types.PrimitiveFloat -> {
                val instruction = if (mode == GREATER_OR_EQUAL || mode == GREATER) FCMPL else FCMPG
                block.addInstruction(instruction)
                block.addJumpInstruction(mode.code, label)
            }
            is net.ormr.asmkt.types.ArrayType, is net.ormr.asmkt.types.ReferenceType -> when (mode) {
                EQUAL -> block.addJumpInstruction(IF_ACMPEQ, label)
                NOT_EQUAL -> block.addJumpInstruction(IF_ACMPNE, label)
                else -> throw IllegalArgumentException("Bad comparison for type $type")
            }
            else -> block.addJumpInstruction(mode.intCode, label)
        }
    }

    @AsmKt
    fun jsr(branch: Label): BytecodeMethod = apply {
        block.jsr(branch)
    }

    // -- PRIMITIVE NUMBER OPERATOR INSTRUCTIONS -- \\
    // generic instructions
    /**
     * Creates the instructions needed to compute the bitwise negation of the value currently at the top of the stack.
     */
    @AsmKt
    fun not(): BytecodeMethod = apply {
        pushBoolean(true)
        xor(net.ormr.asmkt.types.PrimitiveInt)
    }

    @AsmKt
    fun add(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IADD))
    }

    @AsmKt
    fun subtract(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(ISUB))
    }

    @AsmKt
    fun multiply(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IMUL))
    }

    @AsmKt
    fun divide(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IDIV))
    }

    @AsmKt
    fun remainder(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IREM))
    }

    @AsmKt
    fun negate(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(INEG))
    }

    @AsmKt
    fun shiftLeft(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(ISHL))
    }

    @AsmKt
    fun shiftRight(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(ISHR))
    }

    @AsmKt
    fun unsignedShiftRight(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IUSHR))
    }

    @AsmKt
    fun and(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IAND))
    }

    @AsmKt
    fun or(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IOR))
    }

    @AsmKt
    fun xor(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }
        block.addInstruction(type.getOpcode(IXOR))
    }

    @AsmKt
    fun cmpl(type: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }

        if (type is net.ormr.asmkt.types.PrimitiveFloat) {
            block.fcmpl()
        } else {
            block.dcmpl()
        }
    }

    @AsmKt
    fun cmpg(type: net.ormr.asmkt.types.PrimitiveType) {
        require(type !is net.ormr.asmkt.types.PrimitiveVoid) { "'type' must not be 'void'." }

        if (type is net.ormr.asmkt.types.PrimitiveFloat) {
            block.fcmpg()
        } else {
            block.dcmpg()
        }
    }

    /**
     * Pushes the instructions for incrementing the local variable at the given [index] by the given [amount].
     *
     * @param [index] the index of the local variable to increment the value of
     * @param [amount] how much to increment the value of the local variable with
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun incrementInt(index: Int, amount: Int): BytecodeMethod = apply {
        block.iinc(index, amount)
    }

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    /**
     * Creates the instructions needed to cast [from] to [to].
     *
     * @param [from] the primitive type to cast from
     * @param [to] the primitive type to cast to
     */
    @AsmKt
    fun cast(from: net.ormr.asmkt.types.PrimitiveType, to: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = apply {
        if (from != to) {
            when (from) {
                is net.ormr.asmkt.types.PrimitiveDouble -> when (to) {
                    is net.ormr.asmkt.types.PrimitiveLong -> block.d2l()
                    is net.ormr.asmkt.types.PrimitiveFloat -> block.d2f()
                    else -> {
                        block.d2i()
                        cast(net.ormr.asmkt.types.PrimitiveInt, to)
                    }
                }
                is net.ormr.asmkt.types.PrimitiveFloat -> when (to) {
                    is net.ormr.asmkt.types.PrimitiveLong -> block.f2l()
                    is net.ormr.asmkt.types.PrimitiveDouble -> block.f2d()
                    else -> {
                        block.f2i()
                        cast(net.ormr.asmkt.types.PrimitiveInt, to)
                    }
                }
                is net.ormr.asmkt.types.PrimitiveLong -> when (to) {
                    is net.ormr.asmkt.types.PrimitiveFloat -> block.l2f()
                    is net.ormr.asmkt.types.PrimitiveDouble -> block.l2d()
                    else -> {
                        block.l2i()
                        cast(net.ormr.asmkt.types.PrimitiveInt, to)
                    }
                }
                else -> when (to) {
                    is net.ormr.asmkt.types.PrimitiveChar -> block.i2c()
                    is net.ormr.asmkt.types.PrimitiveShort -> block.i2s()
                    is net.ormr.asmkt.types.PrimitiveByte -> block.i2b()
                    is net.ormr.asmkt.types.PrimitiveLong -> block.i2l()
                    is net.ormr.asmkt.types.PrimitiveFloat -> block.i2f()
                    is net.ormr.asmkt.types.PrimitiveDouble -> block.i2d()
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    @AsmKt
    @JvmSynthetic
    infix fun net.ormr.asmkt.types.PrimitiveType.castTo(target: net.ormr.asmkt.types.PrimitiveType): BytecodeMethod = cast(this, target)

    @AsmKt
    @JvmSynthetic
    inline fun <reified From : Any, reified To : Any> cast(): BytecodeMethod =
        cast(net.ormr.asmkt.types.PrimitiveType<From>(), net.ormr.asmkt.types.PrimitiveType<To>())

    // -- TRY CATCH INSTRUCTIONS -- \\
    @AsmKt
    @JvmOverloads
    fun tryCatch(start: Label, end: Label, handler: Label, exception: net.ormr.asmkt.types.ReferenceType? = null): BytecodeMethod =
        addTryCatchBlock(start, end, handler, exception?.internalName)

    // TODO: documentation
    @AsmKt
    @JvmOverloads
    fun catchException(start: Label, end: Label, type: net.ormr.asmkt.types.ReferenceType? = null): BytecodeMethod = apply {
        val catchLabel = Label()
        tryCatch(start, end, catchLabel, type)
        mark(catchLabel)
    }

    @AsmKt
    fun throwException(): BytecodeMethod = apply {
        block.addInstruction(ATHROW)
    }

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Creates an instruction to mark the current point with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun mark(label: Label): BytecodeMethod = apply {
        block.mark(label)
    }

    /**
     * Creates a new [Label] and pushes an instruction to mark the current point with it.
     *
     * @return the created label
     */
    @AsmKt
    fun mark(): Label {
        val label = Label()
        mark(label)
        return label
    }

    /**
     * Creates a new [Label] and marks the given [line] with it.
     *
     * @param [line] the line to mark
     *
     * @return the label created to mark the given [line]
     */
    @AsmKt
    fun markLine(line: Int): Label {
        val label = Label()
        markLine(line, label)
        return label
    }

    /**
     * Marks the given [line] with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun markLine(line: Int, label: Label): BytecodeMethod = apply {
        block.addLineNumber(line, label)
    }

    // -- SWITCH INSTRUCTIONS -- \\
    @AsmKt
    fun lookUpSwitch(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>,
    ): BytecodeMethod = apply {
        block.lookupswitch(defaultLabel, keys, labels)
    }

    @AsmKt
    fun tableSwitch(min: Int, max: Int, defaultLabel: Label, cases: Array<out Label>): BytecodeMethod = apply {
        block.tableswitch(min, max, defaultLabel, cases)
    }

    // -- MONITOR INSTRUCTIONS -- \\
    @AsmKt
    fun monitorEnter(): BytecodeMethod = apply {
        block.monitorenter()
    }

    @AsmKt
    fun monitorExit(): BytecodeMethod = apply {
        block.monitorexit()
    }

    // -- DUP INSTRUCTIONS -- \\
    @AsmKt
    fun dup(): BytecodeMethod = apply {
        block.dup()
    }

    @AsmKt
    fun dupx2(): BytecodeMethod = apply {
        block.dup_x2()
    }

    @AsmKt
    fun dupx1(): BytecodeMethod = apply {
        block.dup_x1()
    }

    @AsmKt
    fun dup2x2(): BytecodeMethod = apply {
        block.dup2_x2()
    }

    @AsmKt
    fun dup2x1(): BytecodeMethod = apply {
        block.dup2_x1()
    }

    @AsmKt
    fun dup2(): BytecodeMethod = apply {
        block.dup2()
    }

    // -- SWAP INSTRUCTIONS -- \\
    @AsmKt
    fun swap(): BytecodeMethod = apply {
        block.swap()
    }

    /**
     * Creates an instruction to swap the two top values currently on the stack.
     */
    @AsmKt
    fun swap(type: Type, prev: Type): BytecodeMethod = apply {
        if (type.size == 1) {
            if (prev.size == 1) {
                swap()
            } else {
                dupx2()
                pop()
            }
        } else {
            if (prev.size == 1) {
                dup2x1()
                pop2()
            } else {
                dup2x2()
                pop2()
            }
        }
    }

    @JvmSynthetic
    @AsmKt
    infix fun Type.swapWith(type: Type): BytecodeMethod = swap(type, this)

    @AsmKt
    fun swap2(): BytecodeMethod = apply {
        dup2x2()
        pop2()
    }

    // -- NOP INSTRUCTIONS -- \\
    @AsmKt
    fun nop(): BytecodeMethod = apply {
        block.nop()
    }

    // -- POP INSTRUCTIONS -- \\
    @AsmKt
    fun pop(): BytecodeMethod = apply {
        block.pop()
    }

    @AsmKt
    fun pop2(): BytecodeMethod = apply {
        block.pop2()
    }

    // -- FRAME INSTRUCTIONS -- \\
    /**
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKt
    fun frame(
        type: Int,
        numLocal: Int,
        local: Array<out Any>,
        numStack: Int,
        stack: Array<out Any>,
    ): BytecodeMethod = apply {
        block.frame(type, numLocal, local, numStack, stack)
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKt
    fun frame(
        type: Int,
        local: Array<out Any>,
        stack: Array<out Any>,
    ): BytecodeMethod = apply {
        frame(type, local.size, local, stack.size, stack)
    }

    /**
     * Adds a compressed frame to the stack
     *
     * @param stackArguments the argument types on the stack, represented as "class path names" e.g
     * `java/lang/RuntimeException`
     */
    @AsmKt
    fun frameSame(vararg stackArguments: Any): BytecodeMethod = apply {
        val type: Int = when {
            stackArguments.isEmpty() -> F_SAME
            stackArguments.size == 1 -> F_SAME1
            else -> throw IllegalArgumentException("same frame should have 0 or 1 arguments on stack, had ${stackArguments.size} arguments.")
        }
        frame(type, 0, emptyArray(), stackArguments.size, stackArguments)
    }

    // -- LOCAL CLASSES -- \\
    fun defineLocalClass(clz: BytecodeClass): BytecodeMethod = apply {
        clz.enclosingMethod = this@BytecodeMethod
    }

    // -- LOCAL VARIABLE INSTRUCTIONS -- \\
    /**
     * Defines the name, type, signature and scope of the local variable stored at the given [index].
     *
     * @param [name] the name of the local variable
     * @param [descriptor] the descriptor of the type of the local variable
     * @param [signature] the signature of the local variable, or `null` if it does not use any generic types
     * @param [start] the start of the block in which the variable belongs
     * @param [end] the end of the block in which the variable belongs
     * @param [index] the index at which the local variable is stored at
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun defineLocalVariable(
        name: String,
        descriptor: String,
        signature: String? = null,
        start: Label,
        end: Label,
        index: Int,
    ): BytecodeMethod = apply {
        localVariableNodes += LocalVariableNode(name, descriptor, signature, start.toNode(), end.toNode(), index)
    }

    /**
     * Defines the name, type, signature and scope of the local variable stored at the given [index].
     *
     * @param [name] the name of the local variable
     * @param [type] the type of the local variable
     * @param [signature] the signature of the local variable, or `null` if it does not use any generic types
     * @param [start] the start of the block in which the variable belongs
     * @param [end] the end of the block in which the variable belongs
     * @param [index] the index at which the local variable is stored at
     *
     * @return `this` *(for chaining)*
     *
     * @throws [IllegalArgumentException] if `this` [is static][isStatic] and [index] is `0`
     */
    @AsmKt
    fun defineLocalVariable(
        name: String,
        type: Type,
        signature: String? = null,
        start: Label,
        end: Label,
        index: Int,
    ): BytecodeMethod = apply {
        defineLocalVariable(name, type.descriptor, signature, start, end, index)
    }

    // -- PARAMETER INSTRUCTIONS -- \\
    /**
     * Sets the `access` of the parameter with the given [name] to the given [access].
     *
     * @param [name] the name of the parameter
     * @param [access] the parameters access flags, valid values are; are [FINAL][Modifiers.FINAL],
     * [SYNTHETIC][Modifiers.SYNTHETIC] and [MANDATED][Modifiers.MANDATED], or any combination of the three.
     */
    @AsmKt
    fun defineParameterAccess(name: String, access: Int): BytecodeMethod = apply {
        parameterNodes += ParameterNode(name, access)
    }

    // TODO: documentation and document throws
    @AsmKt
    @JvmOverloads
    fun defineParameterAnnotation(
        index: Int,
        type: net.ormr.asmkt.types.ReferenceType,
        isVisible: Boolean = true,
        allowRepeats: Boolean = false,
    ): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type)

        if (isVisible) {
            visibleParameterAnnotations.add(index, annotation, allowRepeats)
        } else {
            invisibleParameterAnnotations.add(index, annotation, allowRepeats)
        }

        return annotation
    }

    // TODO: documentation
    @AsmKt
    @JvmOverloads
    fun defineTypeParameterAnnotation(
        typeRef: Int,
        typePath: TypePath,
        type: net.ormr.asmkt.types.ReferenceType,
        isVisible: Boolean = true,
        allowRepeats: Boolean = false,
    ): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type, TypeAnnotationNode(typeRef, typePath, type.descriptor))
        return handleAnnotations(
            this,
            annotation,
            visibleTypeParameterAnnotations,
            invisibleTypeParameterAnnotations,
            isVisible,
            allowRepeats
        )
    }

    // -- ANNOTATABLE BYTECODE -- \\
    @AsmKt
    override fun defineAnnotation(type: net.ormr.asmkt.types.ReferenceType, isVisible: Boolean, allowRepeats: Boolean): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type)
        return handleAnnotations(this, annotation, visibleAnnotations, invisibleAnnotations, isVisible, allowRepeats)
    }

    @AsmKt
    override fun defineTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        annotationType: net.ormr.asmkt.types.ReferenceType,
        isVisible: Boolean,
        allowRepeats: Boolean,
    ): BytecodeAnnotation {
        val node = TypeAnnotationNode(typeRef, typePath, annotationType.descriptor)
        val annotation = BytecodeAnnotation(annotationType, node)
        return handleAnnotations(
            this,
            annotation,
            visibleTypeAnnotations,
            invisibleTypeAnnotations,
            isVisible,
            allowRepeats
        )
    }

    // -- INSTRUCTION FUNCTIONS -- \\
    private fun addTryCatchBlock(
        start: Label,
        end: Label,
        handler: Label,
        type: String?,
    ): BytecodeMethod = apply {
        tryCatchBlocks.add(TryCatchBlockNode(start.toNode(), end.toNode(), handler.toNode(), type))
    }

    // -- UTILS -- \\
    private fun Array<out Any>.replaceTypes(): Array<out Any> = when {
        isEmpty() -> this
        any { it is Type } -> mapToTypedArray { if (it is Type) it.toAsmType() else it }
        else -> this
    }

    /**
     * Returns `this` [Label] wrapped in a [LabelNode].
     */
    private fun Label.toNode(): LabelNode = LabelNode(this)

    private fun MutableMap<Int, MutableList<BytecodeAnnotation>>.add(
        index: Int,
        annotation: BytecodeAnnotation,
        allowRepeats: Boolean,
    ) {
        val entries = getOrPut(index, ::mutableListOf)

        entries += when {
            entries.containsType(annotation.type) -> when {
                allowRepeats -> annotation
                else -> disallowedRepeatingAnnotation(this, annotation.type)
            }
            else -> annotation
        }
    }

    /**
     * Returns a new [MethodNode] based on the contents of `this` bytecode method.
     */
    fun toMethodNode(): MethodNode {
        val node = MethodNode(access, name, type.descriptor, signature, null)

        node.annotationDefault = fixAnnotationValue(defaultAnnotationValue)

        // method exceptions
        node.exceptions = exceptions
            .mapTo(mutableListOf()) { it.internalName }
            .ifEmpty { null }

        // method annotations
        node.visibleAnnotations = visibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }
        node.invisibleAnnotations = invisibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }

        // method type annotations
        node.visibleTypeAnnotations = visibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }
        node.invisibleTypeAnnotations = invisibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }

        // method parameters
        if (parameterNodes.isNotEmpty()) {
            node.parameters = parameterNodes.toMutableList()
        }

        visibleParameterAnnotations.doEach { i, annotation ->
            annotation.node.accept(node.visitParameterAnnotation(i, annotation.type.descriptor, true))
        }

        invisibleParameterAnnotations.doEach { i, annotation ->
            annotation.node.accept(node.visitParameterAnnotation(i, annotation.type.descriptor, false))
        }

        // method type parameters
        for (annotation in visibleTypeParameterAnnotations) {
            val typeNode = annotation.asTypeNode()
            annotation.node.accept(node.visitTypeAnnotation(typeNode.typeRef, typeNode.typePath, typeNode.desc, true))
        }

        for (annotation in invisibleTypeParameterAnnotations) {
            val typeNode = annotation.asTypeNode()
            annotation.node.accept(node.visitTypeAnnotation(typeNode.typeRef, typeNode.typePath, typeNode.desc, false))
        }

        node.instructions.add(block.instructions)

        if (node.tryCatchBlocks == null) {
            node.tryCatchBlocks = mutableListOf()
        }

        node.tryCatchBlocks.addAll(tryCatchBlocks)

        if (node.localVariables == null) {
            node.localVariables = mutableListOf()
        }

        node.localVariables.addAll(localVariableNodes)

        return node
    }

    private fun fixAnnotationValue(value: Any?): Any? = when (value) {
        null -> null
        is net.ormr.asmkt.types.FieldType -> value.toAsmType()
        is BytecodeAnnotation -> value.node
        is List<*> -> value.mapTo(mutableListOf(), this::fixAnnotationValue)
        is Array<*> -> TODO("or an two elements String array (for enumeration values)")
        else -> value
    }

    private inline fun <K, V> Map<K, List<V>>.doEach(action: (K, V) -> Unit) {
        for ((i, annotations) in this) {
            for (annotation in annotations) {
                action(i, annotation)
            }
        }
    }
}