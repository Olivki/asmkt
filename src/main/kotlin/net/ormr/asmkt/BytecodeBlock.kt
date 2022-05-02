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

    @AsmKt
    fun ldc(value: Any): BytecodeBlock = addLdcInstruction(if (value is Type) value.toAsmType() else value)

    @AsmKt
    fun aconst_null(): BytecodeBlock = addInstruction(Opcodes.ACONST_NULL)

    @AsmKt
    fun iconst_m1(): BytecodeBlock = addInstruction(Opcodes.ICONST_M1)

    @AsmKt
    fun iconst_0(): BytecodeBlock = addInstruction(Opcodes.ICONST_0)

    @AsmKt
    fun iconst_1(): BytecodeBlock = addInstruction(Opcodes.ICONST_1)

    @AsmKt
    fun iconst_2(): BytecodeBlock = addInstruction(Opcodes.ICONST_2)

    @AsmKt
    fun iconst_3(): BytecodeBlock = addInstruction(Opcodes.ICONST_3)

    @AsmKt
    fun iconst_4(): BytecodeBlock = addInstruction(Opcodes.ICONST_4)

    @AsmKt
    fun iconst_5(): BytecodeBlock = addInstruction(Opcodes.ICONST_5)

    @AsmKt
    fun lconst_0(): BytecodeBlock = addInstruction(Opcodes.LCONST_0)

    @AsmKt
    fun lconst_1(): BytecodeBlock = addInstruction(Opcodes.LCONST_1)

    @AsmKt
    fun fconst_0(): BytecodeBlock = addInstruction(Opcodes.FCONST_0)

    @AsmKt
    fun fconst_1(): BytecodeBlock = addInstruction(Opcodes.FCONST_1)

    @AsmKt
    fun fconst_2(): BytecodeBlock = addInstruction(Opcodes.FCONST_2)

    @AsmKt
    fun dconst_0(): BytecodeBlock = addInstruction(Opcodes.DCONST_0)

    @AsmKt
    fun dconst_1(): BytecodeBlock = addInstruction(Opcodes.DCONST_1)

    // -- LOAD INSTRUCTIONS -- \\
    @AsmKt
    fun aload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ALOAD, index)

    @AsmKt
    fun iload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ILOAD, index)

    @AsmKt
    fun lload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LLOAD, index)

    @AsmKt
    fun fload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FLOAD, index)

    @AsmKt
    fun dload(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DLOAD, index)

    // -- STORE INSTRUCTIONS -- \\
    @AsmKt
    fun astore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ASTORE, index)

    @AsmKt
    fun istore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.ISTORE, index)

    @AsmKt
    fun lstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.LSTORE, index)

    @AsmKt
    fun fstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.FSTORE, index)

    @AsmKt
    fun dstore(index: Int): BytecodeBlock = addVarInstruction(Opcodes.DSTORE, index)

    @AsmKt
    fun aaload(): BytecodeBlock = addInstruction(Opcodes.AALOAD)

    @AsmKt
    fun aastore(): BytecodeBlock = addInstruction(Opcodes.AASTORE)

    @AsmKt
    fun iaload(): BytecodeBlock = addInstruction(Opcodes.IALOAD)

    @AsmKt
    fun iastore(): BytecodeBlock = addInstruction(Opcodes.IASTORE)

    @AsmKt
    fun laload(): BytecodeBlock = addInstruction(Opcodes.LALOAD)

    @AsmKt
    fun lastore(): BytecodeBlock = addInstruction(Opcodes.LASTORE)

    @AsmKt
    fun baload(): BytecodeBlock = addInstruction(Opcodes.BALOAD)

    @AsmKt
    fun bastore(): BytecodeBlock = addInstruction(Opcodes.BASTORE)

    @AsmKt
    fun saload(): BytecodeBlock = addInstruction(Opcodes.SALOAD)

    @AsmKt
    fun sastore(): BytecodeBlock = addInstruction(Opcodes.SASTORE)

    @AsmKt
    fun caload(): BytecodeBlock = addInstruction(Opcodes.CALOAD)

    @AsmKt
    fun castore(): BytecodeBlock = addInstruction(Opcodes.CASTORE)

    @AsmKt
    fun faload(): BytecodeBlock = addInstruction(Opcodes.FALOAD)

    @AsmKt
    fun fastore(): BytecodeBlock = addInstruction(Opcodes.FASTORE)

    @AsmKt
    fun daload(): BytecodeBlock = addInstruction(Opcodes.DALOAD)

    @AsmKt
    fun dastore(): BytecodeBlock = addInstruction(Opcodes.DASTORE)

    // -- INT INSTRUCTIONS -- \\
    @AsmKt
    fun bipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.BIPUSH, operand)

    @AsmKt
    fun sipush(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.SIPUSH, operand)

    @AsmKt
    fun ret(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.RET, operand)

    // -- ARRAY INSTRUCTIONS -- \\
    @AsmKt
    fun anewarray(descriptor: String): BytecodeBlock = addTypeInstruction(Opcodes.ANEWARRAY, descriptor)

    @AsmKt
    fun newarray(operand: Int): BytecodeBlock = addIntInstruction(Opcodes.NEWARRAY, operand)

    @AsmKt
    fun multianewarray(descriptor: String, numDimensions: Int): BytecodeBlock =
        addMultiANewArrayInstruction(descriptor, numDimensions)

    @AsmKt
    fun arraylength(): BytecodeBlock = addInstruction(Opcodes.ARRAYLENGTH)

    // -- INVOKE/METHOD INSTRUCTIONS -- \\
    @AsmKt
    fun invokestatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESTATIC, owner, name, descriptor, false)

    @AsmKt
    fun invokespecial(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKESPECIAL, owner, name, descriptor, false)

    @AsmKt
    fun invokevirtual(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false)

    @AsmKt
    fun invokeinterface(owner: String, name: String, descriptor: String): BytecodeBlock =
        addMethodInstruction(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true)

    @AsmKt
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
    @AsmKt
    fun areturn(type: FieldType): BytecodeBlock = apply {
        addInstruction(type.getOpcode(Opcodes.IRETURN))
    }

    @AsmKt
    fun voidreturn(): BytecodeBlock = addInstruction(Opcodes.RETURN)

    @AsmKt
    fun areturn(): BytecodeBlock = addInstruction(Opcodes.ARETURN)

    @AsmKt
    fun ireturn(): BytecodeBlock = addInstruction(Opcodes.IRETURN)

    @AsmKt
    fun freturn(): BytecodeBlock = addInstruction(Opcodes.FRETURN)

    @AsmKt
    fun lreturn(): BytecodeBlock = addInstruction(Opcodes.LRETURN)

    @AsmKt
    fun dreturn(): BytecodeBlock = addInstruction(Opcodes.DRETURN)

    // -- FIELD INSTRUCTIONS -- \\
    @AsmKt
    fun getstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETSTATIC, owner, name, descriptor)

    @AsmKt
    fun putstatic(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTSTATIC, owner, name, descriptor)

    @AsmKt
    fun getfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.GETFIELD, owner, name, descriptor)

    @AsmKt
    fun putfield(owner: String, name: String, descriptor: String): BytecodeBlock =
        addFieldInstruction(Opcodes.PUTFIELD, owner, name, descriptor)

    // -- TYPE INSTRUCTIONS -- \\
    @AsmKt
    @JvmName("anew")
    fun new(type: String): BytecodeBlock = addTypeInstruction(Opcodes.NEW, type)

    @AsmKt
    @JvmName("instance_of")
    fun instanceof(type: String): BytecodeBlock = addTypeInstruction(Opcodes.INSTANCEOF, type)

    @AsmKt
    fun checkcast(type: String): BytecodeBlock = addTypeInstruction(Opcodes.CHECKCAST, type)

    // -- JUMP INSTRUCTIONS -- \\
    @AsmKt
    @JvmName("go_to")
    fun goto(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.GOTO, label)

    @AsmKt
    fun ifeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFEQ, label)

    @AsmKt
    fun ifne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNE, label)

    @AsmKt
    fun if_acmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPNE, label)

    @AsmKt
    fun if_acmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ACMPEQ, label)

    @AsmKt
    fun if_icmple(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLE, label)

    @AsmKt
    fun if_icmpgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGT, label)

    @AsmKt
    fun if_icmplt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPLT, label)

    @AsmKt
    fun if_icmpne(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPNE, label)

    @AsmKt
    fun if_icmpeq(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPEQ, label)

    @AsmKt
    fun if_icmpge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IF_ICMPGE, label)

    @AsmKt
    fun ifnonnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNONNULL, label)

    @AsmKt
    fun ifnull(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFNULL, label)

    @AsmKt
    fun iflt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLT, label)

    @AsmKt
    fun ifle(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFLE, label)

    @AsmKt
    fun ifgt(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGT, label)

    @AsmKt
    fun ifge(label: Label): BytecodeBlock = addJumpInstruction(Opcodes.IFGE, label)

    @AsmKt
    fun jsr(branch: Label): BytecodeBlock = addJumpInstruction(Opcodes.JSR, branch)

    // -- PRIMITIVE NUMBER OPERATOR INSTRUCTIONS -- \\
    // int
    @AsmKt
    fun ishr(): BytecodeBlock = addInstruction(Opcodes.ISHR)

    @AsmKt
    fun ishl(): BytecodeBlock = addInstruction(Opcodes.ISHL)

    @AsmKt
    fun iushr(): BytecodeBlock = addInstruction(Opcodes.IUSHR)

    @AsmKt
    fun iand(): BytecodeBlock = addInstruction(Opcodes.IAND)

    @AsmKt
    fun ior(): BytecodeBlock = addInstruction(Opcodes.IOR)

    @AsmKt
    fun ixor(): BytecodeBlock = addInstruction(Opcodes.IXOR)

    @AsmKt
    fun iadd(): BytecodeBlock = addInstruction(Opcodes.IADD)

    @AsmKt
    fun isub(): BytecodeBlock = addInstruction(Opcodes.ISUB)

    @AsmKt
    fun idiv(): BytecodeBlock = addInstruction(Opcodes.IDIV)

    @AsmKt
    fun imul(): BytecodeBlock = addInstruction(Opcodes.IMUL)

    @AsmKt
    fun irem(): BytecodeBlock = addInstruction(Opcodes.IREM)

    @AsmKt
    fun ineg(): BytecodeBlock = addInstruction(Opcodes.INEG)

    @AsmKt
    fun iinc(index: Int, increment: Int): BytecodeBlock = addIincInstruction(index, increment)

    // long
    @AsmKt
    fun lshr(): BytecodeBlock = addInstruction(Opcodes.LSHR)

    @AsmKt
    fun lshl(): BytecodeBlock = addInstruction(Opcodes.LSHL)

    @AsmKt
    fun lushr(): BytecodeBlock = addInstruction(Opcodes.LUSHR)

    @AsmKt
    fun lcmp(): BytecodeBlock = addInstruction(Opcodes.LCMP)

    @AsmKt
    fun land(): BytecodeBlock = addInstruction(Opcodes.LAND)

    @AsmKt
    fun lor(): BytecodeBlock = addInstruction(Opcodes.LOR)

    @AsmKt
    fun lxor(): BytecodeBlock = addInstruction(Opcodes.LXOR)

    @AsmKt
    fun ladd(): BytecodeBlock = addInstruction(Opcodes.LADD)

    @AsmKt
    fun lsub(): BytecodeBlock = addInstruction(Opcodes.LSUB)

    @AsmKt
    fun ldiv(): BytecodeBlock = addInstruction(Opcodes.LDIV)

    @AsmKt
    fun lmul(): BytecodeBlock = addInstruction(Opcodes.LMUL)

    @AsmKt
    fun lrem(): BytecodeBlock = addInstruction(Opcodes.LREM)

    @AsmKt
    fun lneg(): BytecodeBlock = addInstruction(Opcodes.LNEG)

    // float
    @AsmKt
    fun fadd(): BytecodeBlock = addInstruction(Opcodes.FADD)

    @AsmKt
    fun fsub(): BytecodeBlock = addInstruction(Opcodes.FSUB)

    @AsmKt
    fun fdiv(): BytecodeBlock = addInstruction(Opcodes.FDIV)

    @AsmKt
    fun fmul(): BytecodeBlock = addInstruction(Opcodes.FMUL)

    @AsmKt
    fun frem(): BytecodeBlock = addInstruction(Opcodes.FREM)

    @AsmKt
    fun fneg(): BytecodeBlock = addInstruction(Opcodes.FNEG)

    @AsmKt
    fun fcmpl(): BytecodeBlock = addInstruction(Opcodes.FCMPL)

    @AsmKt
    fun fcmpg(): BytecodeBlock = addInstruction(Opcodes.FCMPG)

    // double
    @AsmKt
    fun ddiv(): BytecodeBlock = addInstruction(Opcodes.DDIV)

    @AsmKt
    fun dmul(): BytecodeBlock = addInstruction(Opcodes.DMUL)

    @AsmKt
    fun drem(): BytecodeBlock = addInstruction(Opcodes.DREM)

    @AsmKt
    fun dneg(): BytecodeBlock = addInstruction(Opcodes.DNEG)

    @AsmKt
    fun dadd(): BytecodeBlock = addInstruction(Opcodes.DADD)

    @AsmKt
    fun dsub(): BytecodeBlock = addInstruction(Opcodes.DSUB)

    @AsmKt
    fun dcmpl(): BytecodeBlock = addInstruction(Opcodes.DCMPL)

    @AsmKt
    fun dcmpg(): BytecodeBlock = addInstruction(Opcodes.DCMPG)

    // -- PRIMITIVE NUMBER CONVERSION INSTRUCTIONS -- \\
    // int to ...
    @AsmKt
    fun i2d(): BytecodeBlock = addInstruction(Opcodes.I2D)

    @AsmKt
    fun i2l(): BytecodeBlock = addInstruction(Opcodes.I2L)

    @AsmKt
    fun i2f(): BytecodeBlock = addInstruction(Opcodes.I2F)

    @AsmKt
    fun i2s(): BytecodeBlock = addInstruction(Opcodes.I2S)

    @AsmKt
    fun i2c(): BytecodeBlock = addInstruction(Opcodes.I2C)

    @AsmKt
    fun i2b(): BytecodeBlock = addInstruction(Opcodes.I2B)

    // long to ...
    @AsmKt
    fun l2d(): BytecodeBlock = addInstruction(Opcodes.L2D)

    @AsmKt
    fun l2i(): BytecodeBlock = addInstruction(Opcodes.L2I)

    @AsmKt
    fun l2f(): BytecodeBlock = addInstruction(Opcodes.L2F)

    // float to ...
    @AsmKt
    fun f2d(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKt
    fun f2i(): BytecodeBlock = addInstruction(Opcodes.F2D)

    @AsmKt
    fun f2l(): BytecodeBlock = addInstruction(Opcodes.F2L)

    // double to ...
    @AsmKt
    fun d2f(): BytecodeBlock = addInstruction(Opcodes.D2F)

    @AsmKt
    fun d2i(): BytecodeBlock = addInstruction(Opcodes.D2I)

    @AsmKt
    fun d2l(): BytecodeBlock = addInstruction(Opcodes.D2L)

    // -- TRY CATCH INSTRUCTIONS -- \\
    @AsmKt
    fun athrow(): BytecodeBlock = addInstruction(Opcodes.ATHROW)

    // -- LABEL INSTRUCTIONS -- \\
    /**
     * Creates an instruction to mark the current point with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun mark(label: Label): BytecodeBlock = addLabel(label)

    /**
     * Marks the given [line] with the given [label].
     *
     * @return `this` *(for chaining)*
     */
    @AsmKt
    fun line(line: Int, label: Label): BytecodeBlock = addLineNumber(line, label)

    // -- SWITCH INSTRUCTIONS -- \\
    @AsmKt
    fun lookupswitch(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>
    ): BytecodeBlock = addLookupSwitchInstruction(defaultLabel, keys, labels)

    @AsmKt
    fun tableswitch(min: Int, max: Int, defaultLabel: Label, cases: Array<out Label>): BytecodeBlock =
        addTableSwitchInstruction(min, max, defaultLabel, cases)

    // -- MONITOR INSTRUCTIONS -- \\
    @AsmKt
    fun monitorenter(): BytecodeBlock = addInstruction(Opcodes.MONITORENTER)

    @AsmKt
    fun monitorexit(): BytecodeBlock = addInstruction(Opcodes.MONITOREXIT)

    // -- DUP INSTRUCTIONS -- \\
    @AsmKt
    fun dup(): BytecodeBlock = addInstruction(Opcodes.DUP)

    @AsmKt
    fun dup_x2(): BytecodeBlock = addInstruction(Opcodes.DUP_X2)

    @AsmKt
    fun dup_x1(): BytecodeBlock = addInstruction(Opcodes.DUP_X1)

    @AsmKt
    fun dup2_x2(): BytecodeBlock = addInstruction(Opcodes.DUP2_X2)

    @AsmKt
    fun dup2_x1(): BytecodeBlock = addInstruction(Opcodes.DUP2_X1)

    @AsmKt
    fun dup2(): BytecodeBlock = addInstruction(Opcodes.DUP2)

    // -- SWAP INSTRUCTIONS -- \\
    @AsmKt
    fun swap(): BytecodeBlock = addInstruction(Opcodes.SWAP)

    // -- NOP INSTRUCTIONS -- \\
    @AsmKt
    fun nop(): BytecodeBlock = addInstruction(Opcodes.NOP)

    // -- POP INSTRUCTIONS -- \\
    @AsmKt
    fun pop(): BytecodeBlock = addInstruction(Opcodes.POP)

    @AsmKt
    fun pop2(): BytecodeBlock = addInstruction(Opcodes.POP2)

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
        stack: Array<out Any>
    ): BytecodeBlock = apply {
        instructions.add(FrameNode(type, numLocal, local.replaceLabels(), numStack, stack.replaceLabels()))
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKt
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