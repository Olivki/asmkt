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

@file:Suppress("FunctionName")

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
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Provides "low level" access bytecode instructions.
 *
 * @see [BytecodeMethod]
 */
class BytecodeBlock : Iterable<AbstractInsnNode> {
    val instructions = InsnList()

    @get:JvmName("returns")
    var returns: Boolean = false
        private set

    /**
     * Returns `true` if no instructions have been added to `this` block, otherwise `false`.
     */
    fun isEmpty(): Boolean = instructions.size() == 0

    /**
     * Returns `true` if any instructions have been added to `this` block, otherwise `false`.
     */
    fun isNotEmpty(): Boolean = !(isEmpty())

    // see https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html for documentation on all the different instructions

    @AsmKtDsl
    fun ldc(value: Any): BytecodeBlock = addLdcInstruction(if (value is Type) value.toAsmType() else value)

    @AsmKtDsl
    fun aconst_null(): BytecodeBlock = addInstruction(Opcodes.ACONST_NULL)

    @AsmKtDsl
    fun iconst_m1(): BytecodeBlock = addInstruction(Opcodes.ICONST_M1)

    @AsmKtDsl
    fun iconst_0(): BytecodeBlock = addInstruction(Opcodes.ICONST_0)

    @AsmKtDsl
    fun iconst_1(): BytecodeBlock = addInstruction(Opcodes.ICONST_1)

    @AsmKtDsl
    fun iconst_2(): BytecodeBlock = addInstruction(Opcodes.ICONST_2)

    @AsmKtDsl
    fun iconst_3(): BytecodeBlock = addInstruction(Opcodes.ICONST_3)

    @AsmKtDsl
    fun iconst_4(): BytecodeBlock = addInstruction(Opcodes.ICONST_4)

    @AsmKtDsl
    fun iconst_5(): BytecodeBlock = addInstruction(Opcodes.ICONST_5)

    @AsmKtDsl
    fun lconst_0(): BytecodeBlock = addInstruction(Opcodes.LCONST_0)

    @AsmKtDsl
    fun lconst_1(): BytecodeBlock = addInstruction(Opcodes.LCONST_1)

    @AsmKtDsl
    fun fconst_0(): BytecodeBlock = addInstruction(Opcodes.FCONST_0)

    @AsmKtDsl
    fun fconst_1(): BytecodeBlock = addInstruction(Opcodes.FCONST_1)

    @AsmKtDsl
    fun fconst_2(): BytecodeBlock = addInstruction(Opcodes.FCONST_2)

    @AsmKtDsl
    fun dconst_0(): BytecodeBlock = addInstruction(Opcodes.DCONST_0)

    @AsmKtDsl
    fun dconst_1(): BytecodeBlock = addInstruction(Opcodes.DCONST_1)

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKtDsl
    fun aload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ALOAD, index)

    @AsmKtDsl
    fun iload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ILOAD, index)

    @AsmKtDsl
    fun lload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LLOAD, index)

    @AsmKtDsl
    fun fload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FLOAD, index)

    @AsmKtDsl
    fun dload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DLOAD, index)

    // -- STORE INSTRUCTIONS -- \\
    @AsmKtDsl
    fun astore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ASTORE, index)

    @AsmKtDsl
    fun istore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ISTORE, index)

    @AsmKtDsl
    fun lstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LSTORE, index)

    @AsmKtDsl
    fun fstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FSTORE, index)

    @AsmKtDsl
    fun dstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DSTORE, index)

    @AsmKtDsl
    fun aaload(): BytecodeBlock = addInstruction(Opcodes.AALOAD)

    @AsmKtDsl
    fun aastore(): BytecodeBlock = addInstruction(Opcodes.AASTORE)

    @AsmKtDsl
    fun iaload(): BytecodeBlock = addInstruction(Opcodes.IALOAD)

    @AsmKtDsl
    fun iastore(): BytecodeBlock = addInstruction(Opcodes.IASTORE)

    @AsmKtDsl
    fun laload(): BytecodeBlock = addInstruction(Opcodes.LALOAD)

    @AsmKtDsl
    fun lastore(): BytecodeBlock = addInstruction(Opcodes.LASTORE)

    @AsmKtDsl
    fun baload(): BytecodeBlock = addInstruction(Opcodes.BALOAD)

    @AsmKtDsl
    fun bastore(): BytecodeBlock = addInstruction(Opcodes.BASTORE)

    @AsmKtDsl
    fun saload(): BytecodeBlock = addInstruction(Opcodes.SALOAD)

    @AsmKtDsl
    fun sastore(): BytecodeBlock = addInstruction(Opcodes.SASTORE)

    @AsmKtDsl
    fun caload(): BytecodeBlock = addInstruction(Opcodes.CALOAD)

    @AsmKtDsl
    fun castore(): BytecodeBlock = addInstruction(Opcodes.CASTORE)

    @AsmKtDsl
    fun faload(): BytecodeBlock = addInstruction(Opcodes.FALOAD)

    @AsmKtDsl
    fun fastore(): BytecodeBlock = addInstruction(Opcodes.FASTORE)

    @AsmKtDsl
    fun daload(): BytecodeBlock = addInstruction(Opcodes.DALOAD)

    @AsmKtDsl
    fun dastore(): BytecodeBlock = addInstruction(Opcodes.DASTORE)

    // -- INT INSTRUCTIONS -- \\
    @AsmKtDsl
    fun bipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.BIPUSH, operand)

    @AsmKtDsl
    fun sipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.SIPUSH, operand)

    @AsmKtDsl
    fun ret(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.RET, operand)

    // -- ARRAY INSTRUCTIONS -- \\
    @AsmKtDsl
    fun anewarray(descriptor: String): BytecodeBlock = addTypeInstruction(Opcodes.ANEWARRAY, descriptor)

    @AsmKtDsl
    fun newarray(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.NEWARRAY, operand)

    @AsmKtDsl
    fun multianewarray(descriptor: String, numDimensions: Int): BytecodeBlock =
        addMultiANewArrayInstruction(descriptor, numDimensions)

    @AsmKtDsl
    fun arraylength(): BytecodeBlock = addInstruction(Opcodes.ARRAYLENGTH)

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    @AsmKtDsl
    fun invokestatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESTATIC, owner, name, descriptor, false)

    @AsmKtDsl
    fun invokespecial(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESPECIAL, owner, name, descriptor, false)

    @AsmKtDsl
    fun invokevirtual(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false)

    @AsmKtDsl
    fun invokeinterface(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true)

    @AsmKtDsl
    fun invokedynamic(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        bootstrapMethodArguments: Array<out Any>
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
    fun areturn(type: FieldType): BytecodeBlock = apply {
        addInstruction(type.getOpcode(Opcodes.IRETURN))
    }

    @AsmKtDsl
    fun voidreturn(): BytecodeBlock = addInstruction(Opcodes.RETURN)

    @AsmKtDsl
    fun areturn(): BytecodeBlock = addInstruction(Opcodes.ARETURN)

    @AsmKtDsl
    fun ireturn(): BytecodeBlock = addInstruction(Opcodes.IRETURN)

    @AsmKtDsl
    fun freturn(): BytecodeBlock = addInstruction(Opcodes.FRETURN)

    @AsmKtDsl
    fun lreturn(): BytecodeBlock = addInstruction(Opcodes.LRETURN)

    @AsmKtDsl
    fun dreturn(): BytecodeBlock = addInstruction(Opcodes.DRETURN)

    // -- FIELD INSTRUCTIONS -- \\
    @AsmKtDsl
    fun getstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETSTATIC, owner, name, descriptor)

    @AsmKtDsl
    fun putstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTSTATIC, owner, name, descriptor)

    @AsmKtDsl
    fun getfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETFIELD, owner, name, descriptor)

    @AsmKtDsl
    fun putfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTFIELD, owner, name, descriptor)

    // -- TYPE INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("anew")
    fun new(type: String): BytecodeBlock = addTypeInstruction(Opcodes.NEW, type)

    @AsmKtDsl
    @JvmName("instance_of")
    fun instanceof(type: String): BytecodeBlock = addTypeInstruction(Opcodes.INSTANCEOF, type)

    @AsmKtDsl
    fun checkcast(type: String): BytecodeBlock = addTypeInstruction(Opcodes.CHECKCAST, type)

    // -- JUMP INSTRUCTIONS -- \\
    @AsmKtDsl
    @JvmName("go_to")
    fun goto(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.GOTO, label)

    @AsmKtDsl
    fun ifeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFEQ, label)

    @AsmKtDsl
    fun ifne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNE, label)

    @AsmKtDsl
    fun if_acmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPNE, label)

    @AsmKtDsl
    fun if_acmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPEQ, label)

    @AsmKtDsl
    fun if_icmple(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLE, label)

    @AsmKtDsl
    fun if_icmpgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGT, label)

    @AsmKtDsl
    fun if_icmplt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLT, label)

    @AsmKtDsl
    fun if_icmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPNE, label)

    @AsmKtDsl
    fun if_icmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPEQ, label)

    @AsmKtDsl
    fun if_icmpge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGE, label)

    @AsmKtDsl
    fun ifnonnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNONNULL, label)

    @AsmKtDsl
    fun ifnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNULL, label)

    @AsmKtDsl
    fun iflt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLT, label)

    @AsmKtDsl
    fun ifle(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLE, label)

    @AsmKtDsl
    fun ifgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGT, label)

    @AsmKtDsl
    fun ifge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGE, label)

    @AsmKtDsl
    fun jsr(branch: Label): BytecodeBlock = addJumpInstruction(Opcodes.JSR, branch)

    // -- PRIMITIVE NUMBER OPERATOR INSTRUCTIONS -- \\
    // int
    @AsmKtDsl
    fun ishr(): BytecodeBlock = addInstruction(Opcodes.ISHR)

    @AsmKtDsl
    fun ishl(): BytecodeBlock = addInstruction(Opcodes.ISHL)

    @AsmKtDsl
    fun iushr(): BytecodeBlock = addInstruction(Opcodes.IUSHR)

    @AsmKtDsl
    fun iand(): BytecodeBlock = addInstruction(Opcodes.IAND)

    @AsmKtDsl
    fun ior(): BytecodeBlock = addInstruction(Opcodes.IOR)

    @AsmKtDsl
    fun ixor(): BytecodeBlock = addInstruction(Opcodes.IXOR)

    @AsmKtDsl
    fun iadd(): BytecodeBlock = addInstruction(Opcodes.IADD)

    @AsmKtDsl
    fun isub(): BytecodeBlock = addInstruction(Opcodes.ISUB)

    @AsmKtDsl
    fun idiv(): BytecodeBlock = addInstruction(Opcodes.IDIV)

    @AsmKtDsl
    fun imul(): BytecodeBlock = addInstruction(Opcodes.IMUL)

    @AsmKtDsl
    fun irem(): BytecodeBlock = addInstruction(Opcodes.IREM)

    @AsmKtDsl
    fun ineg(): BytecodeBlock = addInstruction(Opcodes.INEG)

    @AsmKtDsl
    fun iinc(index: Int, increment: Int): BytecodeBlock = addIincInstruction(index, increment)

    // long
    @AsmKtDsl
    fun lshr(): BytecodeBlock = addInstruction(Opcodes.LSHR)

    @AsmKtDsl
    fun lshl(): BytecodeBlock = addInstruction(Opcodes.LSHL)

    @AsmKtDsl
    fun lushr(): BytecodeBlock = addInstruction(Opcodes.LUSHR)

    @AsmKtDsl
    fun lcmp(): BytecodeBlock = addInstruction(Opcodes.LCMP)

    @AsmKtDsl
    fun land(): BytecodeBlock = addInstruction(Opcodes.LAND)

    @AsmKtDsl
    fun lor(): BytecodeBlock = addInstruction(Opcodes.LOR)

    @AsmKtDsl
    fun lxor(): BytecodeBlock = addInstruction(Opcodes.LXOR)

    @AsmKtDsl
    fun ladd(): BytecodeBlock = addInstruction(Opcodes.LADD)

    @AsmKtDsl
    fun lsub(): BytecodeBlock = addInstruction(Opcodes.LSUB)

    @AsmKtDsl
    fun ldiv(): BytecodeBlock = addInstruction(Opcodes.LDIV)

    @AsmKtDsl
    fun lmul(): BytecodeBlock = addInstruction(Opcodes.LMUL)

    @AsmKtDsl
    fun lrem(): BytecodeBlock = addInstruction(Opcodes.LREM)

    @AsmKtDsl
    fun lneg(): BytecodeBlock = addInstruction(Opcodes.LNEG)

    // float
    @AsmKtDsl
    fun fadd(): BytecodeBlock = addInstruction(Opcodes.FADD)

    @AsmKtDsl
    fun fsub(): BytecodeBlock = addInstruction(Opcodes.FSUB)

    @AsmKtDsl
    fun fdiv(): BytecodeBlock = addInstruction(Opcodes.FDIV)

    @AsmKtDsl
    fun fmul(): BytecodeBlock = addInstruction(Opcodes.FMUL)

    @AsmKtDsl
    fun frem(): BytecodeBlock = addInstruction(Opcodes.FREM)

    @AsmKtDsl
    fun fneg(): BytecodeBlock = addInstruction(Opcodes.FNEG)

    @AsmKtDsl
    fun fcmpl(): BytecodeBlock = addInstruction(Opcodes.FCMPL)

    @AsmKtDsl
    fun fcmpg(): BytecodeBlock = addInstruction(Opcodes.FCMPG)

    // double
    @AsmKtDsl
    fun ddiv(): BytecodeBlock = addInstruction(Opcodes.DDIV)

    @AsmKtDsl
    fun dmul(): BytecodeBlock = addInstruction(Opcodes.DMUL)

    @AsmKtDsl
    fun drem(): BytecodeBlock = addInstruction(Opcodes.DREM)

    @AsmKtDsl
    fun dneg(): BytecodeBlock = addInstruction(Opcodes.DNEG)

    @AsmKtDsl
    fun dadd(): BytecodeBlock = addInstruction(Opcodes.DADD)

    @AsmKtDsl
    fun dsub(): BytecodeBlock = addInstruction(Opcodes.DSUB)

    @AsmKtDsl
    fun dcmpl(): BytecodeBlock = addInstruction(Opcodes.DCMPL)

    @AsmKtDsl
    fun dcmpg(): BytecodeBlock = addInstruction(Opcodes.DCMPG)

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    // int to ...
    @AsmKtDsl
    fun i2d(): BytecodeBlock = addInstruction(Opcodes.I2D)

    @AsmKtDsl
    fun i2l(): BytecodeBlock = addInstruction(Opcodes.I2L)

    @AsmKtDsl
    fun i2f(): BytecodeBlock = addInstruction(Opcodes.I2F)

    @AsmKtDsl
    fun i2s(): BytecodeBlock = addInstruction(Opcodes.I2S)

    @AsmKtDsl
    fun i2c(): BytecodeBlock = addInstruction(Opcodes.I2C)

    @AsmKtDsl
    fun i2b(): BytecodeBlock = addInstruction(Opcodes.I2B)

    // long to ...
    @AsmKtDsl
    fun l2d(): BytecodeBlock = addInstruction(Opcodes.L2D)

    @AsmKtDsl
    fun l2i(): BytecodeBlock = addInstruction(Opcodes.L2I)

    @AsmKtDsl
    fun l2f(): BytecodeBlock = addInstruction(Opcodes.L2F)

    // float to ...
    @AsmKtDsl
    fun f2d(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKtDsl
    fun f2i(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKtDsl
    fun f2l(): BytecodeBlock = addInstruction(Opcodes.F2L)

    // double to ...
    @AsmKtDsl
    fun d2f(): BytecodeBlock = addInstruction(Opcodes.D2F)

    @AsmKtDsl
    fun d2i(): BytecodeBlock = addInstruction(Opcodes.D2I)

    @AsmKtDsl
    fun d2l(): BytecodeBlock = addInstruction(Opcodes.D2L)

    // -- TRY CATCH INSTRUCTIONS -- \\
    @AsmKtDsl
    fun athrow(): BytecodeBlock = addInstruction(Opcodes.ATHROW)

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Creates an instruction to mark the current point with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    fun mark(label: Label): BytecodeBlock = addLabel(label)

    /**
     * Marks the given [line] with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKtDsl
    fun line(line: Int, label: Label): BytecodeBlock = addLineNumber(line, label)

    // -- SWITCH INSTRUCTIONS -- \\
    @AsmKtDsl
    fun lookupswitch(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>
    ): BytecodeBlock = addLookupSwitchInstruction(defaultLabel, keys, labels)

    @AsmKtDsl
    fun tableswitch(min: Int, max: Int, defaultLabel: Label, cases: Array<out Label>): BytecodeBlock =
        addTableSwitchInstruction(min, max, defaultLabel, cases)

    // -- MONITOR INSTRUCTIONS -- \\
    @AsmKtDsl
    fun monitorenter(): BytecodeBlock = addInstruction(Opcodes.MONITORENTER)

    @AsmKtDsl
    fun monitorexit(): BytecodeBlock = addInstruction(Opcodes.MONITOREXIT)

    // -- DUP INSTRUCTIONS -- \\
    @AsmKtDsl
    fun dup(): BytecodeBlock = addInstruction(Opcodes.DUP)

    @AsmKtDsl
    fun dup_x2(): BytecodeBlock = addInstruction(Opcodes.DUP_X2)

    @AsmKtDsl
    fun dup_x1(): BytecodeBlock = addInstruction(Opcodes.DUP_X1)

    @AsmKtDsl
    fun dup2_x2(): BytecodeBlock = addInstruction(Opcodes.DUP2_X2)

    @AsmKtDsl
    fun dup2_x1(): BytecodeBlock = addInstruction(Opcodes.DUP2_X1)

    @AsmKtDsl
    fun dup2(): BytecodeBlock = addInstruction(Opcodes.DUP2)

    // -- SWAP INSTRUCTIONS -- \\
    @AsmKtDsl
    fun swap(): BytecodeBlock = addInstruction(Opcodes.SWAP)

    // -- NOP INSTRUCTIONS -- \\
    @AsmKtDsl
    fun nop(): BytecodeBlock = addInstruction(Opcodes.NOP)

    // -- POP INSTRUCTIONS -- \\
    @AsmKtDsl
    fun pop(): BytecodeBlock = addInstruction(Opcodes.POP)

    @AsmKtDsl
    fun pop2(): BytecodeBlock = addInstruction(Opcodes.POP2)

    // -- FRAME INSTRUCTIONS -- \\
    /**
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKtDsl
    fun frame(
        type: Int,
        numLocal: Int,
        local: Array<out Any>,
        numStack: Int,
        stack: Array<out Any>
    ): BytecodeBlock = apply {
        instructions.add(FrameNode(type, numLocal, local.replaceLabels(), numStack, stack.replaceLabels()))
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKtDsl
    fun frame(type: Int, local: Array<out Any>, stack: Array<out Any>): BytecodeBlock =
        frame(type, local.size, local, stack.size, stack)

    // -- INSTRUCTION FUNCTIONS -- \\
    fun addInstruction(opcode: Int): BytecodeBlock = apply {
        when (opcode) {
            Opcodes.RETURN, Opcodes.ARETURN, Opcodes.IRETURN, Opcodes.FRETURN, Opcodes.LRETURN, Opcodes.DRETURN -> returns =
                true
        }

        instructions.add(InsnNode(opcode))
    }

    fun addVarInstruction(opcode: Int, index: Int): BytecodeBlock = apply {
        instructions.add(VarInsnNode(opcode, index))
    }

    fun addIntInstruction(opcode: Int, operand: Int): BytecodeBlock = apply {
        instructions.add(IntInsnNode(opcode, operand))
    }

    fun addTypeInstruction(opcode: Int, descriptor: String): BytecodeBlock = apply {
        instructions.add(TypeInsnNode(opcode, descriptor))
    }

    fun addFieldInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String
    ): BytecodeBlock = apply {
        instructions.add(FieldInsnNode(opcode, owner, name, descriptor))
    }

    fun addMethodInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ): BytecodeBlock = apply {
        instructions.add(MethodInsnNode(opcode, owner, name, descriptor, isInterface))
    }

    fun addJumpInstruction(opcode: Int, label: Label): BytecodeBlock = apply {
        instructions.add(JumpInsnNode(opcode, label.toNode()))
    }

    fun addLabel(label: Label): BytecodeBlock = apply {
        instructions.add(LabelNode(label))
    }

    private fun addLdcInstruction(value: Any): BytecodeBlock = apply {
        instructions.add(LdcInsnNode(value))
    }

    fun addIincInstruction(index: Int, increment: Int): BytecodeBlock = apply {
        instructions.add(IincInsnNode(index, increment))
    }

    fun addTableSwitchInstruction(
        min: Int,
        max: Int,
        defaultLabel: Label,
        labels: Array<out Label>
    ): BytecodeBlock = apply {
        instructions.add(TableSwitchInsnNode(min, max, defaultLabel.toNode(), *labels.toNodeArray()))
    }

    fun addLookupSwitchInstruction(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>
    ): BytecodeBlock = apply {
        instructions.add(LookupSwitchInsnNode(defaultLabel.toNode(), keys, labels.toNodeArray()))
    }

    fun addMultiANewArrayInstruction(descriptor: String, numDimensions: Int): BytecodeBlock = apply {
        instructions.add(MultiANewArrayInsnNode(descriptor, numDimensions))
    }

    fun addLineNumber(line: Int, start: Label): BytecodeBlock = apply {
        instructions.add(LineNumberNode(line, start.toNode()))
    }

    fun toBytecodeMethod(
        name: String,
        access: Int,
        type: MethodType,
        signature: String?,
        exceptions: List<ReferenceType>,
        parent: BytecodeClass
    ): BytecodeMethod = BytecodeMethod(name, access, type, signature, exceptions, parent, this)

    // -- ADDS -- \\
    fun prepend(instructions: InsnList): BytecodeBlock = apply {
        this.instructions.insert(instructions)
    }

    fun prepend(other: BytecodeBlock): BytecodeBlock = apply {
        this.instructions.insert(other.instructions)
    }

    fun append(instructions: InsnList): BytecodeBlock = apply {
        this.instructions.add(instructions)
    }

    fun append(other: BytecodeBlock): BytecodeBlock = apply {
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