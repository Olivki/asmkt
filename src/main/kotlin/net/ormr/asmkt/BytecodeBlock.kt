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

@file:Suppress("FunctionName", "unused", "SpellCheckingInspection")

package net.ormr.asmkt

import krautils.collections.mapToTypedArray
import net.ormr.asmkt.types.FieldType
import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.Type
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Provides "low level" access bytecode instructions.
 *
 * @see [BytecodeMethod]
 */
@AsmKtDsl
public class BytecodeBlock : Iterable<AbstractInsnNode> {
    public val instructions: InsnList = InsnList()

    public var returns: Boolean = false
        private set

    /**
     * Returns `true` if no instructions have been added to `this` block, otherwise `false`.
     */
    public fun isEmpty(): Boolean = instructions.size() == 0

    /**
     * Returns `true` if any instructions have been added to `this` block, otherwise `false`.
     */
    public fun isNotEmpty(): Boolean = !(isEmpty())

    // see https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html for documentation on all the different instructions

    @AsmKtDsl
    public fun ldc(value: Any): BytecodeBlock = addLdcInstruction(if (value is Type) value.toAsmType() else value)

    @AsmKtDsl
    public fun aconst_null(): BytecodeBlock = addInstruction(Opcodes.ACONST_NULL)

    @AsmKtDsl
    public fun iconst_m1(): BytecodeBlock = addInstruction(Opcodes.ICONST_M1)

    @AsmKtDsl
    public fun iconst_0(): BytecodeBlock = addInstruction(Opcodes.ICONST_0)

    @AsmKtDsl
    public fun iconst_1(): BytecodeBlock = addInstruction(Opcodes.ICONST_1)

    @AsmKtDsl
    public fun iconst_2(): BytecodeBlock = addInstruction(Opcodes.ICONST_2)

    @AsmKtDsl
    public fun iconst_3(): BytecodeBlock = addInstruction(Opcodes.ICONST_3)

    @AsmKtDsl
    public fun iconst_4(): BytecodeBlock = addInstruction(Opcodes.ICONST_4)

    @AsmKtDsl
    public fun iconst_5(): BytecodeBlock = addInstruction(Opcodes.ICONST_5)

    @AsmKtDsl
    public fun lconst_0(): BytecodeBlock = addInstruction(Opcodes.LCONST_0)

    @AsmKtDsl
    public fun lconst_1(): BytecodeBlock = addInstruction(Opcodes.LCONST_1)

    @AsmKtDsl
    public fun fconst_0(): BytecodeBlock = addInstruction(Opcodes.FCONST_0)

    @AsmKtDsl
    public fun fconst_1(): BytecodeBlock = addInstruction(Opcodes.FCONST_1)

    @AsmKtDsl
    public fun fconst_2(): BytecodeBlock = addInstruction(Opcodes.FCONST_2)

    @AsmKtDsl
    public fun dconst_0(): BytecodeBlock = addInstruction(Opcodes.DCONST_0)

    @AsmKtDsl
    public fun dconst_1(): BytecodeBlock = addInstruction(Opcodes.DCONST_1)

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun aload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ALOAD, index)

    @AsmKtDsl
    public fun iload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ILOAD, index)

    @AsmKtDsl
    public fun lload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LLOAD, index)

    @AsmKtDsl
    public fun fload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FLOAD, index)

    @AsmKtDsl
    public fun dload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DLOAD, index)

    // -- STORE INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun astore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ASTORE, index)

    @AsmKtDsl
    public fun istore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ISTORE, index)

    @AsmKtDsl
    public fun lstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LSTORE, index)

    @AsmKtDsl
    public fun fstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FSTORE, index)

    @AsmKtDsl
    public fun dstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DSTORE, index)

    @AsmKtDsl
    public fun aaload(): BytecodeBlock = addInstruction(Opcodes.AALOAD)

    @AsmKtDsl
    public fun aastore(): BytecodeBlock = addInstruction(Opcodes.AASTORE)

    @AsmKtDsl
    public fun iaload(): BytecodeBlock = addInstruction(Opcodes.IALOAD)

    @AsmKtDsl
    public fun iastore(): BytecodeBlock = addInstruction(Opcodes.IASTORE)

    @AsmKtDsl
    public fun laload(): BytecodeBlock = addInstruction(Opcodes.LALOAD)

    @AsmKtDsl
    public fun lastore(): BytecodeBlock = addInstruction(Opcodes.LASTORE)

    @AsmKtDsl
    public fun baload(): BytecodeBlock = addInstruction(Opcodes.BALOAD)

    @AsmKtDsl
    public fun bastore(): BytecodeBlock = addInstruction(Opcodes.BASTORE)

    @AsmKtDsl
    public fun saload(): BytecodeBlock = addInstruction(Opcodes.SALOAD)

    @AsmKtDsl
    public fun sastore(): BytecodeBlock = addInstruction(Opcodes.SASTORE)

    @AsmKtDsl
    public fun caload(): BytecodeBlock = addInstruction(Opcodes.CALOAD)

    @AsmKtDsl
    public fun castore(): BytecodeBlock = addInstruction(Opcodes.CASTORE)

    @AsmKtDsl
    public fun faload(): BytecodeBlock = addInstruction(Opcodes.FALOAD)

    @AsmKtDsl
    public fun fastore(): BytecodeBlock = addInstruction(Opcodes.FASTORE)

    @AsmKtDsl
    public fun daload(): BytecodeBlock = addInstruction(Opcodes.DALOAD)

    @AsmKtDsl
    public fun dastore(): BytecodeBlock = addInstruction(Opcodes.DASTORE)

    // -- INT INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun bipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.BIPUSH, operand)

    @AsmKtDsl
    public fun sipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.SIPUSH, operand)

    @AsmKtDsl
    public fun ret(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.RET, operand)

    // -- ARRAY INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun anewarray(descriptor: String): BytecodeBlock = addTypeInstruction(Opcodes.ANEWARRAY, descriptor)

    @AsmKtDsl
    public fun newarray(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.NEWARRAY, operand)

    @AsmKtDsl
    public fun multianewarray(descriptor: String, numDimensions: Int): BytecodeBlock =
        addMultiANewArrayInstruction(descriptor, numDimensions)

    @AsmKtDsl
    public fun arraylength(): BytecodeBlock = addInstruction(Opcodes.ARRAYLENGTH)

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun invokestatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESTATIC, owner, name, descriptor, false)

    @AsmKtDsl
    public fun invokespecial(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESPECIAL, owner, name, descriptor, false)

    @AsmKtDsl
    public fun invokevirtual(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false)

    @AsmKtDsl
    public fun invokeinterface(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true)

    @AsmKtDsl
    public fun invokedynamic(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        bootstrapMethodArguments: Array<out Any>,
    ): BytecodeBlock = apply {
        instructions.add(InvokeDynamicInsnNode(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments))
    }

    // -- RETURN INSTRUCTIONS -- \\
    /**
     * Generates the appropriate return instruction based on the given primitive [type].
     *
     * @param [type] the primitive type to generate the instruction for
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun areturn(type: FieldType): BytecodeBlock = apply {
        addInstruction(type.getOpcode(Opcodes.IRETURN))
    }

    @AsmKtDsl
    public fun voidreturn(): BytecodeBlock = addInstruction(Opcodes.RETURN)

    @AsmKtDsl
    public fun areturn(): BytecodeBlock = addInstruction(Opcodes.ARETURN)

    @AsmKtDsl
    public fun ireturn(): BytecodeBlock = addInstruction(Opcodes.IRETURN)

    @AsmKtDsl
    public fun freturn(): BytecodeBlock = addInstruction(Opcodes.FRETURN)

    @AsmKtDsl
    public fun lreturn(): BytecodeBlock = addInstruction(Opcodes.LRETURN)

    @AsmKtDsl
    public fun dreturn(): BytecodeBlock = addInstruction(Opcodes.DRETURN)

    // -- FIELD INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun getstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETSTATIC, owner, name, descriptor)

    @AsmKtDsl
    public fun putstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTSTATIC, owner, name, descriptor)

    @AsmKtDsl
    public fun getfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETFIELD, owner, name, descriptor)

    @AsmKtDsl
    public fun putfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTFIELD, owner, name, descriptor)

    // -- TYPE INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("anew")
    public fun new(type: String): BytecodeBlock = addTypeInstruction(Opcodes.NEW, type)

    @AsmKtDsl
    @JvmName("instance_of")
    public fun instanceof(type: String): BytecodeBlock = addTypeInstruction(Opcodes.INSTANCEOF, type)

    @AsmKtDsl
    public fun checkcast(type: String): BytecodeBlock = addTypeInstruction(Opcodes.CHECKCAST, type)

    // -- JUMP INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("go_to")
    public fun goto(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.GOTO, label)

    @AsmKtDsl
    public fun ifeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFEQ, label)

    @AsmKtDsl
    public fun ifne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNE, label)

    @AsmKtDsl
    public fun if_acmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPNE, label)

    @AsmKtDsl
    public fun if_acmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPEQ, label)

    @AsmKtDsl
    public fun if_icmple(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLE, label)

    @AsmKtDsl
    public fun if_icmpgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGT, label)

    @AsmKtDsl
    public fun if_icmplt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLT, label)

    @AsmKtDsl
    public fun if_icmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPNE, label)

    @AsmKtDsl
    public fun if_icmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPEQ, label)

    @AsmKtDsl
    public fun if_icmpge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGE, label)

    @AsmKtDsl
    public fun ifnonnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNONNULL, label)

    @AsmKtDsl
    public fun ifnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNULL, label)

    @AsmKtDsl
    public fun iflt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLT, label)

    @AsmKtDsl
    public fun ifle(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLE, label)

    @AsmKtDsl
    public fun ifgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGT, label)

    @AsmKtDsl
    public fun ifge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGE, label)

    @AsmKtDsl
    public fun jsr(branch: Label): BytecodeBlock = addJumpInstruction(Opcodes.JSR, branch)

    // -- PRIMITIVE NUMBER OPERATOR INSTRUCTIONS -- \\
    // int
    @AsmKtDsl
    public fun ishr(): BytecodeBlock = addInstruction(Opcodes.ISHR)

    @AsmKtDsl
    public fun ishl(): BytecodeBlock = addInstruction(Opcodes.ISHL)

    @AsmKtDsl
    public fun iushr(): BytecodeBlock = addInstruction(Opcodes.IUSHR)

    @AsmKtDsl
    public fun iand(): BytecodeBlock = addInstruction(Opcodes.IAND)

    @AsmKtDsl
    public fun ior(): BytecodeBlock = addInstruction(Opcodes.IOR)

    @AsmKtDsl
    public fun ixor(): BytecodeBlock = addInstruction(Opcodes.IXOR)

    @AsmKtDsl
    public fun iadd(): BytecodeBlock = addInstruction(Opcodes.IADD)

    @AsmKtDsl
    public fun isub(): BytecodeBlock = addInstruction(Opcodes.ISUB)

    @AsmKtDsl
    public fun idiv(): BytecodeBlock = addInstruction(Opcodes.IDIV)

    @AsmKtDsl
    public fun imul(): BytecodeBlock = addInstruction(Opcodes.IMUL)

    @AsmKtDsl
    public fun irem(): BytecodeBlock = addInstruction(Opcodes.IREM)

    @AsmKtDsl
    public fun ineg(): BytecodeBlock = addInstruction(Opcodes.INEG)

    @AsmKtDsl
    public fun iinc(index: Int, increment: Int): BytecodeBlock = addIincInstruction(index, increment)

    // long
    @AsmKtDsl
    public fun lshr(): BytecodeBlock = addInstruction(Opcodes.LSHR)

    @AsmKtDsl
    public fun lshl(): BytecodeBlock = addInstruction(Opcodes.LSHL)

    @AsmKtDsl
    public fun lushr(): BytecodeBlock = addInstruction(Opcodes.LUSHR)

    @AsmKtDsl
    public fun lcmp(): BytecodeBlock = addInstruction(Opcodes.LCMP)

    @AsmKtDsl
    public fun land(): BytecodeBlock = addInstruction(Opcodes.LAND)

    @AsmKtDsl
    public fun lor(): BytecodeBlock = addInstruction(Opcodes.LOR)

    @AsmKtDsl
    public fun lxor(): BytecodeBlock = addInstruction(Opcodes.LXOR)

    @AsmKtDsl
    public fun ladd(): BytecodeBlock = addInstruction(Opcodes.LADD)

    @AsmKtDsl
    public fun lsub(): BytecodeBlock = addInstruction(Opcodes.LSUB)

    @AsmKtDsl
    public fun ldiv(): BytecodeBlock = addInstruction(Opcodes.LDIV)

    @AsmKtDsl
    public fun lmul(): BytecodeBlock = addInstruction(Opcodes.LMUL)

    @AsmKtDsl
    public fun lrem(): BytecodeBlock = addInstruction(Opcodes.LREM)

    @AsmKtDsl
    public fun lneg(): BytecodeBlock = addInstruction(Opcodes.LNEG)

    // float
    @AsmKtDsl
    public fun fadd(): BytecodeBlock = addInstruction(Opcodes.FADD)

    @AsmKtDsl
    public fun fsub(): BytecodeBlock = addInstruction(Opcodes.FSUB)

    @AsmKtDsl
    public fun fdiv(): BytecodeBlock = addInstruction(Opcodes.FDIV)

    @AsmKtDsl
    public fun fmul(): BytecodeBlock = addInstruction(Opcodes.FMUL)

    @AsmKtDsl
    public fun frem(): BytecodeBlock = addInstruction(Opcodes.FREM)

    @AsmKtDsl
    public fun fneg(): BytecodeBlock = addInstruction(Opcodes.FNEG)

    @AsmKtDsl
    public fun fcmpl(): BytecodeBlock = addInstruction(Opcodes.FCMPL)

    @AsmKtDsl
    public fun fcmpg(): BytecodeBlock = addInstruction(Opcodes.FCMPG)

    // double
    @AsmKtDsl
    public fun ddiv(): BytecodeBlock = addInstruction(Opcodes.DDIV)

    @AsmKtDsl
    public fun dmul(): BytecodeBlock = addInstruction(Opcodes.DMUL)

    @AsmKtDsl
    public fun drem(): BytecodeBlock = addInstruction(Opcodes.DREM)

    @AsmKtDsl
    public fun dneg(): BytecodeBlock = addInstruction(Opcodes.DNEG)

    @AsmKtDsl
    public fun dadd(): BytecodeBlock = addInstruction(Opcodes.DADD)

    @AsmKtDsl
    public fun dsub(): BytecodeBlock = addInstruction(Opcodes.DSUB)

    @AsmKtDsl
    public fun dcmpl(): BytecodeBlock = addInstruction(Opcodes.DCMPL)

    @AsmKtDsl
    public fun dcmpg(): BytecodeBlock = addInstruction(Opcodes.DCMPG)

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    // int to ...
    @AsmKtDsl
    public fun i2d(): BytecodeBlock = addInstruction(Opcodes.I2D)

    @AsmKtDsl
    public fun i2l(): BytecodeBlock = addInstruction(Opcodes.I2L)

    @AsmKtDsl
    public fun i2f(): BytecodeBlock = addInstruction(Opcodes.I2F)

    @AsmKtDsl
    public fun i2s(): BytecodeBlock = addInstruction(Opcodes.I2S)

    @AsmKtDsl
    public fun i2c(): BytecodeBlock = addInstruction(Opcodes.I2C)

    @AsmKtDsl
    public fun i2b(): BytecodeBlock = addInstruction(Opcodes.I2B)

    // long to ...
    @AsmKtDsl
    public fun l2d(): BytecodeBlock = addInstruction(Opcodes.L2D)

    @AsmKtDsl
    public fun l2i(): BytecodeBlock = addInstruction(Opcodes.L2I)

    @AsmKtDsl
    public fun l2f(): BytecodeBlock = addInstruction(Opcodes.L2F)

    // float to ...
    @AsmKtDsl
    public fun f2d(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKtDsl
    public fun f2i(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKtDsl
    public fun f2l(): BytecodeBlock = addInstruction(Opcodes.F2L)

    // double to ...
    @AsmKtDsl
    public fun d2f(): BytecodeBlock = addInstruction(Opcodes.D2F)

    @AsmKtDsl
    public fun d2i(): BytecodeBlock = addInstruction(Opcodes.D2I)

    @AsmKtDsl
    public fun d2l(): BytecodeBlock = addInstruction(Opcodes.D2L)

    // -- TRY CATCH INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun athrow(): BytecodeBlock = addInstruction(Opcodes.ATHROW)

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Creates an instruction to mark the current point with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun mark(label: Label): BytecodeBlock = addLabel(label)

    /**
     * Marks the given [line] with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    public fun line(line: Int, label: Label): BytecodeBlock = addLineNumber(line, label)

    // -- SWITCH INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun lookupswitch(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>,
    ): BytecodeBlock = addLookupSwitchInstruction(defaultLabel, keys, labels)

    @AsmKtDsl
    public fun tableswitch(min: Int, max: Int, defaultLabel: Label, cases: Array<out Label>): BytecodeBlock =
        addTableSwitchInstruction(min, max, defaultLabel, cases)

    // -- MONITOR INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun monitorenter(): BytecodeBlock = addInstruction(Opcodes.MONITORENTER)

    @AsmKtDsl
    public fun monitorexit(): BytecodeBlock = addInstruction(Opcodes.MONITOREXIT)

    // -- DUP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun dup(): BytecodeBlock = addInstruction(Opcodes.DUP)

    @AsmKtDsl
    public fun dup_x2(): BytecodeBlock = addInstruction(Opcodes.DUP_X2)

    @AsmKtDsl
    public fun dup_x1(): BytecodeBlock = addInstruction(Opcodes.DUP_X1)

    @AsmKtDsl
    public fun dup2_x2(): BytecodeBlock = addInstruction(Opcodes.DUP2_X2)

    @AsmKtDsl
    public fun dup2_x1(): BytecodeBlock = addInstruction(Opcodes.DUP2_X1)

    @AsmKtDsl
    public fun dup2(): BytecodeBlock = addInstruction(Opcodes.DUP2)

    // -- SWAP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun swap(): BytecodeBlock = addInstruction(Opcodes.SWAP)

    // -- NOP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun nop(): BytecodeBlock = addInstruction(Opcodes.NOP)

    // -- POP INSTRUCTIONS -- \\
    @AsmKtDsl
    public fun pop(): BytecodeBlock = addInstruction(Opcodes.POP)

    @AsmKtDsl
    public fun pop2(): BytecodeBlock = addInstruction(Opcodes.POP2)

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
    ): BytecodeBlock = apply {
        instructions.add(FrameNode(type, numLocal, local.replaceLabels(), numStack, stack.replaceLabels()))
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKtDsl
    public fun frame(type: Int, local: Array<out Any>, stack: Array<out Any>): BytecodeBlock =
        frame(type, local.size, local, stack.size, stack)

    // -- INSTRUCTION FUNCTIONS -- \\
    public fun addInstruction(opcode: Int): BytecodeBlock = apply {
        when (opcode) {
            Opcodes.RETURN, Opcodes.ARETURN, Opcodes.IRETURN, Opcodes.FRETURN, Opcodes.LRETURN, Opcodes.DRETURN -> returns =
                true
        }

        instructions.add(InsnNode(opcode))
    }

    public fun addVarInstruction(opcode: Int, index: Int): BytecodeBlock = apply {
        instructions.add(VarInsnNode(opcode, index))
    }

    public fun addIntInstruction(opcode: Int, operand: Int): BytecodeBlock = apply {
        instructions.add(IntInsnNode(opcode, operand))
    }

    public fun addTypeInstruction(opcode: Int, descriptor: String): BytecodeBlock = apply {
        instructions.add(TypeInsnNode(opcode, descriptor))
    }

    public fun addFieldInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
    ): BytecodeBlock = apply {
        instructions.add(FieldInsnNode(opcode, owner, name, descriptor))
    }

    public fun addMethodInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean,
    ): BytecodeBlock = apply {
        instructions.add(MethodInsnNode(opcode, owner, name, descriptor, isInterface))
    }

    public fun addJumpInstruction(opcode: Int, label: Label): BytecodeBlock = apply {
        instructions.add(JumpInsnNode(opcode, label.toNode()))
    }

    public fun addLabel(label: Label): BytecodeBlock = apply {
        instructions.add(LabelNode(label))
    }

    private fun addLdcInstruction(value: Any): BytecodeBlock = apply {
        instructions.add(LdcInsnNode(value))
    }

    public fun addIincInstruction(index: Int, increment: Int): BytecodeBlock = apply {
        instructions.add(IincInsnNode(index, increment))
    }

    public fun addTableSwitchInstruction(
        min: Int,
        max: Int,
        defaultLabel: Label,
        labels: Array<out Label>,
    ): BytecodeBlock = apply {
        instructions.add(TableSwitchInsnNode(min, max, defaultLabel.toNode(), *labels.toNodeArray()))
    }

    public fun addLookupSwitchInstruction(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>,
    ): BytecodeBlock = apply {
        instructions.add(LookupSwitchInsnNode(defaultLabel.toNode(), keys, labels.toNodeArray()))
    }

    public fun addMultiANewArrayInstruction(descriptor: String, numDimensions: Int): BytecodeBlock = apply {
        instructions.add(MultiANewArrayInsnNode(descriptor, numDimensions))
    }

    public fun addLineNumber(line: Int, start: Label): BytecodeBlock = apply {
        instructions.add(LineNumberNode(line, start.toNode()))
    }

    public fun toBytecodeMethod(
        name: String,
        access: Modifier,
        type: MethodType,
        signature: String?,
        exceptions: List<ReferenceType>,
        parent: BytecodeClass,
    ): BytecodeMethod = BytecodeMethod(name, access, type, signature, exceptions, parent, this)

    // -- ADDS -- \\
    public fun prepend(instructions: InsnList): BytecodeBlock = apply {
        this.instructions.insert(instructions)
    }

    public fun prepend(other: BytecodeBlock): BytecodeBlock = apply {
        this.instructions.insert(other.instructions)
    }

    public fun append(instructions: InsnList): BytecodeBlock = apply {
        this.instructions.add(instructions)
    }

    public fun append(other: BytecodeBlock): BytecodeBlock = apply {
        this.instructions.add(other.instructions)
    }

    override fun iterator(): Iterator<AbstractInsnNode> = instructions.iterator()

    // -- UTILS -- \\
    /**
     * Returns a copy of `this` array where all the [Label] instances have been wrapped in [LabelNode]s.
     */
    private fun Array<out Label>.toNodeArray(): Array<out LabelNode> = mapToTypedArray { it.toNode() }

    /**
     * Returns a copy of `this` array where all instances of [Label] have been replaced with [LabelNode]s.
     */
    private fun Array<out Any>.replaceLabels(): Array<out Any> =
        mapToTypedArray { if (it is Label) it.toNode() else it }

    /**
     * Returns `this` [Label] wrapped in a [LabelNode].
     */
    private fun Label.toNode(): LabelNode = LabelNode(this)
}