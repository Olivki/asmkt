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

package net.ormr.asmkt

import net.ormr.asmkt.type.*
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@AsmKtDsl
public class MethodBodyBuilder internal constructor(public val method: MethodElementBuilder) : ElementBuilder,
    VersionedElementBuilder {
    public val code: CodeBuilder = CodeBuilder(this)

    override val version: ClassFileVersion
        get() = method.version

    public val returns: Boolean
        get() = code.returns

    public val throws: Boolean
        get() = code.throws

    /**
     * Returns a new [Label].
     *
     * @see [newBoundLabel]
     */
    public fun newLabel(): Label = code.newLabel()

    /**
     * Returns a new [Label] that has been bound to the *next* instruction.
     *
     * @see [newLabel]
     * @see [bindLabel]
     */
    public fun newBoundLabel(): Label = code.newBoundLabel()

    // -- LDC INSTRUCTIONS -- \\
    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * Unlike [ldc][CodeBuilder.ldc] this function accepts a wider variety of types for `value` and it also outputs
     * optimized bytecode where possible.
     *
     * For example, `push(3)` pushes the `ICONST_3` instruction onto the stack, and `push(true)` pushes the
     * `ICONST_1` instruction onto the stack.
     *
     * @param [value] the value to generate the instruction for, must be one of the following types; `null`, [Boolean],
     * [Char], [String], [Byte], [Short], [Int], [Long], [Float], [Double], [Type], [Handle] or [ConstantDynamic]
     *
     * @throws [IllegalArgumentException] if [value] is not one of the allowed types
     *
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
    public fun pushConstant(value: Any?) {
        when (value) {
            null -> pushNull()
            is Boolean -> pushBoolean(value)
            is Char -> pushChar(value)
            is String -> pushString(value)
            is ReturnableType -> pushType(value)
            is Handle -> pushHandle(value)
            is ConstantDynamic -> pushConstantDynamic(value)
            is Byte -> pushByte(value)
            is Short -> pushShort(value)
            is Int -> pushInt(value)
            is Long -> pushLong(value)
            is Float -> pushFloat(value)
            is Double -> pushDouble(value)
            else -> throw IllegalArgumentException("Invalid value ($value :: ${value::class})")
        }
    }

    /**
     * Pushes the `ACONST_NULL` instruction onto the stack.
     */
    @AsmKtDsl
    public fun pushNull() {
        code.aconst_null()
    }

    /**
     * Pushes the `ICONST_1` instruction onto the stack if [value] is `true`, or the `ICONST_0` instruction if `value`
     * is `false`.
     */
    @AsmKtDsl
    public fun pushBoolean(value: Boolean) {
        if (value) {
            code.iconst_1()
        } else {
            code.iconst_0()
        }
    }

    /**
     * Pushes an `LDC` instruction for the given [value] onto the stack.
     */
    @AsmKtDsl
    public fun pushString(value: String) {
        code.ldc(value)
    }

    /**
     * Pushes a `BIPUSH` instruction for the given [value] onto the stack.
     *
     * If `value` is either `-1`, `0`, `1`, `2`, `3`, `4` or `5` then a `ICONST_X` instruction will be pushed onto
     * the stack instead.
     *
     * @see [pushInt]
     */
    @AsmKtDsl
    public fun pushByte(value: Byte) {
        pushInt(value.toInt())
    }

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @see [pushInt]
     */
    @AsmKtDsl
    public fun pushChar(value: Char) {
        pushInt(value.code)
    }

    /**
     * Pushes an appropriate instruction for the given [value] onto the stack.
     *
     * @see [pushInt]
     */
    @AsmKtDsl
    public fun pushShort(value: Short) {
        pushInt(value.toInt())
    }

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
     * If none of the above requirements are satisfied then a `LDC` instruction for `value` will be pushed onto the
     * stack.
     */
    @AsmKtDsl
    public fun pushInt(value: Int) {
        withCode {
            when (value) {
                in -1..5 -> addInstruction(Opcodes.ICONST_0 + value)
                in Byte.MIN_VALUE..Byte.MAX_VALUE -> bipush(value)
                in Short.MIN_VALUE..Short.MAX_VALUE -> sipush(value)
                else -> ldc(value)
            }
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either `0L` or `1L` then a `LCONST_X` instruction will be pushed onto the
     * stack instead.
     */
    @AsmKtDsl
    public fun pushLong(value: Long) {
        withCode {
            when (value) {
                0L -> lconst_0()
                1L -> lconst_1()
                else -> ldc(value)
            }
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either *exactly* `+0.0F`, `1.0F` or `2.0F` then a `FCONST_X` instruction will be pushed onto the
     * stack instead.
     */
    @AsmKtDsl
    public fun pushFloat(value: Float) {
        withCode {
            when (value.toRawBits()) {
                0 -> fconst_0()
                else -> when (value) {
                    1.0F -> fconst_1()
                    2.0F -> fconst_2()
                    else -> ldc(value)
                }
            }
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * If `value` is either *exactly* `+0.0` or `1.0` then a `DCONST_X` instruction will be pushed onto the stack
     * instead.
     */
    @AsmKtDsl
    public fun pushDouble(value: Double) {
        withCode {
            when (value.toRawBits()) {
                0L -> dconst_0()
                else -> when (value) {
                    1.0 -> dconst_1()
                    else -> ldc(value)
                }
            }
        }
    }

    /**
     * Pushes a `LDC` instruction to retrieve the `class` instance for the given [value] onto the stack.
     *
     * If `value` is [a primitive][PrimitiveType] then depending on the [version] a different behavior will happen:
     * - If `version` >= [RELEASE_11][ClassFileVersion.RELEASE_11] then a [constant dynamic][pushConstantDynamic]
     * pointing to the `ConstantBootstraps.primitiveClass` function is pushed onto the stack.
     * - If `version` < [RELEASE_11][ClassFileVersion.RELEASE_11] then a [GETSTATIC][getStaticField] instruction
     * pointing to the `TYPE` field located in the [wrapper][PrimitiveType.box] class for `value` is pushed onto
     * the stack.
     */
    @AsmKtDsl
    public fun pushType(value: ReturnableType) {
        if (value is PrimitiveType) {
            if (version >= ClassFileVersion.RELEASE_11) {
                pushConstantDynamic(value.descriptor, ReferenceType.CLASS, primitiveClassHandle)
            } else {
                getStaticField(value.box(), "TYPE", ReferenceType.CLASS)
            }
        } else {
            code.ldc(value.asAsmType())
        }
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     */
    @AsmKtDsl
    public fun pushHandle(value: Handle) {
        code.ldc(value)
    }

    /**
     * Pushes a `LDC` instruction for the given [value] onto the stack.
     *
     * @throws [IllegalArgumentException] if [version] < [RELEASE_11][ClassFileVersion.RELEASE_11]
     */
    @AsmKtDsl
    public fun pushConstantDynamic(value: ConstantDynamic) {
        requireMinVersion(ClassFileVersion.RELEASE_11) { "Constant Dynamic" }
        code.ldc(value)
    }

    /**
     * Pushes a `LDC` instruction for a constant dynamic with the given [name], [type], [bootStrapMethod] and
     * [bootStrapMethodArguments] onto the stack.
     *
     * @throws [IllegalArgumentException] if [version] < [RELEASE_11][ClassFileVersion.RELEASE_11]
     */
    @AsmKtDsl
    public fun pushConstantDynamic(
        name: String,
        type: FieldType,
        bootStrapMethod: Handle,
        bootStrapMethodArguments: List<Any> = emptyList(),
    ) {
        requireMinVersion(ClassFileVersion.RELEASE_11) { "Constant Dynamic" }
        val arguments = bootStrapMethodArguments.replaceTypes()
        pushConstantDynamic(ConstantDynamic(name, type.descriptor, bootStrapMethod, *arguments.toTypedArray()))
    }

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun loadLocal(index: Int, type: FieldType) {
        code.addVarInstruction(type.getOpcode(Opcodes.ILOAD), index)
    }

    @AsmKtDsl
    public fun arrayLoad(type: FieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IALOAD))
    }

    // -- STORE INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun storeLocal(index: Int, type: FieldType) {
        code.addVarInstruction(type.getOpcode(Opcodes.ISTORE), index)
    }

    @AsmKtDsl
    public fun arrayStore(type: FieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IASTORE))
    }

    // -- INT INSTRUCTIONS -- \\
    /**
     * See [ret][CodeBuilder.ret].
     */
    @AsmKtDsl
    public fun ret(operand: Int) {
        code.ret(operand)
    }

    // -- ARRAY INSTRUCTIONS -- \\
    /**
     * Pushes an instruction to create a new array of type [type] onto the stack.
     *
     * @param [type] the type of the array
     */
    @AsmKtDsl
    public fun newArray(type: FieldType) {
        if (type is TypeWithInternalName) {
            code.anewarray(type.internalName)
            return
        }

        val arrayType = when (type) {
            BooleanType -> Opcodes.T_BOOLEAN
            CharType -> Opcodes.T_CHAR
            ByteType -> Opcodes.T_BYTE
            ShortType -> Opcodes.T_SHORT
            IntType -> Opcodes.T_INT
            LongType -> Opcodes.T_LONG
            FloatType -> Opcodes.T_FLOAT
            DoubleType -> Opcodes.T_DOUBLE
            // Kotlin (maybe just the IDE) knows that this is unreachable, but the compiler doesn't
            else -> throw IllegalStateException("Exhaustive when was not exhaustive for '$type'.")
        }

        code.newarray(arrayType)
    }

    @AsmKtDsl
    public fun newMultiArray(type: FieldType, dimensions: Int) {
        code.multianewarray(type.descriptor, dimensions)
    }

    @AsmKtDsl
    public fun arrayLength() {
        code.arraylength()
    }

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    /**
     * See [invokestatic][CodeBuilder.invokestatic].
     */
    @AsmKtDsl
    public fun invokeStatic(owner: ReferenceType, name: String, type: MethodType) {
        code.invokestatic(owner.internalName, name, type.descriptor)
    }

    /**
     * See [invokespecial][CodeBuilder.invokespecial].
     */
    @AsmKtDsl
    public fun invokeSpecial(owner: ReferenceType, name: String, type: MethodType) {
        code.invokespecial(owner.internalName, name, type.descriptor)
    }

    /**
     * See [invokevirtual][CodeBuilder.invokevirtual].
     */
    @AsmKtDsl
    public fun invokeVirtual(owner: ReferenceType, name: String, type: MethodType) {
        code.invokevirtual(owner.internalName, name, type.descriptor)
    }

    /**
     * See [invokeinterface][CodeBuilder.invokeinterface].
     */
    @AsmKtDsl
    public fun invokeInterface(owner: ReferenceType, name: String, type: MethodType) {
        code.invokeinterface(owner.internalName, name, type.descriptor)
    }

    /**
     * See [invokedynamic][CodeBuilder.invokedynamic].
     */
    @AsmKtDsl
    public fun invokeDynamic(name: String, type: MethodType, method: Handle, arguments: List<Any> = emptyList()) {
        requireMinVersion(ClassFileVersion.RELEASE_7) { "Invoke Dynamic" }
        code.invokedynamic(name, type.descriptor, method, arguments)
    }

    // -- FIELD INSTRUCTIONS -- \\
    /**
     * See [getstatic][CodeBuilder.getstatic].
     */
    @AsmKtDsl
    public fun getStaticField(owner: ReferenceType, name: String, type: FieldType) {
        code.getstatic(owner.internalName, name, type.descriptor)
    }

    /**
     * See [putstatic][CodeBuilder.putstatic].
     */
    @AsmKtDsl
    public fun setStaticField(owner: ReferenceType, name: String, type: FieldType) {
        code.putstatic(owner.internalName, name, type.descriptor)
    }

    /**
     * See [getfield][CodeBuilder.getfield].
     */
    @AsmKtDsl
    public fun getField(owner: ReferenceType, name: String, type: FieldType) {
        code.getfield(owner.internalName, name, type.descriptor)
    }

    /**
     * See [putfield][CodeBuilder.putfield].
     */
    @AsmKtDsl
    public fun setField(owner: ReferenceType, name: String, type: FieldType) {
        code.putfield(owner.internalName, name, type.descriptor)
    }

    // -- RETURN INSTRUCTIONS -- \\
    /**
     * Pushes the appropriate return instruction based on the [returnType][MethodElementBuilder.returnType] of [method].
     */
    @AsmKtDsl
    public fun returnValue() {
        code.addInstruction(method.returnType.getOpcode(Opcodes.IRETURN))
    }

    // -- TYPE INSTRUCTIONS -- \\
    /**
     * See [new][CodeBuilder.new].
     */
    @AsmKtDsl
    @JvmName("new_")
    public fun new(type: ReferenceType) {
        code.new(type.internalName)
    }

    /**
     * See [instanceof][CodeBuilder.instanceof].
     */
    @AsmKtDsl
    public fun instanceOf(type: ReferenceType) {
        code.instanceof(type.internalName)
    }

    /**
     * See [checkcast][CodeBuilder.checkcast].
     */
    @AsmKtDsl
    public fun checkCast(type: ReferenceType) {
        // there is no point in casting something to 'Object' as everything that isn't a primitive will be extending
        // 'Object' anyway, and we only accept 'ReferenceType' values here anyway, so 'type' can't be a primitive
        if (type != ReferenceType.OBJECT) {
            code.checkcast(type.internalName)
        }
    }

    // -- JUMP INSTRUCTIONS -- \\
    /**
     * See [goto][CodeBuilder.goto].
     */
    @AsmKtDsl
    public fun goto(label: Label) {
        code.goto(label)
    }

    /**
     * See [ifnonnull][CodeBuilder.ifnonnull].
     */
    @AsmKtDsl
    public fun ifNonNull(label: Label) {
        code.ifnonnull(label)
    }

    /**
     * See [ifnull][CodeBuilder.ifnull].
     */
    @AsmKtDsl
    public fun ifNull(label: Label) {
        code.ifnull(label)
    }

    /**
     * See [jsr][CodeBuilder.jsr].
     */
    @AsmKtDsl
    public fun jsr(label: Label) {
        code.jsr(label)
    }

    /**
     * See [ifeq][CodeBuilder.ifeq].
     */
    @AsmKtDsl
    public fun ifFalse(label: Label) {
        code.ifeq(label)
    }

    /**
     * See [ifne][CodeBuilder.ifne].
     */
    @AsmKtDsl
    public fun ifTrue(label: Label) {
        code.ifne(label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifEqual(type: FieldType, label: Label) {
        ifCmp(type, ComparisonMode.EQUAL, label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifNotEqual(type: FieldType, label: Label) {
        ifCmp(type, ComparisonMode.NOT_EQUAL, label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifGreater(type: PrimitiveFieldType, label: Label) {
        ifCmp(type, ComparisonMode.GREATER, label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifGreaterOrEqual(type: PrimitiveFieldType, label: Label) {
        ifCmp(type, ComparisonMode.GREATER_OR_EQUAL, label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifLess(type: PrimitiveFieldType, label: Label) {
        ifCmp(type, ComparisonMode.LESS, label)
    }

    /**
     * Pushes the appropriate comparison instruction based on the [type] of the values on the stack.
     */
    @AsmKtDsl
    public fun ifLessOrEqual(type: PrimitiveFieldType, label: Label) {
        ifCmp(type, ComparisonMode.LESS_OR_EQUAL, label)
    }

    private enum class ComparisonMode(val code: Int, val intCode: Int) {
        EQUAL(Opcodes.IFEQ, Opcodes.IF_ICMPEQ),
        NOT_EQUAL(Opcodes.IFNE, Opcodes.IF_ICMPNE),
        GREATER(Opcodes.IFGT, Opcodes.IF_ICMPGT),
        GREATER_OR_EQUAL(Opcodes.IFGE, Opcodes.IF_ICMPGE),
        LESS(Opcodes.IFLT, Opcodes.IF_ICMPLT),
        LESS_OR_EQUAL(Opcodes.IFLE, Opcodes.IF_ICMPLE);
    }

    private fun ifCmp(type: FieldType, mode: ComparisonMode, label: Label) {
        withCode {
            when (type) {
                LongType -> {
                    addInstruction(Opcodes.LCMP)
                    addJumpInstruction(mode.code, label)
                }
                DoubleType -> {
                    val instruction = when (mode) {
                        ComparisonMode.GREATER_OR_EQUAL, ComparisonMode.GREATER -> Opcodes.DCMPL
                        else -> Opcodes.DCMPG
                    }
                    addInstruction(instruction)
                    addJumpInstruction(mode.code, label)
                }
                FloatType -> {
                    val instruction = when (mode) {
                        ComparisonMode.GREATER_OR_EQUAL, ComparisonMode.GREATER -> Opcodes.FCMPL
                        else -> Opcodes.FCMPG
                    }
                    addInstruction(instruction)
                    addJumpInstruction(mode.code, label)
                }
                is ArrayType, is ReferenceType -> when (mode) {
                    ComparisonMode.EQUAL -> addJumpInstruction(Opcodes.IF_ACMPEQ, label)
                    ComparisonMode.NOT_EQUAL -> addJumpInstruction(Opcodes.IF_ACMPNE, label)
                    else -> throw IllegalArgumentException("Bad comparison ($mode) for type ($type)")
                }
                else -> addJumpInstruction(mode.intCode, label)
            }
        }
    }

    // -- MATH -- \\
    /**
     * Pushes the appropriate instruction for computing the addition of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun add(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IADD))
    }

    /**
     * Pushes the appropriate instruction for computing the subtraction of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun sub(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.ISUB))
    }

    /**
     * Pushes the appropriate instruction for computing the multiplication of the value currently at the top of the
     * stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun mul(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IMUL))
    }

    /**
     * Pushes the appropriate instruction for computing the division of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun div(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IDIV))
    }

    /**
     * Pushes the appropriate instruction for computing the remainder of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun rem(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IREM))
    }

    /**
     * Pushes the appropriate instruction for computing the negation of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun neg(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.INEG))
    }

    /**
     * Pushes the appropriate instruction for computing the arithmetic shift left of the value currently at the top of
     * the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun shl(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.ISHL))
    }

    /**
     * Pushes the appropriate instruction for computing the arithmetic shift right of the value currently at the top of
     * the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun shr(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.ISHR))
    }

    /**
     * Pushes the appropriate instruction for computing the unsigned shift right of the value currently at the top of
     * the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun ushr(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IUSHR))
    }

    /**
     * Pushes the appropriate instruction for computing the bitwise and of the value currently at the top of the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun and(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IAND))
    }

    /**
     * Pushes the appropriate instruction for computing the bitwise inclusive or of the value currently at the top of
     * the stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun or(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IOR))
    }

    /**
     * Pushes the appropriate instruction for computing the bitwise negation of the value currently at the top of the
     * stack.
     *
     * @param [type] the type of the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun xor(type: PrimitiveFieldType) {
        code.addInstruction(type.getOpcode(Opcodes.IXOR))
    }

    /**
     * Pushes the instructions for incrementing the local variable at the given [index] by the given [amount].
     *
     * @param [index] the index of the local variable to increment the value of
     * @param [amount] how much to increment the value of the local variable with
     *
     * @see [CodeBuilder.iinc]
     */
    @AsmKtDsl
    public fun inc(index: Int, amount: Int) {
        code.iinc(index, amount)
    }

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    /**
     * Pushes the appropriate instruction for converting the value currently at the top of the stack to the given
     * [to].
     *
     * If `from` is the same as `to` then no instruction is pushed.
     *
     * @param [from] the type of the value currently at the top of the stack
     * @param [to] the type to convert the value to
     */
    @AsmKtDsl
    public fun convert(from: PrimitiveFieldType, to: PrimitiveFieldType) {
        if (from != to) {
            withCode {
                when (from) {
                    DoubleType -> when (to) {
                        LongType -> d2l()
                        FloatType -> d2f()
                        else -> {
                            d2i()
                            body.convert(IntType, to)
                        }
                    }
                    FloatType -> when (to) {
                        LongType -> f2l()
                        DoubleType -> f2d()
                        else -> {
                            f2i()
                            body.convert(IntType, to)
                        }
                    }
                    LongType -> when (to) {
                        FloatType -> l2f()
                        DoubleType -> l2d()
                        else -> {
                            l2i()
                            body.convert(IntType, to)
                        }
                    }
                    else -> when (to) {
                        CharType -> i2c()
                        ShortType -> i2s()
                        ByteType -> i2b()
                        LongType -> i2l()
                        FloatType -> i2f()
                        DoubleType -> i2d()
                        else -> throw IllegalArgumentException("Invalid conversion from ($from) to ($to)")
                    }
                }
            }
        }
    }

    // -- TRY CATCH INSTRUCTIONS -- \\
    /**
     * Adds a try catch block starting at [start] and ending at [end] with the handler at [handler] for the given
     * [exception].
     *
     * If [exception] is `null` then the handler will catch all exceptions, this is used for `finally` blocks.
     *
     * @param [start] the start of the try catch block
     * @param [end] the end of the try catch block
     * @param [handler] the handler of the try catch block
     * @param [exception] the exception to catch, or `null` to catch all exceptions *(for `finally` blocks)*
     */
    @AsmKtDsl
    public fun tryCatch(start: Label, end: Label, handler: Label, exception: ReferenceType? = null) {
        method.addTryCatchBlock(start, end, handler, exception?.internalName)
    }

    /**
     * Pushes instructions for throwing the value currently at the top of the stack.
     */
    @AsmKtDsl
    public fun throwException() {
        code.athrow()
    }

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Binds the given [label] to the *next* instruction.
     *
     * @param [label] the label to bind
     *
     * @see [Label]
     * @see [LabelNode]
     */
    @AsmKtDsl
    public fun bindLabel(label: Label) {
        code.bindLabel(label)
    }

    /**
     * Sets the line number for the instruction at the given [start] to [line].
     *
     * By default, the line number is set to the line number of the previous instruction.
     *
     * @param [line] the line number
     * @param [start] the label of the first instruction at the line number
     *
     * @see [LineNumberNode]
     */
    @AsmKtDsl
    public fun lineNumber(line: Int, start: Label = newLabel()) {
        code.lineNumber(line, start)
    }

    // -- SWITCH INSTRUCTIONS -- \\
    /**
     * Pushes the instructions for a lookup switch.
     *
     * @param [default] the label to jump to if no match is found
     * @param [cases] the cases to match against
     *
     * @see [CodeBuilder.lookupswitch]
     */
    @AsmKtDsl
    public fun lookUpSwitch(default: Label, cases: List<SwitchCase>) {
        code.lookupswitch(
            default = default,
            keys = IntArray(cases.size) { cases[it].value },
            labels = cases.map(SwitchCase::label),
        )
    }

    /**
     * Pushes the instructions for a table switch.
     *
     * @param [default] the label to jump to if no match is found
     * @param [cases] the labels to jump to if a match is found
     * @param [min] the minimum value to match against, defaults to the minimum value of [cases] or [Int.MIN_VALUE] if
     * `cases` is empty
     * @param [max] the maximum value to match against, defaults to the maximum value of [cases] or [Int.MAX_VALUE] if
     * `cases` is empty
     *
     * @see [CodeBuilder.tableswitch]
     */
    @AsmKtDsl
    public fun tableSwitch(
        default: Label,
        cases: List<SwitchCase>,
        min: Int = cases.minOfOrNull { it.value } ?: Int.MIN_VALUE,
        max: Int = cases.maxOfOrNull { it.value } ?: Int.MAX_VALUE,
    ) {
        code.tableswitch(
            min = min,
            max = max,
            default = default,
            labels = cases.map(SwitchCase::label),
        )
    }

    // -- MONITOR INSTRUCTIONS -- \\
    /**
     * See [monitorenter][CodeBuilder.monitorenter].
     */
    @AsmKtDsl
    public fun monitorEnter() {
        code.monitorenter()
    }

    /**
     * See [monitorexit][CodeBuilder.monitorexit].
     */
    @AsmKtDsl
    public fun monitorExit() {
        code.monitorexit()
    }

    // -- STACK -- \\
    /**
     * See [dup][CodeBuilder.dup].
     */
    @AsmKtDsl
    public fun dup() {
        code.dup()
    }

    /**
     * See [dup2][CodeBuilder.dup2].
     */
    @AsmKtDsl
    public fun dup2() {
        code.dup2()
    }

    /**
     * See [dup_x1][CodeBuilder.dup_x1].
     */
    @AsmKtDsl
    public fun dupX1() {
        code.dup_x1()
    }

    /**
     * See [dup_x2][CodeBuilder.dup_x2].
     */
    @AsmKtDsl
    public fun dupX2() {
        code.dup_x2()
    }

    /**
     * See [dup2_x1][CodeBuilder.dup2_x1].
     */
    @AsmKtDsl
    public fun dup2X1() {
        code.dup2_x1()
    }

    /**
     * See [dup2_x2][CodeBuilder.dup2_x2].
     */
    @AsmKtDsl
    public fun dup2X2() {
        code.dup2_x2()
    }

    /**
     * Pushes the appropriate instructions for swapping the value currently at the top of the stack with the value
     * below it.
     *
     * @param [from] the type of the value currently at the top of the stack
     * @param [to] the type of the value below the value currently at the top of the stack
     */
    @AsmKtDsl
    public fun swap(from: ReturnableType, to: ReturnableType) {
        when (to.slotSize) {
            1 -> when (from.slotSize) {
                1 -> code.swap()
                else -> {
                    dupX2()
                    pop()
                }
            }
            else -> when (from.slotSize) {
                1 -> {
                    dup2X1()
                    pop2()
                }
                else -> {
                    dup2X2()
                    pop2()
                }
            }
        }
    }

    /**
     * See [pop][CodeBuilder.pop].
     */
    @AsmKtDsl
    public fun pop() {
        code.pop()
    }

    /**
     * See [pop2][CodeBuilder.pop2].
     */
    @AsmKtDsl
    public fun pop2() {
        code.pop2()
    }

    /**
     * See [nop][CodeBuilder.nop].
     */
    @AsmKtDsl
    public fun nop() {
        code.nop()
    }

    // -- LOCAL VARIABLES -- \\
    /**
     * Defines the name, type, signature and scope of the local variable stored at the given [index].
     *
     * @param [index] the index at which the local variable is stored at
     * @param [name] the name of the local variable
     * @param [type] the type of the local variable
     * @param [signature] the signature of the local variable, or `null` if it does not use any generic types
     * @param [start] the start of the block in which the variable belongs
     * @param [end] the end of the block in which the variable belongs
     */
    @AsmKtDsl
    public fun localVariable(
        index: Int,
        name: String,
        type: FieldType,
        signature: String? = null,
        start: Label,
        end: Label,
    ) {
        method.addLocalVariable(name, type.descriptor, signature, start, end, index)
    }

    // -- MISC -- \\
    public inline fun withCode(block: CodeBuilder.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        block(code)
    }

    // -- HELPERS -- \\

    /*private fun MutableMap<Int, MutableList<BytecodeAnnotation>>.add(
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
    }*/

    private fun fixAnnotationValue(value: Any?): Any? = when (value) {
        null -> null
        is ReturnableType -> value.asAsmType()
        //is BytecodeAnnotation -> value.node
        is List<*> -> value.mapTo(mutableListOf(), this::fixAnnotationValue)
        is Array<*> -> TODO("or an two elements String array (for enumeration values)")
        else -> value
    }

    private companion object {
        private val primitiveClassHandle: Handle = InvokeStaticHandle(
            owner = ReferenceType("java/lang/invoke/ConstantBootstraps"),
            name = "primitiveClass",
            type = MethodType(
                ReferenceType.CLASS,
                ReferenceType.METHOD_HANDLES_LOOKUP,
                ReferenceType.STRING,
                ReferenceType.CLASS,
            ),
        )
    }
}