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

@file:Suppress("unused", "SpellCheckingInspection")

package net.ormr.asmkt

import krautils.collections.mapToTypedArray
import net.ormr.asmkt.BytecodeMethod.ComparisonMode.*
import net.ormr.asmkt.types.*
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.lang.invoke.ConstantBootstraps
import org.objectweb.asm.Type as AsmType

/**
 * Represents a method containing various instructions.
 *
 * See  [Chapter 6. The Java Virtual Machine Instruction Set](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html)
 * for documentation regarding all the instructions available in this class.
 */
@AsmKtDsl
public data class BytecodeMethod internal constructor(
    val name: String,
    override val access: Modifier,
    val type: MethodType,
    val signature: String?,
    val exceptions: List<ReferenceType>,
    val parent: BytecodeClass,
    val block: BytecodeBlock = BytecodeBlock(),
) : AccessibleBytecode, AnnotatableBytecode, AnnotatableTypeBytecode, BytecodeVersionContainer {
    private companion object {
        // +0.0F
        private const val POSITIVE_ZERO_FLOAT = 0

        // 1.0F
        private const val ONE_FLOAT = 0x3F800000

        // 2.0F
        private const val TWO_FLOAT = 0x40000000

        // +0.0D
        private const val POSITIVE_ZERO_DOUBLE = 0L

        // 1.0D
        private const val ONE_DOUBLE = 0x3FF0000000000000L

        private val primitiveClassHandle: Handle by lazy {
            InvokeStaticHandle(
                ReferenceType.CONSTANT_BOOTSTRAPS,
                "primitiveClass",
                MethodType.of(
                    ReferenceType.CLASS,
                    ReferenceType.METHOD_HANDLES_LOOKUP,
                    ReferenceType.STRING,
                    ReferenceType.CLASS,
                ),
            )
        }
    }

    override val version: BytecodeVersion
        get() = parent.version

    /**
     * Returns `true` if `this` method is [synchronized][Modifier.SYNCHRONIZED], otherwise `false`.
     */
    public val isSynchronized: Boolean
        get() = Modifier.SYNCHRONIZED in access

    /**
     * Returns `true` if `this` method is [bridge][Modifier.BRIDGE], otherwise `false`.
     */
    public val isBridge: Boolean
        get() = Modifier.BRIDGE in access

    /**
     * Returns `true` if `this` method is [varargs][Modifier.VARARGS], otherwise `false`.
     */
    public val isVarargs: Boolean
        get() = Modifier.VARARGS in access

    /**
     * Returns `true` if `this` method is [native][Modifier.NATIVE], otherwise `false`.
     */
    public val isNative: Boolean
        get() = Modifier.NATIVE in access

    /**
     * Returns `true` if `this` method is [strict][Modifier.STRICT], otherwise `false`.
     */
    public val isStrict: Boolean
        get() = Modifier.STRICT in access

    /**
     * Returns the return type of `this` method.
     */
    public val returnType: FieldType
        get() = type.returnType

    /**
     * Returns a list containing the types of the parameters of `this` method.
     */
    public val parameterTypes: List<FieldType>
        get() = type.argumentTypes

    /**
     * Returns how many parameters `this` method has.
     */
    public val arity: Int
        get() = parameterTypes.size

    /**
     * Returns the [type][BytecodeClass.type] of `this` methods [parent].
     */
    public val parentType: ReferenceType
        get() = parent.type


    // TODO: document valid types
    public var defaultAnnotationValue: Any? = null
        set(value) {
            require(isValidAnnotationValue(value)) { "Value '$value' is not a valid annotation value (${value?.javaClass?.name})" }
            field = value
        }

    private fun isValidAnnotationValue(value: Any?): Boolean = when (value) {
        null -> true
        is Boolean, is Byte, is Short, is Int, is Long, is Float, is Double, is String, is AsmType, is FieldType -> true
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

    val returns: Boolean
        get() = block.returns

    /**
     * Returns `true` if no instructions have been added to `this` block, otherwise `false`.
     */
    public fun isEmpty(): Boolean = tryCatchBlocks.isEmpty() && localVariableNodes.isEmpty() && parameterNodes.isEmpty()
            && visibleAnnotations.isEmpty() && block.isEmpty()

    /**
     * Returns `true` if any instructions have been added to `this` block, otherwise `false`.
     */
    public fun isNotEmpty(): Boolean = !(isEmpty())

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
    @AsmKtDsl
    public fun ldc(value: Any): BytecodeMethod = apply {
        require(isValidLdcValue(value)) { "Can't push value '$value' (${value.javaClass.name}) onto the stack." }
        block.ldc(value)
    }

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * Unlike [ldc] this function accepts a wider variety of types for `value` and it also outputs optimized bytecode
     * where possible.
     *
     * For example, `push(3)` pushes the `ICONST_3` instruction onto the stack, and `push(true)` pushes the
     * `ICONST_1` instruction onto the stack.
     *
     * @param [value] the value to generate the instruction for, must be one of the following types; `null`, [Boolean],
     * [Char], [String], [Byte], [Short], [Int], [Long], [Float], [Double], [Type], [Handle] or [ConstantDynamic]
     *
     * @throws [IllegalArgumentException] if [value] is not one of the allowed types
     *
     * @see [ldc]
     * @see [pushNull]
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
    @AsmKtDsl
    public fun push(value: Any?): BytecodeMethod = apply {
        when (value) {
            null -> pushNull()
            is Boolean -> pushBoolean(value)
            is Char -> pushInt(value.code)
            is String -> pushString(value)
            is FieldType -> pushType(value)
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
     * Pushes the `ACONST_NULL` instruction onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun pushNull(): BytecodeMethod = apply {
        block.aconst_null()
    }

    /**
     * Pushes the `ICONST_1` instruction onto the stack if [value] is `true`, or the `ICONST_0` instruction if `value`
     * is `false`.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun pushBoolean(value: Boolean): BytecodeMethod = apply {
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
    @AsmKtDsl
    public fun pushString(value: String): BytecodeMethod = apply {
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
    @AsmKtDsl
    public fun pushByte(value: Byte): BytecodeMethod = pushInt(value.toInt())

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     *
     * @see [pushInt]
     */
    @AsmKtDsl
    public fun pushChar(value: Char): BytecodeMethod = pushInt(value.code)

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     *
     * @see [pushInt]
     */
    @AsmKtDsl
    public fun pushShort(value: Short): BytecodeMethod = pushInt(value.toInt())

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
    @AsmKtDsl
    public fun pushInt(value: Int): BytecodeMethod = apply {
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
    @AsmKtDsl
    public fun pushLong(value: Long): BytecodeMethod = apply {
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
    @AsmKtDsl
    public fun pushFloat(value: Float): BytecodeMethod = apply {
        val bits = value.toBits()
        if (bits == POSITIVE_ZERO_FLOAT || bits == ONE_FLOAT || bits == TWO_FLOAT) { // 0..2
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
    @AsmKtDsl
    public fun pushDouble(value: Double): BytecodeMethod = apply {
        val bits = value.toBits()

        if (bits == POSITIVE_ZERO_DOUBLE || bits == ONE_DOUBLE) {
            block.addInstruction(DCONST_0 + value.toInt())
        } else {
            block.ldc(value)
        }
    }

    /**
     * Pushes a `LDC` instruction to retrieve the `class` instance for the given [value] onto the stack.
     *
     * If `value` is [a primitive][PrimitiveType] then depending on the [version][BytecodeClass.version] a different
     * behavior will happen:
     * - If `version` >= [BytecodeVersion.JAVA_11] then a [constant dynamic][pushConstantDynamic] pointing to the
     * [ConstantBootstraps.primitiveClass] function is pushed onto the stack.
     * - If `version` < [BytecodeVersion.JAVA_11] then a [GETSTATIC][getStaticField] instruction pointing to the `TYPE` field
     * located in the [wrapper][PrimitiveType.toBoxed] class for `value` is pushed onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun pushType(value: FieldType): BytecodeMethod = apply {
        if (value is PrimitiveType) {
            if (parent.version >= BytecodeVersion.JAVA_11) {
                pushConstantDynamic(value.descriptor, ReferenceType.CLASS, primitiveClassHandle)
            } else {
                getStaticField(value.toBoxed(), "TYPE", ReferenceType.CLASS)
            }
        } else {
            ldc(value.toAsmType())
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun pushHandle(value: Handle): BytecodeMethod = apply {
        ldc(value)
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun pushConstantDynamic(value: ConstantDynamic): BytecodeMethod = apply {
        ldc(value)
    }

    // TODO: documentation
    @AsmKtDsl
    public fun pushConstantDynamic(
        name: String,
        type: FieldType,
        bootStrapMethod: Handle,
        vararg bootStrapMethodArguments: Any,
    ): BytecodeMethod = apply {
        requireMinVersion(BytecodeVersion.JAVA_11, "Constant Dynamic")
        requireNotVoid(type)
        val arguments = bootStrapMethodArguments.replaceTypes()
        pushConstantDynamic(ConstantDynamic(name, type.descriptor, bootStrapMethod, *arguments))
    }

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun loadLocal(index: Int, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addVarInstruction(type.getOpcode(ILOAD), index)
    }

    @AsmKtDsl
    public fun arrayLoad(type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IALOAD))
    }

    // -- STORE INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun storeLocal(index: Int, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addVarInstruction(type.getOpcode(ISTORE), index)
    }

    @AsmKtDsl
    public fun arrayStore(type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IASTORE))
    }

    // -- INT INSTRUCTIONS -- \\
    // TODO: find a better name?
    @AsmKtDsl
    public fun ret(operand: Int): BytecodeMethod = apply {
        block.ret(operand)
    }

    // -- ARRAY INSTRUCTIONS -- \\
    /**
     * Pushes an instruction to create a new array of type [type] onto the stack.
     *
     * @param [type] the type of the array
     *
     * @return `this` *(for chaining)*
     *
     * @throws [IllegalArgumentException] if [type] is [void][PrimitiveType.Void]
     */
    @AsmKtDsl
    public fun newArray(type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type)

        if (type is TypeWithInternalName) {
            block.anewarray(type.internalName)
            return this
        }

        val arrayType = when (type) {
            is PrimitiveType.Boolean -> T_BOOLEAN
            is PrimitiveType.Char -> T_CHAR
            is PrimitiveType.Byte -> T_BYTE
            is PrimitiveType.Short -> T_SHORT
            is PrimitiveType.Int -> T_INT
            is PrimitiveType.Float -> T_FLOAT
            is PrimitiveType.Long -> T_LONG
            is PrimitiveType.Double -> T_DOUBLE
            else -> throw IllegalStateException("Exhaustive switch was not exhaustive for '$type'.")
        }

        block.newarray(arrayType)
    }

    @AsmKtDsl
    public fun newMultiArray(type: FieldType, dimensions: Int): BytecodeMethod = apply {
        requireNotVoid(type)
        block.multianewarray(type.descriptor, dimensions)
    }

    @AsmKtDsl
    public fun arrayLength(): BytecodeMethod = apply {
        block.arraylength()
    }

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun invokeStatic(owner: ReferenceType, name: String, type: MethodType): BytecodeMethod = apply {
        block.invokestatic(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun invokeSpecial(owner: ReferenceType, name: String, type: MethodType): BytecodeMethod = apply {
        block.invokespecial(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun invokeVirtual(owner: ReferenceType, name: String, type: MethodType): BytecodeMethod = apply {
        block.invokevirtual(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun invokeInterface(owner: ReferenceType, name: String, type: MethodType): BytecodeMethod = apply {
        block.invokeinterface(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun invokeDynamic(
        name: String,
        descriptor: MethodType,
        bootstrapMethod: Handle,
        vararg bootstrapMethodArguments: Any,
    ): BytecodeMethod = apply {
        requireMinVersion(BytecodeVersion.JAVA_11, "Invoke Dynamic")
        val arguments = bootstrapMethodArguments.replaceTypes()
        block.invokedynamic(name, descriptor.descriptor, bootstrapMethod, arguments)
    }

    // local invokes
    @AsmKtDsl
    public fun invokeLocalStatic(name: String, type: MethodType): BytecodeMethod = apply {
        invokeStatic(parentType, name, type)
    }

    @AsmKtDsl
    public fun invokeLocalSpecial(name: String, type: MethodType): BytecodeMethod = apply {
        invokeSpecial(parentType, name, type)
    }

    @AsmKtDsl
    public fun invokeLocalVirtual(name: String, type: MethodType): BytecodeMethod = apply {
        invokeVirtual(parentType, name, type)
    }

    @AsmKtDsl
    public fun invokeLocalInterface(name: String, type: MethodType): BytecodeMethod = apply {
        invokeInterface(parentType, name, type)
    }

    // -- RETURN INSTRUCTIONS -- \\
    /**
     * Generates the appropriate return instruction based on the [returnType] of `this` method.
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun returnValue(): BytecodeMethod = apply {
        block.addInstruction(returnType.getOpcode(IRETURN))
    }

    // -- FIELD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun getStaticField(owner: ReferenceType, name: String, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type, name = "descriptor")
        block.getstatic(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun putStaticField(owner: ReferenceType, name: String, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type, name = "descriptor")
        block.putstatic(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun getField(owner: ReferenceType, name: String, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type, name = "descriptor")
        block.getfield(owner.internalName, name, type.descriptor)
    }

    @AsmKtDsl
    public fun putField(owner: ReferenceType, name: String, type: FieldType): BytecodeMethod = apply {
        requireNotVoid(type, name = "descriptor")
        block.putfield(owner.internalName, name, type.descriptor)
    }

    // local fields
    @AsmKtDsl
    public fun getLocalStaticField(name: String, type: FieldType): BytecodeMethod = apply {
        getStaticField(parentType, name, type)
    }

    @AsmKtDsl
    public fun putLocalStaticField(name: String, type: FieldType): BytecodeMethod = apply {
        putStaticField(parentType, name, type)
    }

    @AsmKtDsl
    public fun getLocalField(name: String, type: FieldType): BytecodeMethod = apply {
        getField(parentType, name, type)
    }

    @AsmKtDsl
    public fun putLocalField(name: String, type: FieldType): BytecodeMethod = apply {
        putField(parentType, name, type)
    }

    // -- TYPE INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("newInstance")
    public fun new(type: ReferenceType): BytecodeMethod = apply {
        block.new(type.internalName)
    }

    @AsmKtDsl
    public fun instanceOf(type: ReferenceType): BytecodeMethod = apply {
        block.instanceof(type.internalName)
    }

    @AsmKtDsl
    public fun checkCast(type: ReferenceType): BytecodeMethod = apply {
        // there is no point in casting something to 'Object' as everything that isn't a primitive will be extending
        // 'Object' anyway, and we only accept 'ReferenceType' values here anyways, so 'type' can't be a primitive
        if (type != OBJECT) {
            block.checkcast(type.internalName)
        }
    }

    // -- JUMP INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("goTo")
    public fun goto(label: Label): BytecodeMethod = apply {
        block.goto(label)
    }

    @AsmKtDsl
    public fun ifNonNull(label: Label): BytecodeMethod = apply {
        block.ifnonnull(label)
    }

    @AsmKtDsl
    public fun ifNull(label: Label): BytecodeMethod = apply {
        block.ifnull(label)
    }

    // TODO: document that 'ifEqual' and 'ifNotEqual' here actually uses the 'ifCmp' function and not the 'ifeq' and
    //       'ifne' functions as 'ifFalse' and 'ifTrue' uses those instead

    @AsmKtDsl
    public fun ifFalse(label: Label): BytecodeMethod = apply {
        block.ifeq(label)
    }

    @AsmKtDsl
    public fun ifTrue(label: Label): BytecodeMethod = apply {
        block.ifne(label)
    }

    @AsmKtDsl
    public fun ifEqual(type: FieldType, label: Label): BytecodeMethod = apply {
        ifCmp(type, EQUAL, label)
    }

    @AsmKtDsl
    public fun ifNotEqual(type: FieldType, label: Label): BytecodeMethod = apply {
        ifCmp(type, NOT_EQUAL, label)
    }

    @AsmKtDsl
    public fun ifGreater(type: PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, GREATER, label)
    }

    @AsmKtDsl
    public fun ifGreaterOrEqual(type: PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, GREATER_OR_EQUAL, label)
    }

    @AsmKtDsl
    public fun ifLess(type: PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, LESS, label)
    }

    @AsmKtDsl
    public fun ifLessOrEqual(type: PrimitiveType, label: Label): BytecodeMethod = apply {
        ifCmp(type, LESS_OR_EQUAL, label)
    }

    private enum class ComparisonMode(val code: Int, val intCode: Int) {
        EQUAL(IFEQ, IF_ICMPEQ),
        NOT_EQUAL(IFNE, IF_ICMPNE),
        GREATER(IFGT, IF_ICMPGT),
        GREATER_OR_EQUAL(IFGE, IF_ICMPGE),
        LESS(IFLT, IF_ICMPLT),
        LESS_OR_EQUAL(IFLE, IF_ICMPLE);
    }

    @AsmKtDsl
    private fun ifCmp(type: FieldType, mode: ComparisonMode, label: Label): BytecodeMethod = apply {
        requireNotVoid(type)

        when (type) {
            is PrimitiveType.Long -> {
                block.addInstruction(LCMP)
                block.addJumpInstruction(mode.code, label)
            }
            is PrimitiveType.Double -> {
                val instruction = if (mode == GREATER_OR_EQUAL || mode == GREATER) DCMPL else DCMPG
                block.addInstruction(instruction)
                block.addJumpInstruction(mode.code, label)
            }
            is PrimitiveType.Float -> {
                val instruction = if (mode == GREATER_OR_EQUAL || mode == GREATER) FCMPL else FCMPG
                block.addInstruction(instruction)
                block.addJumpInstruction(mode.code, label)
            }
            is ArrayType, is ReferenceType -> when (mode) {
                EQUAL -> block.addJumpInstruction(IF_ACMPEQ, label)
                NOT_EQUAL -> block.addJumpInstruction(IF_ACMPNE, label)
                else -> throw IllegalArgumentException("Bad comparison for type $type")
            }
            else -> block.addJumpInstruction(mode.intCode, label)
        }
    }

    @AsmKtDsl
    public fun jsr(branch: Label): BytecodeMethod = apply {
        block.jsr(branch)
    }

    // -- PRIMITIVE NUMBER OPERATOR INSTRUCTIONS -- \\
    // generic instructions
    /**
     * Creates the instructions needed to compute the bitwise negation of the value currently at the top of the stack.
     */
    @AsmKtDsl
    public fun not(): BytecodeMethod = apply {
        pushBoolean(true)
        xor(PrimitiveType.Int)
    }

    @AsmKtDsl
    public fun add(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IADD))
    }

    @AsmKtDsl
    public fun subtract(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(ISUB))
    }

    @AsmKtDsl
    public fun multiply(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IMUL))
    }

    @AsmKtDsl
    public fun divide(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IDIV))
    }

    @AsmKtDsl
    public fun remainder(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IREM))
    }

    @AsmKtDsl
    public fun negate(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(INEG))
    }

    @AsmKtDsl
    public fun shiftLeft(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(ISHL))
    }

    @AsmKtDsl
    public fun shiftRight(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(ISHR))
    }

    @AsmKtDsl
    public fun unsignedShiftRight(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IUSHR))
    }

    @AsmKtDsl
    public fun and(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IAND))
    }

    @AsmKtDsl
    public fun or(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IOR))
    }

    @AsmKtDsl
    public fun xor(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)
        block.addInstruction(type.getOpcode(IXOR))
    }

    @AsmKtDsl
    public fun cmpl(type: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(type)

        if (type is PrimitiveType.Float) {
            block.fcmpl()
        } else {
            block.dcmpl()
        }
    }

    @AsmKtDsl
    public fun cmpg(type: PrimitiveType) {
        requireNotVoid(type)

        if (type is PrimitiveType.Float) {
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
    @AsmKtDsl
    public fun incrementInt(index: Int, amount: Int): BytecodeMethod = apply {
        block.iinc(index, amount)
    }

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    /**
     * Creates the instructions needed to cast [from] to [to].
     *
     * @param [from] the primitive type to cast from
     * @param [to] the primitive type to cast to
     */
    @AsmKtDsl
    public fun cast(from: PrimitiveType, to: PrimitiveType): BytecodeMethod = apply {
        requireNotVoid(from, "from")
        requireNotVoid(to, "from")
        if (from != to) {
            when (from) {
                is PrimitiveType.Double -> when (to) {
                    is PrimitiveType.Long -> block.d2l()
                    is PrimitiveType.Float -> block.d2f()
                    else -> {
                        block.d2i()
                        cast(PrimitiveType.Int, to)
                    }
                }
                is PrimitiveType.Float -> when (to) {
                    is PrimitiveType.Long -> block.f2l()
                    is PrimitiveType.Double -> block.f2d()
                    else -> {
                        block.f2i()
                        cast(PrimitiveType.Int, to)
                    }
                }
                is PrimitiveType.Long -> when (to) {
                    is PrimitiveType.Float -> block.l2f()
                    is PrimitiveType.Double -> block.l2d()
                    else -> {
                        block.l2i()
                        cast(PrimitiveType.Int, to)
                    }
                }
                else -> when (to) {
                    is PrimitiveType.Char -> block.i2c()
                    is PrimitiveType.Short -> block.i2s()
                    is PrimitiveType.Byte -> block.i2b()
                    is PrimitiveType.Long -> block.i2l()
                    is PrimitiveType.Float -> block.i2f()
                    is PrimitiveType.Double -> block.i2d()
                    else -> error("Unknown type: $to")
                }
            }
        }
    }

    @AsmKtDsl
    public infix fun PrimitiveType.castTo(target: PrimitiveType): BytecodeMethod = cast(this, target)

    @AsmKtDsl
    public inline fun <reified From : Any, reified To : Any> cast(): BytecodeMethod =
        cast(PrimitiveType<From>(), PrimitiveType<To>())

    // -- TRY CATCH INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun tryCatch(start: Label, end: Label, handler: Label, exception: ReferenceType? = null): BytecodeMethod =
        addTryCatchBlock(start, end, handler, exception?.internalName)

    // TODO: documentation
    @AsmKtDsl
    public fun catchException(start: Label, end: Label, type: ReferenceType? = null): BytecodeMethod = apply {
        val catchLabel = Label()
        tryCatch(start, end, catchLabel, type)
        mark(catchLabel)
    }

    @AsmKtDsl
    public fun throwException(): BytecodeMethod = apply {
        block.addInstruction(ATHROW)
    }

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Creates an instruction to mark the current point with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun mark(label: Label): BytecodeMethod = apply {
        block.mark(label)
    }

    /**
     * Creates a new [Label] and pushes an instruction to mark the current point with it.
     *
     * @return the created label
     */
    @AsmKtDsl
    public fun mark(): Label {
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
    @AsmKtDsl
    public fun markLine(line: Int): Label {
        val label = Label()
        markLine(line, label)
        return label
    }

    /**
     * Marks the given [line] with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun markLine(line: Int, label: Label): BytecodeMethod = apply {
        block.addLineNumber(line, label)
    }

    // -- SWITCH INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun lookUpSwitch(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>,
    ): BytecodeMethod = apply {
        block.lookupswitch(defaultLabel, keys, labels)
    }

    @AsmKtDsl
    public fun tableSwitch(min: Int, max: Int, defaultLabel: Label, cases: Array<out Label>): BytecodeMethod = apply {
        block.tableswitch(min, max, defaultLabel, cases)
    }

    // -- MONITOR INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun monitorEnter(): BytecodeMethod = apply {
        block.monitorenter()
    }

    @AsmKtDsl
    public fun monitorExit(): BytecodeMethod = apply {
        block.monitorexit()
    }

    // -- DUP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun dup(): BytecodeMethod = apply {
        block.dup()
    }

    @AsmKtDsl
    public fun dupx2(): BytecodeMethod = apply {
        block.dup_x2()
    }

    @AsmKtDsl
    public fun dupx1(): BytecodeMethod = apply {
        block.dup_x1()
    }

    @AsmKtDsl
    public fun dup2x2(): BytecodeMethod = apply {
        block.dup2_x2()
    }

    @AsmKtDsl
    public fun dup2x1(): BytecodeMethod = apply {
        block.dup2_x1()
    }

    @AsmKtDsl
    public fun dup2(): BytecodeMethod = apply {
        block.dup2()
    }

    // -- SWAP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun swap(): BytecodeMethod = apply {
        block.swap()
    }

    /**
     * Creates an instruction to swap the two top values currently on the stack.
     */
    @AsmKtDsl
    public fun swap(type: Type, prev: Type): BytecodeMethod = apply {
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

    @AsmKtDsl
    public infix fun Type.swapWith(type: Type): BytecodeMethod = swap(type, this)

    @AsmKtDsl
    public fun swap2(): BytecodeMethod = apply {
        dup2x2()
        pop2()
    }

    // -- NOP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun nop(): BytecodeMethod = apply {
        block.nop()
    }

    // -- POP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun pop(): BytecodeMethod = apply {
        block.pop()
    }

    @AsmKtDsl
    public fun pop2(): BytecodeMethod = apply {
        block.pop2()
    }

    // -- FRAME INSTRUCTIONS -- \\
    /**
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKtDsl
    public fun frame(
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
    @AsmKtDsl
    public fun frame(
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
    @AsmKtDsl
    public fun frameSame(vararg stackArguments: Any): BytecodeMethod = apply {
        val type: Int = when {
            stackArguments.isEmpty() -> F_SAME
            stackArguments.size == 1 -> F_SAME1
            else -> throw IllegalArgumentException("same frame should have 0 or 1 arguments on stack, had ${stackArguments.size} arguments.")
        }
        frame(type, 0, emptyArray(), stackArguments.size, stackArguments)
    }

    // -- LOCAL CLASSES -- \\
    public fun defineLocalClass(clz: BytecodeClass): BytecodeMethod = apply {
        clz.enclosingMethod = this@BytecodeMethod
    }

    // -- LOCAL VARIABLE INSTRUCTIONS -- \\
    /**
     * Defines the name, type, signature and scope of the local variable stored at the given [index].
     *
     * @param [index] the index at which the local variable is stored at
     * @param [name] the name of the local variable
     * @param [descriptor] the descriptor of the type of the local variable
     * @param [start] the start of the block in which the variable belongs
     * @param [end] the end of the block in which the variable belongs
     * @param [signature] the signature of the local variable, or `null` if it does not use any generic types
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun defineLocalVariable(
        index: Int,
        name: String,
        descriptor: String,
        start: Label,
        end: Label,
        signature: String? = null,
    ): BytecodeMethod = apply {
        localVariableNodes += LocalVariableNode(name, descriptor, signature, start.toNode(), end.toNode(), index)
    }

    /**
     * Defines the name, type, signature and scope of the local variable stored at the given [index].
     *
     * @param [index] the index at which the local variable is stored at
     * @param [name] the name of the local variable
     * @param [type] the type of the local variable
     * @param [start] the start of the block in which the variable belongs
     * @param [end] the end of the block in which the variable belongs
     * @param [signature] the signature of the local variable, or `null` if it does not use any generic types
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun defineLocalVariable(
        index: Int,
        name: String,
        type: FieldType,
        start: Label,
        end: Label,
        signature: String? = null,
    ): BytecodeMethod = apply {
        defineLocalVariable(index, name, type.descriptor, start, end, signature)
    }

    // -- PARAMETER INSTRUCTIONS -- \\
    /**
     * Sets the `access` of the parameter with the given [name] to the given [access].
     *
     * @param [name] the name of the parameter
     * @param [access] the parameters access flags, valid values are; are [FINAL][Modifier.FINAL],
     * [SYNTHETIC][Modifier.SYNTHETIC] and [MANDATED][Modifier.MANDATED], or any combination of the three.
     */
    @AsmKtDsl
    public fun defineParameterAccess(name: String, access: Modifier): BytecodeMethod = apply {
        parameterNodes += ParameterNode(name, access.asInt())
    }

    // TODO: documentation and document throws
    @AsmKtDsl
    public fun defineParameterAnnotation(
        index: Int,
        type: ReferenceType,
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
    @AsmKtDsl
    public fun defineTypeParameterAnnotation(
        typeRef: Int,
        typePath: TypePath,
        type: ReferenceType,
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
    @AsmKtDsl
    override fun defineAnnotation(type: ReferenceType, isVisible: Boolean, allowRepeats: Boolean): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type)
        return handleAnnotations(this, annotation, visibleAnnotations, invisibleAnnotations, isVisible, allowRepeats)
    }

    @AsmKtDsl
    override fun defineTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        annotationType: ReferenceType,
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
    public fun toMethodNode(): MethodNode {
        val node = MethodNode(access.asInt(), name, type.descriptor, signature, null)

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
        is FieldType -> value.toAsmType()
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