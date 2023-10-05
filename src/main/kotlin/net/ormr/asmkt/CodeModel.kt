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

@file:Suppress("FunctionName", "SpellCheckingInspection")

package net.ormr.asmkt

import net.ormr.asmkt.type.TypeDesc
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

@AsmKtDsl
public class CodeModel : Iterable<AbstractInsnNode> {
    public val instructions: InsnList = InsnList()

    public var returns: Boolean = false
        private set

    // see https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html for documentation on all the different instructions

    /**
     * See [ldc](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ldc).
     */
    public fun ldc(value: Any) {
        addLdcInstruction(if (value is TypeDesc) value.asAsmType() else value)
    }

    // -- CONSTANTS -- \\
    /**
     * See [aconst_null](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aconst_null).
     */
    public fun aconst_null() {
        addInstruction(ACONST_NULL)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_m1() {
        addInstruction(ICONST_M1)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_0() {
        addInstruction(ICONST_0)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_1() {
        addInstruction(ICONST_1)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_2() {
        addInstruction(ICONST_2)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_3() {
        addInstruction(ICONST_3)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_4() {
        addInstruction(ICONST_4)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    public fun iconst_5() {
        addInstruction(ICONST_5)
    }

    /**
     * See [lconst_l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lconst_l).
     */
    public fun lconst_0() {
        addInstruction(LCONST_0)
    }

    /**
     * See [lconst_l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lconst_l).
     */
    public fun lconst_1() {
        addInstruction(LCONST_1)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    public fun fconst_0() {
        addInstruction(FCONST_0)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    public fun fconst_1() {
        addInstruction(FCONST_1)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    public fun fconst_2() {
        addInstruction(FCONST_2)
    }

    /**
     * See [dconst_d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dconst_d).
     */
    public fun dconst_0() {
        addInstruction(DCONST_0)
    }

    /**
     * See [dconst_d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dconst_d).
     */
    public fun dconst_1() {
        addInstruction(DCONST_1)
    }

    // -- LOAD -- \\
    /**
     * See [iload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iload).
     */
    public fun iload(index: Int) {
        addVarInstruction(ILOAD, index)
    }

    /**
     * See [lload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lload).
     */
    public fun lload(index: Int) {
        addVarInstruction(LLOAD, index)
    }

    /**
     * See [fload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fload).
     */
    public fun fload(index: Int) {
        addVarInstruction(FLOAD, index)
    }

    /**
     * See [dload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dload).
     */
    public fun dload(index: Int) {
        addVarInstruction(DLOAD, index)
    }

    /**
     * See [aload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aload).
     */
    public fun aload(index: Int) {
        addVarInstruction(ALOAD, index)
    }

    // -- ARRAY LOAD -- \\

    /**
     * See [iaload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iaload).
     */
    public fun iaload() {
        addInstruction(IALOAD)
    }

    /**
     * See [laload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.laload).
     */
    public fun laload() {
        addInstruction(LALOAD)
    }

    /**
     * See [faload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.faload).
     */
    public fun faload() {
        addInstruction(FALOAD)
    }

    /**
     * See [daload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.daload).
     */
    public fun daload() {
        addInstruction(DALOAD)
    }

    /**
     * See [aaload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aaload).
     */
    public fun aaload() {
        addInstruction(AALOAD)
    }

    /**
     * See [baload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.baload).
     */
    public fun baload() {
        addInstruction(BALOAD)
    }

    /**
     * See [caload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.caload).
     */
    public fun caload() {
        addInstruction(CALOAD)
    }

    /**
     * See [saload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.saload).
     */
    public fun saload() {
        addInstruction(SALOAD)
    }

    // -- STORE -- \\
    /**
     * See [istore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.istore).
     */
    public fun istore(index: Int) {
        addVarInstruction(ISTORE, index)
    }

    /**
     * See [lstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lstore).
     */
    public fun lstore(index: Int) {
        addVarInstruction(LSTORE, index)
    }

    /**
     * See [fstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fstore).
     */
    public fun fstore(index: Int) {
        addVarInstruction(FSTORE, index)
    }

    /**
     * See [dstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dstore).
     */
    public fun dstore(index: Int) {
        addVarInstruction(DSTORE, index)
    }

    /**
     * See [astore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.astore).
     */
    public fun astore(index: Int) {
        addVarInstruction(ASTORE, index)
    }

    // -- ARRAY STORE -- \\

    /**
     * See [iastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iastore).
     */
    public fun iastore() {
        addInstruction(IASTORE)
    }

    /**
     * See [lastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lastore).
     */
    public fun lastore() {
        addInstruction(LASTORE)
    }

    /**
     * See [fastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fastore).
     */
    public fun fastore() {
        addInstruction(FASTORE)
    }

    /**
     * See [dastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dastore).
     */
    public fun dastore() {
        addInstruction(DASTORE)
    }

    /**
     * See [aastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aastore).
     */
    public fun aastore() {
        addInstruction(AASTORE)
    }

    /**
     * See [bastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.bastore).
     */
    public fun bastore() {
        addInstruction(BASTORE)
    }

    /**
     * See [castore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.castore).
     */
    public fun castore() {
        addInstruction(CASTORE)
    }

    /**
     * See [sastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.sastore).
     */
    public fun sastore() {
        addInstruction(SASTORE)
    }

    // -- INT -- \\
    /**
     * See [bipush](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.bipush).
     */
    public fun bipush(value: Int) {
        addIntInstruction(BIPUSH, value)
    }

    /**
     * See [sipush](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.sipush).
     */
    public fun sipush(value: Int) {
        addIntInstruction(SIPUSH, value)
    }

    // -- STACK -- \\
    /**
     * See [pop](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.pop).
     */
    public fun pop() {
        addInstruction(POP)
    }

    /**
     * See [pop2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.pop2).
     */
    public fun pop2() {
        addInstruction(POP2)
    }

    /**
     * See [dup](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup).
     */
    public fun dup() {
        addInstruction(DUP)
    }

    /**
     * See [dup_x1](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup_x1).
     */
    public fun dup_x1() {
        addInstruction(DUP_X1)
    }

    /**
     * See [dup_x2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup_x2).
     */
    public fun dup_x2() {
        addInstruction(DUP_X2)
    }

    /**
     * See [dup2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2).
     */
    public fun dup2() {
        addInstruction(DUP2)
    }

    /**
     * See [dup2_x1](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2_x1).
     */
    public fun dup2_x1() {
        addInstruction(DUP2_X1)
    }

    /**
     * See [dup2_x2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2_x2).
     */
    public fun dup2_x2() {
        addInstruction(DUP2_X2)
    }

    /**
     * See [swap](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.swap).
     */
    public fun swap() {
        addInstruction(SWAP)
    }

    /**
     * See [nop](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.nop).
     */
    public fun nop() {
        addInstruction(NOP)
    }

    // -- MATH -- \\
    /**
     * See [iadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iadd).
     */
    public fun iadd() {
        addInstruction(IADD)
    }

    /**
     * See [ladd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ladd).
     */
    public fun ladd() {
        addInstruction(LADD)
    }

    /**
     * See [fadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fadd).
     */
    public fun fadd() {
        addInstruction(FADD)
    }

    /**
     * See [dadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dadd).
     */
    public fun dadd() {
        addInstruction(DADD)
    }

    /**
     * See [isub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.isub).
     */
    public fun isub() {
        addInstruction(ISUB)
    }

    /**
     * See [lsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lsub).
     */
    public fun lsub() {
        addInstruction(LSUB)
    }

    /**
     * See [fsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fsub).
     */
    public fun fsub() {
        addInstruction(FSUB)
    }

    /**
     * See [dsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dsub).
     */
    public fun dsub() {
        addInstruction(DSUB)
    }

    /**
     * See [imul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.imul).
     */
    public fun imul() {
        addInstruction(IMUL)
    }

    /**
     * See [lmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lmul).
     */
    public fun lmul() {
        addInstruction(LMUL)
    }

    /**
     * See [fmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fmul).
     */
    public fun fmul() {
        addInstruction(FMUL)
    }

    /**
     * See [dmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dmul).
     */
    public fun dmul() {
        addInstruction(DMUL)
    }

    /**
     * See [idiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.idiv).
     */
    public fun idiv() {
        addInstruction(IDIV)
    }

    /**
     * See [ldiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ldiv).
     */
    public fun ldiv() {
        addInstruction(LDIV)
    }

    /**
     * See [fdiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fdiv).
     */
    public fun fdiv() {
        addInstruction(FDIV)
    }

    /**
     * See [ddiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ddiv).
     */
    public fun ddiv() {
        addInstruction(DDIV)
    }

    /**
     * See [irem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.irem).
     */
    public fun irem() {
        addInstruction(IREM)
    }

    /**
     * See [lrem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lrem).
     */
    public fun lrem() {
        addInstruction(LREM)
    }

    /**
     * See [frem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.frem).
     */
    public fun frem() {
        addInstruction(FREM)
    }

    /**
     * See [drem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.drem).
     */
    public fun drem() {
        addInstruction(DREM)
    }

    /**
     * See [ineg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ineg).
     */
    public fun ineg() {
        addInstruction(INEG)
    }

    /**
     * See [lneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lneg).
     */
    public fun lneg() {
        addInstruction(LNEG)
    }

    /**
     * See [fneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fneg).
     */
    public fun fneg() {
        addInstruction(FNEG)
    }

    /**
     * See [dneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dneg).
     */
    public fun dneg() {
        addInstruction(DNEG)
    }

    /**
     * See [ishl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ishl).
     */
    public fun ishl() {
        addInstruction(ISHL)
    }

    /**
     * See [lshl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lshl).
     */
    public fun lshl() {
        addInstruction(LSHL)
    }

    /**
     * See [ishr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ishr).
     */
    public fun ishr() {
        addInstruction(ISHR)
    }

    /**
     * See [lshr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lshr).
     */
    public fun lshr() {
        addInstruction(LSHR)
    }

    /**
     * See [iushr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iushr).
     */
    public fun iushr() {
        addInstruction(IUSHR)
    }

    /**
     * See [lushr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lushr).
     */
    public fun lushr() {
        addInstruction(LUSHR)
    }

    /**
     * See [iand](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iand).
     */
    public fun iand() {
        addInstruction(IAND)
    }

    /**
     * See [land](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.land).
     */
    public fun land() {
        addInstruction(LAND)
    }

    /**
     * See [ior](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ior).
     */
    public fun ior() {
        addInstruction(IOR)
    }

    /**
     * See [lor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lor).
     */
    public fun lor() {
        addInstruction(LOR)
    }

    /**
     * See [ixor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ixor).
     */
    public fun ixor() {
        addInstruction(IXOR)
    }

    /**
     * See [lxor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lxor).
     */
    public fun lxor() {
        addInstruction(LXOR)
    }

    /**
     * See [iinc](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iinc).
     */
    public fun iinc(index: Int, increment: Int) {
        addIincInstruction(index, increment)
    }

    // -- CONVERSION -- \\
    /**
     * See [i2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2l).
     */
    public fun i2l() {
        addInstruction(I2L)
    }

    /**
     * See [i2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2f).
     */
    public fun i2f() {
        addInstruction(I2F)
    }

    /**
     * See [i2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2d).
     */
    public fun i2d() {
        addInstruction(I2D)
    }

    /**
     * See [l2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2i).
     */
    public fun l2i() {
        addInstruction(L2I)
    }

    /**
     * See [l2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2f).
     */
    public fun l2f() {
        addInstruction(L2F)
    }

    /**
     * See [l2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2d).
     */
    public fun l2d() {
        addInstruction(L2D)
    }

    /**
     * See [f2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2i).
     */
    public fun f2i() {
        addInstruction(F2I)
    }

    /**
     * See [f2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2l).
     */
    public fun f2l() {
        addInstruction(F2L)
    }

    /**
     * See [f2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2d).
     */
    public fun f2d() {
        addInstruction(F2D)
    }

    /**
     * See [d2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2i).
     */
    public fun d2i() {
        addInstruction(D2I)
    }

    /**
     * See [d2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2l).
     */
    public fun d2l() {
        addInstruction(D2L)
    }

    /**
     * See [d2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2f).
     */
    public fun d2f() {
        addInstruction(D2F)
    }

    /**
     * See [i2b](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2b).
     */
    public fun i2b() {
        addInstruction(I2B)
    }

    /**
     * See [i2c](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2c).
     */
    public fun i2c() {
        addInstruction(I2C)
    }

    /**
     * See [i2s](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2s).
     */
    public fun i2s() {
        addInstruction(I2S)
    }

    // -- COMPARISON -- \\
    /**
     * See [lcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lcmp).
     */
    public fun lcmp() {
        addInstruction(LCMP)
    }

    /**
     * See [fcmpl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fcmpl).
     */
    public fun fcmpl() {
        addInstruction(FCMPL)
    }

    /**
     * See [fcmpg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fcmpg).
     */
    public fun fcmpg() {
        addInstruction(FCMPG)
    }

    /**
     * See [dcmpl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dcmpl).
     */
    public fun dcmpl() {
        addInstruction(DCMPL)
    }

    /**
     * See [dcmpg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dcmpg).
     */
    public fun dcmpg() {
        addInstruction(DCMPG)
    }

    /**
     * See [ifeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifeq).
     */
    public fun ifeq(label: Label) {
        addJumpInstruction(IFEQ, label)
    }

    /**
     * See [ifne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifne).
     */
    public fun ifne(label: Label) {
        addJumpInstruction(IFNE, label)
    }

    /**
     * See [iflt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iflt).
     */
    public fun iflt(label: Label) {
        addJumpInstruction(IFLT, label)
    }

    /**
     * See [ifge](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifge).
     */
    public fun ifge(label: Label) {
        addJumpInstruction(IFGE, label)
    }

    /**
     * See [ifgt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifgt).
     */
    public fun ifgt(label: Label) {
        addJumpInstruction(IFGT, label)
    }

    /**
     * See [ifle](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifle).
     */
    public fun ifle(label: Label) {
        addJumpInstruction(IFLE, label)
    }

    /**
     * See [if_icmpeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpeq).
     */
    public fun if_icmpeq(label: Label) {
        addJumpInstruction(IF_ICMPEQ, label)
    }

    /**
     * See [if_icmpne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpne).
     */
    public fun if_icmpne(label: Label) {
        addJumpInstruction(IF_ICMPNE, label)
    }

    /**
     * See [if_icmplt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmplt).
     */
    public fun if_icmplt(label: Label) {
        addJumpInstruction(IF_ICMPLT, label)
    }

    /**
     * See [if_icmpge](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpge).
     */
    public fun if_icmpge(label: Label) {
        addJumpInstruction(IF_ICMPGE, label)
    }

    /**
     * See [if_icmpgt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpgt).
     */
    public fun if_icmpgt(label: Label) {
        addJumpInstruction(IF_ICMPGT, label)
    }

    /**
     * See [if_icmple](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmple).
     */
    public fun if_icmple(label: Label) {
        addJumpInstruction(IF_ICMPLE, label)
    }

    /**
     * See [if_acmpeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_acmpeq).
     */
    public fun if_acmpeq(label: Label) {
        addJumpInstruction(IF_ACMPEQ, label)
    }

    /**
     * See [if_acmpne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_acmpne).
     */
    public fun if_acmpne(label: Label) {
        addJumpInstruction(IF_ACMPNE, label)
    }

    // -- JUMPS -- \\

    /**
     * See [goto](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.goto).
     */
    @JvmName("_goto")
    public fun goto(label: Label) {
        addJumpInstruction(GOTO, label)
    }

    /**
     * See [jsr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.jsr).
     */
    public fun jsr(label: Label) {
        addJumpInstruction(JSR, label)
    }

    /**
     * See [ret](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ret).
     */
    public fun ret(index: Int) {
        addVarInstruction(RET, index)
    }

    // -- SWITCH -- \\

    /**
     * See [tableswitch](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.tableswitch).
     */
    public fun tableswitch(
        min: Int,
        max: Int,
        defaultLabel: Label,
        vararg labels: Label,
    ) {
        addTableSwitchInstruction(min, max, defaultLabel, labels)
    }

    /**
     * See [lookupswitch](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lookupswitch).
     */
    public fun lookupswitch(
        defaultLabel: Label,
        keys: IntArray,
        vararg labels: Label,
    ) {
        addLookupSwitchInstruction(defaultLabel, keys, labels)
    }

    // -- RETURN -- \\
    /**
     * See [ireturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ireturn).
     */
    public fun ireturn() {
        addInstruction(IRETURN)
    }

    /**
     * See [lreturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lreturn).
     */
    public fun lreturn() {
        addInstruction(LRETURN)
    }

    /**
     * See [freturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.freturn).
     */
    public fun freturn() {
        addInstruction(FRETURN)
    }

    /**
     * See [dreturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dreturn).
     */
    public fun dreturn() {
        addInstruction(DRETURN)
    }

    /**
     * See [areturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.areturn).
     */
    public fun areturn() {
        addInstruction(ARETURN)
    }

    /**
     * See [return](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.return).
     */
    @JvmName("_return")
    public fun `return`() {
        addInstruction(RETURN)
    }

    // -- OBJECT CREATION -- \\
    /**
     * See [new](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.new).
     */
    @JvmName("_new")
    public fun new(typeDescriptor: String) {
        addTypeInstruction(NEW, typeDescriptor)
    }

    // -- ARRAYS -- \\

    /**
     * See [newarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.newarray).
     */
    public fun newarray(type: Int) {
        addIntInstruction(NEWARRAY, type)
    }

    /**
     * See [anewarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.anewarray).
     */
    public fun anewarray(typeDescriptor: String) {
        addTypeInstruction(ANEWARRAY, typeDescriptor)
    }

    /**
     * See [arraylength](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.arraylength).
     */
    public fun arraylength() {
        addInstruction(ARRAYLENGTH)
    }

    // -- THROWING -- \\
    /**
     * See [athrow](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.athrow).
     */
    public fun athrow() {
        addInstruction(ATHROW)
    }

    // -- CHECKCAST -- \\
    /**
     * See [checkcast](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.checkcast).
     */
    public fun checkcast(typeDescriptor: String) {
        addTypeInstruction(CHECKCAST, typeDescriptor)
    }

    // -- INSTANCEOF -- \\
    /**
     * See [instanceof](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.instanceof).
     */
    @JvmName("_instanceof")
    public fun instanceof(typeDescriptor: String) {
        addTypeInstruction(INSTANCEOF, typeDescriptor)
    }

    // -- MONITOR -- \\
    /**
     * See [monitorenter](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.monitorenter).
     */
    public fun monitorenter() {
        addInstruction(MONITORENTER)
    }

    /**
     * See [monitorexit](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.monitorexit).
     */
    public fun monitorexit() {
        addInstruction(MONITOREXIT)
    }

    // -- MULTIANEWARRAY -- \\
    /**
     * See [multianewarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.multianewarray).
     */
    public fun multianewarray(typeDescriptor: String, numDimensions: Int) {
        addMultiANewArrayInstruction(typeDescriptor, numDimensions)
    }

    // -- LABELS -- \\
    /**
     * See [label](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.label).
     */
    public fun label(label: Label = Label()) {
        addLabel(label)
    }

    // -- LINE NUMBER -- \\
    /**
     * See [line number](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.12).
     */
    public fun lineNumber(line: Int, start: Label = Label()) {
        addLineNumber(line, start)
    }

    // -- INVOKE -- \\
    /**
     * See [invokevirtual](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokevirtual).
     */
    public fun invokevirtual(owner: String, name: String, descriptor: String, isInterface: Boolean) {
        addMethodInstruction(INVOKEVIRTUAL, owner, name, descriptor, isInterface)
    }

    /**
     * See [invokespecial](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokespecial).
     */
    public fun invokespecial(owner: String, name: String, descriptor: String, isInterface: Boolean) {
        addMethodInstruction(INVOKESPECIAL, owner, name, descriptor, isInterface)
    }

    /**
     * See [invokestatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokestatic).
     */
    public fun invokestatic(owner: String, name: String, descriptor: String, isInterface: Boolean) {
        addMethodInstruction(INVOKESTATIC, owner, name, descriptor, isInterface)
    }

    /**
     * See [invokeinterface](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokeinterface).
     */
    public fun invokeinterface(owner: String, name: String, descriptor: String) {
        addMethodInstruction(INVOKEINTERFACE, owner, name, descriptor, isInterface = true)
    }

    /**
     * See [invokedynamic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokedynamic).
     */
    public fun invokedynamic(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        vararg bootstrapMethodArguments: Any,
    ) {
        instructions.add(InvokeDynamicInsnNode(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments))
    }

    // -- FIELD -- \\
    /**
     * See [getfield](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.getfield).
     */
    public fun getfield(owner: String, name: String, descriptor: String) {
        addFieldInstruction(GETFIELD, owner, name, descriptor)
    }

    /**
     * See [putfield](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.putfield).
     */
    public fun putfield(owner: String, name: String, descriptor: String) {
        addFieldInstruction(PUTFIELD, owner, name, descriptor)
    }

    /**
     * See [getstatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.getstatic).
     */
    public fun getstatic(owner: String, name: String, descriptor: String) {
        addFieldInstruction(GETSTATIC, owner, name, descriptor)
    }

    /**
     * See [putstatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.putstatic).
     */
    public fun putstatic(owner: String, name: String, descriptor: String) {
        addFieldInstruction(PUTSTATIC, owner, name, descriptor)
    }

    // -- FRAMES -- \\
    /**
     * @see [MethodVisitor.visitFrame]
     */
    public fun frame(
        type: Int,
        numLocal: Int,
        local: Array<out Any>,
        numStack: Int,
        stack: Array<out Any>,
    ) {
        instructions.add(FrameNode(type, numLocal, local.replaceLabels(), numStack, stack.replaceLabels()))
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    public fun frame(type: Int, local: Array<out Any>, stack: Array<out Any>) {
        frame(type, local.size, local, stack.size, stack)
    }

    // -- INSTRUCTIONS -- \\
    public fun addInstruction(opcode: Int) {
        when (opcode) {
            RETURN, ARETURN, IRETURN, FRETURN, LRETURN, DRETURN -> returns = true
        }

        instructions.add(InsnNode(opcode))
    }

    public fun addVarInstruction(opcode: Int, index: Int) {
        instructions.add(VarInsnNode(opcode, index))
    }

    public fun addIntInstruction(opcode: Int, operand: Int) {
        instructions.add(IntInsnNode(opcode, operand))
    }

    public fun addTypeInstruction(opcode: Int, descriptor: String) {
        instructions.add(TypeInsnNode(opcode, descriptor))
    }

    public fun addFieldInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
    ) {
        instructions.add(FieldInsnNode(opcode, owner, name, descriptor))
    }

    public fun addMethodInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean,
    ) {
        instructions.add(MethodInsnNode(opcode, owner, name, descriptor, isInterface))
    }

    public fun addJumpInstruction(opcode: Int, label: Label) {
        instructions.add(JumpInsnNode(opcode, label.toNode()))
    }

    public fun addLabel(label: Label) {
        instructions.add(LabelNode(label))
    }

    private fun addLdcInstruction(value: Any) {
        instructions.add(LdcInsnNode(value))
    }

    public fun addIincInstruction(index: Int, increment: Int) {
        instructions.add(IincInsnNode(index, increment))
    }

    public fun addTableSwitchInstruction(
        min: Int,
        max: Int,
        defaultLabel: Label,
        labels: Array<out Label>,
    ) {
        instructions.add(TableSwitchInsnNode(min, max, defaultLabel.toNode(), *labels.toNodeArray()))
    }

    public fun addLookupSwitchInstruction(
        defaultLabel: Label,
        keys: IntArray,
        labels: Array<out Label>,
    ) {
        instructions.add(LookupSwitchInsnNode(defaultLabel.toNode(), keys, labels.toNodeArray()))
    }

    public fun addMultiANewArrayInstruction(descriptor: String, numDimensions: Int) {
        instructions.add(MultiANewArrayInsnNode(descriptor, numDimensions))
    }

    public fun addLineNumber(line: Int, start: Label) {
        instructions.add(LineNumberNode(line, start.toNode()))
    }


    override fun iterator(): Iterator<AbstractInsnNode> = instructions.iterator()

    /**
     * Returns a copy of `this` array where all the [Label] instances have been wrapped in [LabelNode]s.
     */
    private fun Array<out Label>.toNodeArray(): Array<out LabelNode> = Array(size) { this[it].toNode() }

    /**
     * Returns a copy of `this` array where all instances of [Label] have been replaced with [LabelNode]s.
     */
    private fun Array<out Any>.replaceLabels(): Array<out Any> = Array(size) {
        val value = this[it]
        if (value is Label) value.toNode() else value
    }

    /**
     * Returns `this` [Label] wrapped in a [LabelNode].
     */
    private fun Label.toNode(): LabelNode = LabelNode(this)
}