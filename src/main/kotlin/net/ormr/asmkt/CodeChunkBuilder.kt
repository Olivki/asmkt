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

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

@AsmKtDsl
public class CodeChunkBuilder internal constructor(public val body: MethodBodyBuilder) {
    internal val instructions: InsnList = InsnList()

    public val startLabel: LabelElement = newLabel()
    public val endLabel: LabelElement = newLabel()

    /**
     * Whether a `return` instruction has been defined in the code.
     */
    public var returns: Boolean = false
        private set

    /**
     * Whether a `throw` instruction has been defined in the code.
     */
    public var throws: Boolean = false
        private set

    public var isReachable: Boolean = true
        private set

    /**
     * Returns a new [LabelElement].
     *
     * @see [newBoundLabel]
     */
    public fun newLabel(): LabelElement = LabelElement()

    /**
     * Returns a new [LabelElement] that has been bound to the *next* instruction.
     *
     * @see [newLabel]
     * @see [bindLabel]
     */
    public fun newBoundLabel(): LabelElement {
        val label = newLabel()
        bindLabel(label)
        return label
    }

    // instructions
    // see https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html for documentation on all the different instructions

    // -- LDC -- \\
    /**
     * See [ldc](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ldc).
     */
    @AsmKtDsl
    public fun ldc(value: Any) {
        val fixedValue = toAsmConstant(value)
        addLdcInstruction(fixedValue)
    }

    // -- CONSTANTS -- \\
    /**
     * See [aconst_null](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aconst_null).
     */
    @AsmKtDsl
    public fun aconst_null() {
        addInstruction(ACONST_NULL)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_m1() {
        addInstruction(ICONST_M1)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_0() {
        addInstruction(ICONST_0)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_1() {
        addInstruction(ICONST_1)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_2() {
        addInstruction(ICONST_2)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_3() {
        addInstruction(ICONST_3)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_4() {
        addInstruction(ICONST_4)
    }

    /**
     * See [iconst_i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iconst_i).
     */
    @AsmKtDsl
    public fun iconst_5() {
        addInstruction(ICONST_5)
    }

    /**
     * See [lconst_l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lconst_l).
     */
    @AsmKtDsl
    public fun lconst_0() {
        addInstruction(LCONST_0)
    }

    /**
     * See [lconst_l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lconst_l).
     */
    @AsmKtDsl
    public fun lconst_1() {
        addInstruction(LCONST_1)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    @AsmKtDsl
    public fun fconst_0() {
        addInstruction(FCONST_0)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    @AsmKtDsl
    public fun fconst_1() {
        addInstruction(FCONST_1)
    }

    /**
     * See [fconst_f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fconst_f).
     */
    @AsmKtDsl
    public fun fconst_2() {
        addInstruction(FCONST_2)
    }

    /**
     * See [dconst_d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dconst_d).
     */
    @AsmKtDsl
    public fun dconst_0() {
        addInstruction(DCONST_0)
    }

    /**
     * See [dconst_d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dconst_d).
     */
    @AsmKtDsl
    public fun dconst_1() {
        addInstruction(DCONST_1)
    }

    // -- LOAD -- \\
    /**
     * See [iload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iload).
     */
    @AsmKtDsl
    public fun iload(index: Int) {
        addVarInstruction(ILOAD, index)
    }

    /**
     * See [lload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lload).
     */
    @AsmKtDsl
    public fun lload(index: Int) {
        addVarInstruction(LLOAD, index)
    }

    /**
     * See [fload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fload).
     */
    @AsmKtDsl
    public fun fload(index: Int) {
        addVarInstruction(FLOAD, index)
    }

    /**
     * See [dload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dload).
     */
    @AsmKtDsl
    public fun dload(index: Int) {
        addVarInstruction(DLOAD, index)
    }

    /**
     * See [aload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aload).
     */
    @AsmKtDsl
    public fun aload(index: Int) {
        addVarInstruction(ALOAD, index)
    }

    // -- ARRAY LOAD -- \\

    /**
     * See [iaload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iaload).
     */
    @AsmKtDsl
    public fun iaload() {
        addInstruction(IALOAD)
    }

    /**
     * See [laload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.laload).
     */
    @AsmKtDsl
    public fun laload() {
        addInstruction(LALOAD)
    }

    /**
     * See [faload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.faload).
     */
    @AsmKtDsl
    public fun faload() {
        addInstruction(FALOAD)
    }

    /**
     * See [daload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.daload).
     */
    @AsmKtDsl
    public fun daload() {
        addInstruction(DALOAD)
    }

    /**
     * See [aaload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aaload).
     */
    @AsmKtDsl
    public fun aaload() {
        addInstruction(AALOAD)
    }

    /**
     * See [baload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.baload).
     */
    @AsmKtDsl
    public fun baload() {
        addInstruction(BALOAD)
    }

    /**
     * See [caload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.caload).
     */
    @AsmKtDsl
    public fun caload() {
        addInstruction(CALOAD)
    }

    /**
     * See [saload](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.saload).
     */
    @AsmKtDsl
    public fun saload() {
        addInstruction(SALOAD)
    }

    // -- STORE -- \\
    /**
     * See [istore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.istore).
     */
    @AsmKtDsl
    public fun istore(index: Int) {
        addVarInstruction(ISTORE, index)
    }

    /**
     * See [lstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lstore).
     */
    @AsmKtDsl
    public fun lstore(index: Int) {
        addVarInstruction(LSTORE, index)
    }

    /**
     * See [fstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fstore).
     */
    @AsmKtDsl
    public fun fstore(index: Int) {
        addVarInstruction(FSTORE, index)
    }

    /**
     * See [dstore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dstore).
     */
    @AsmKtDsl
    public fun dstore(index: Int) {
        addVarInstruction(DSTORE, index)
    }

    /**
     * See [astore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.astore).
     */
    @AsmKtDsl
    public fun astore(index: Int) {
        addVarInstruction(ASTORE, index)
    }

    // -- ARRAY STORE -- \\

    /**
     * See [iastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iastore).
     */
    @AsmKtDsl
    public fun iastore() {
        addInstruction(IASTORE)
    }

    /**
     * See [lastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lastore).
     */
    @AsmKtDsl
    public fun lastore() {
        addInstruction(LASTORE)
    }

    /**
     * See [fastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fastore).
     */
    @AsmKtDsl
    public fun fastore() {
        addInstruction(FASTORE)
    }

    /**
     * See [dastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dastore).
     */
    @AsmKtDsl
    public fun dastore() {
        addInstruction(DASTORE)
    }

    /**
     * See [aastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.aastore).
     */
    @AsmKtDsl
    public fun aastore() {
        addInstruction(AASTORE)
    }

    /**
     * See [bastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.bastore).
     */
    @AsmKtDsl
    public fun bastore() {
        addInstruction(BASTORE)
    }

    /**
     * See [castore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.castore).
     */
    @AsmKtDsl
    public fun castore() {
        addInstruction(CASTORE)
    }

    /**
     * See [sastore](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.sastore).
     */
    @AsmKtDsl
    public fun sastore() {
        addInstruction(SASTORE)
    }

    // -- INT -- \\
    /**
     * See [bipush](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.bipush).
     */
    @AsmKtDsl
    public fun bipush(value: Int) {
        addIntInstruction(BIPUSH, value)
    }

    /**
     * See [sipush](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.sipush).
     */
    @AsmKtDsl
    public fun sipush(value: Int) {
        addIntInstruction(SIPUSH, value)
    }

    // -- STACK -- \\
    /**
     * See [pop](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.pop).
     */
    @AsmKtDsl
    public fun pop() {
        addInstruction(POP)
    }

    /**
     * See [pop2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.pop2).
     */
    @AsmKtDsl
    public fun pop2() {
        addInstruction(POP2)
    }

    /**
     * See [dup](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup).
     */
    @AsmKtDsl
    public fun dup() {
        addInstruction(DUP)
    }

    /**
     * See [dup_x1](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup_x1).
     */
    @AsmKtDsl
    public fun dup_x1() {
        addInstruction(DUP_X1)
    }

    /**
     * See [dup_x2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup_x2).
     */
    @AsmKtDsl
    public fun dup_x2() {
        addInstruction(DUP_X2)
    }

    /**
     * See [dup2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2).
     */
    @AsmKtDsl
    public fun dup2() {
        addInstruction(DUP2)
    }

    /**
     * See [dup2_x1](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2_x1).
     */
    @AsmKtDsl
    public fun dup2_x1() {
        addInstruction(DUP2_X1)
    }

    /**
     * See [dup2_x2](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dup2_x2).
     */
    @AsmKtDsl
    public fun dup2_x2() {
        addInstruction(DUP2_X2)
    }

    /**
     * See [swap](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.swap).
     */
    @AsmKtDsl
    public fun swap() {
        addInstruction(SWAP)
    }

    /**
     * See [nop](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.nop).
     */
    @AsmKtDsl
    public fun nop() {
        addInstruction(NOP)
    }

    // -- MATH -- \\
    /**
     * See [iadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iadd).
     */
    @AsmKtDsl
    public fun iadd() {
        addInstruction(IADD)
    }

    /**
     * See [ladd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ladd).
     */
    @AsmKtDsl
    public fun ladd() {
        addInstruction(LADD)
    }

    /**
     * See [fadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fadd).
     */
    @AsmKtDsl
    public fun fadd() {
        addInstruction(FADD)
    }

    /**
     * See [dadd](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dadd).
     */
    @AsmKtDsl
    public fun dadd() {
        addInstruction(DADD)
    }

    /**
     * See [isub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.isub).
     */
    @AsmKtDsl
    public fun isub() {
        addInstruction(ISUB)
    }

    /**
     * See [lsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lsub).
     */
    @AsmKtDsl
    public fun lsub() {
        addInstruction(LSUB)
    }

    /**
     * See [fsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fsub).
     */
    @AsmKtDsl
    public fun fsub() {
        addInstruction(FSUB)
    }

    /**
     * See [dsub](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dsub).
     */
    @AsmKtDsl
    public fun dsub() {
        addInstruction(DSUB)
    }

    /**
     * See [imul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.imul).
     */
    @AsmKtDsl
    public fun imul() {
        addInstruction(IMUL)
    }

    /**
     * See [lmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lmul).
     */
    @AsmKtDsl
    public fun lmul() {
        addInstruction(LMUL)
    }

    /**
     * See [fmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fmul).
     */
    @AsmKtDsl
    public fun fmul() {
        addInstruction(FMUL)
    }

    /**
     * See [dmul](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dmul).
     */
    @AsmKtDsl
    public fun dmul() {
        addInstruction(DMUL)
    }

    /**
     * See [idiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.idiv).
     */
    @AsmKtDsl
    public fun idiv() {
        addInstruction(IDIV)
    }

    /**
     * See [ldiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ldiv).
     */
    @AsmKtDsl
    public fun ldiv() {
        addInstruction(LDIV)
    }

    /**
     * See [fdiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fdiv).
     */
    @AsmKtDsl
    public fun fdiv() {
        addInstruction(FDIV)
    }

    /**
     * See [ddiv](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ddiv).
     */
    @AsmKtDsl
    public fun ddiv() {
        addInstruction(DDIV)
    }

    /**
     * See [irem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.irem).
     */
    @AsmKtDsl
    public fun irem() {
        addInstruction(IREM)
    }

    /**
     * See [lrem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lrem).
     */
    @AsmKtDsl
    public fun lrem() {
        addInstruction(LREM)
    }

    /**
     * See [frem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.frem).
     */
    @AsmKtDsl
    public fun frem() {
        addInstruction(FREM)
    }

    /**
     * See [drem](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.drem).
     */
    @AsmKtDsl
    public fun drem() {
        addInstruction(DREM)
    }

    /**
     * See [ineg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ineg).
     */
    @AsmKtDsl
    public fun ineg() {
        addInstruction(INEG)
    }

    /**
     * See [lneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lneg).
     */
    @AsmKtDsl
    public fun lneg() {
        addInstruction(LNEG)
    }

    /**
     * See [fneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fneg).
     */
    @AsmKtDsl
    public fun fneg() {
        addInstruction(FNEG)
    }

    /**
     * See [dneg](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dneg).
     */
    @AsmKtDsl
    public fun dneg() {
        addInstruction(DNEG)
    }

    /**
     * See [ishl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ishl).
     */
    @AsmKtDsl
    public fun ishl() {
        addInstruction(ISHL)
    }

    /**
     * See [lshl](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lshl).
     */
    @AsmKtDsl
    public fun lshl() {
        addInstruction(LSHL)
    }

    /**
     * See [ishr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ishr).
     */
    @AsmKtDsl
    public fun ishr() {
        addInstruction(ISHR)
    }

    /**
     * See [lshr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lshr).
     */
    @AsmKtDsl
    public fun lshr() {
        addInstruction(LSHR)
    }

    /**
     * See [iushr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iushr).
     */
    @AsmKtDsl
    public fun iushr() {
        addInstruction(IUSHR)
    }

    /**
     * See [lushr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lushr).
     */
    @AsmKtDsl
    public fun lushr() {
        addInstruction(LUSHR)
    }

    /**
     * See [iand](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iand).
     */
    @AsmKtDsl
    public fun iand() {
        addInstruction(IAND)
    }

    /**
     * See [land](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.land).
     */
    @AsmKtDsl
    public fun land() {
        addInstruction(LAND)
    }

    /**
     * See [ior](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ior).
     */
    @AsmKtDsl
    public fun ior() {
        addInstruction(IOR)
    }

    /**
     * See [lor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lor).
     */
    @AsmKtDsl
    public fun lor() {
        addInstruction(LOR)
    }

    /**
     * See [ixor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ixor).
     */
    @AsmKtDsl
    public fun ixor() {
        addInstruction(IXOR)
    }

    /**
     * See [lxor](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lxor).
     */
    @AsmKtDsl
    public fun lxor() {
        addInstruction(LXOR)
    }

    /**
     * See [iinc](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iinc).
     */
    @AsmKtDsl
    public fun iinc(index: Int, increment: Int) {
        addIincInstruction(index, increment)
    }

    // -- CONVERSION -- \\
    /**
     * See [i2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2l).
     */
    @AsmKtDsl
    public fun i2l() {
        addInstruction(I2L)
    }

    /**
     * See [i2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2f).
     */
    @AsmKtDsl
    public fun i2f() {
        addInstruction(I2F)
    }

    /**
     * See [i2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2d).
     */
    @AsmKtDsl
    public fun i2d() {
        addInstruction(I2D)
    }

    /**
     * See [l2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2i).
     */
    @AsmKtDsl
    public fun l2i() {
        addInstruction(L2I)
    }

    /**
     * See [l2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2f).
     */
    @AsmKtDsl
    public fun l2f() {
        addInstruction(L2F)
    }

    /**
     * See [l2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.l2d).
     */
    @AsmKtDsl
    public fun l2d() {
        addInstruction(L2D)
    }

    /**
     * See [f2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2i).
     */
    @AsmKtDsl
    public fun f2i() {
        addInstruction(F2I)
    }

    /**
     * See [f2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2l).
     */
    @AsmKtDsl
    public fun f2l() {
        addInstruction(F2L)
    }

    /**
     * See [f2d](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.f2d).
     */
    @AsmKtDsl
    public fun f2d() {
        addInstruction(F2D)
    }

    /**
     * See [d2i](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2i).
     */
    @AsmKtDsl
    public fun d2i() {
        addInstruction(D2I)
    }

    /**
     * See [d2l](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2l).
     */
    @AsmKtDsl
    public fun d2l() {
        addInstruction(D2L)
    }

    /**
     * See [d2f](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.d2f).
     */
    @AsmKtDsl
    public fun d2f() {
        addInstruction(D2F)
    }

    /**
     * See [i2b](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2b).
     */
    @AsmKtDsl
    public fun i2b() {
        addInstruction(I2B)
    }

    /**
     * See [i2c](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2c).
     */
    @AsmKtDsl
    public fun i2c() {
        addInstruction(I2C)
    }

    /**
     * See [i2s](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.i2s).
     */
    @AsmKtDsl
    public fun i2s() {
        addInstruction(I2S)
    }

    // -- COMPARISON -- \\
    /**
     * See [lcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lcmp).
     */
    @AsmKtDsl
    public fun lcmp() {
        addInstruction(LCMP)
    }

    /**
     * See [fcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fcmp_op).
     */
    @AsmKtDsl
    public fun fcmpl() {
        addInstruction(FCMPL)
    }

    /**
     * See [fcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.fcmp_op).
     */
    @AsmKtDsl
    public fun fcmpg() {
        addInstruction(FCMPG)
    }

    /**
     * See [dcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dcmp_op).
     */
    @AsmKtDsl
    public fun dcmpl() {
        addInstruction(DCMPL)
    }

    /**
     * See [dcmp](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dcmp_op).
     */
    @AsmKtDsl
    public fun dcmpg() {
        addInstruction(DCMPG)
    }

    /**
     * See [ifnonnull](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifnonnull).
     */
    @AsmKtDsl
    public fun ifnonnull(label: LabelElement) {
        addJumpInstruction(IFNONNULL, label)
    }

    /**
     * See [ifnull](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifnull).
     */
    @AsmKtDsl
    public fun ifnull(label: LabelElement) {
        addJumpInstruction(IFNULL, label)
    }

    /**
     * See [ifeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifeq).
     */
    @AsmKtDsl
    public fun ifeq(label: LabelElement) {
        addJumpInstruction(IFEQ, label)
    }

    /**
     * See [ifne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifne).
     */
    @AsmKtDsl
    public fun ifne(label: LabelElement) {
        addJumpInstruction(IFNE, label)
    }

    /**
     * See [iflt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.iflt).
     */
    @AsmKtDsl
    public fun iflt(label: LabelElement) {
        addJumpInstruction(IFLT, label)
    }

    /**
     * See [ifge](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifge).
     */
    @AsmKtDsl
    public fun ifge(label: LabelElement) {
        addJumpInstruction(IFGE, label)
    }

    /**
     * See [ifgt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifgt).
     */
    @AsmKtDsl
    public fun ifgt(label: LabelElement) {
        addJumpInstruction(IFGT, label)
    }

    /**
     * See [ifle](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ifle).
     */
    @AsmKtDsl
    public fun ifle(label: LabelElement) {
        addJumpInstruction(IFLE, label)
    }

    /**
     * See [if_icmpeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpeq).
     */
    @AsmKtDsl
    public fun if_icmpeq(label: LabelElement) {
        addJumpInstruction(IF_ICMPEQ, label)
    }

    /**
     * See [if_icmpne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpne).
     */
    @AsmKtDsl
    public fun if_icmpne(label: LabelElement) {
        addJumpInstruction(IF_ICMPNE, label)
    }

    /**
     * See [if_icmplt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmplt).
     */
    @AsmKtDsl
    public fun if_icmplt(label: LabelElement) {
        addJumpInstruction(IF_ICMPLT, label)
    }

    /**
     * See [if_icmpge](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpge).
     */
    @AsmKtDsl
    public fun if_icmpge(label: LabelElement) {
        addJumpInstruction(IF_ICMPGE, label)
    }

    /**
     * See [if_icmpgt](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmpgt).
     */
    @AsmKtDsl
    public fun if_icmpgt(label: LabelElement) {
        addJumpInstruction(IF_ICMPGT, label)
    }

    /**
     * See [if_icmple](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_icmple).
     */
    @AsmKtDsl
    public fun if_icmple(label: LabelElement) {
        addJumpInstruction(IF_ICMPLE, label)
    }

    /**
     * See [if_acmpeq](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_acmpeq).
     */
    @AsmKtDsl
    public fun if_acmpeq(label: LabelElement) {
        addJumpInstruction(IF_ACMPEQ, label)
    }

    /**
     * See [if_acmpne](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.if_acmpne).
     */
    @AsmKtDsl
    public fun if_acmpne(label: LabelElement) {
        addJumpInstruction(IF_ACMPNE, label)
    }

    // -- JUMPS -- \\

    /**
     * See [goto](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.goto).
     */
    @JvmName("goto_")
    @AsmKtDsl
    public fun goto(label: LabelElement) {
        addJumpInstruction(GOTO, label)
    }

    /**
     * See [jsr](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.jsr).
     */
    @AsmKtDsl
    public fun jsr(label: LabelElement) {
        addJumpInstruction(JSR, label)
    }

    /**
     * See [ret](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ret).
     */
    @AsmKtDsl
    public fun ret(index: Int) {
        addVarInstruction(RET, index)
    }

    // -- SWITCH -- \\

    /**
     * See [tableswitch](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.tableswitch).
     */
    @AsmKtDsl
    public fun tableswitch(min: Int, max: Int, default: LabelElement, labels: List<LabelElement>) {
        addTableSwitchInstruction(min, max, default, labels)
    }

    /**
     * See [lookupswitch](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lookupswitch).
     *
     * @throws [IllegalArgumentException] if the size of [keys] and [labels] is not the same
     */
    @AsmKtDsl
    public fun lookupswitch(default: LabelElement, keys: IntArray, labels: List<LabelElement>) {
        require(keys.size == labels.size) { "keys (${keys.size}) and labels (${labels.size}) must have the same size" }
        addLookupSwitchInstruction(default, keys, labels)
    }

    // -- RETURN -- \\
    /**
     * See [ireturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.ireturn).
     */
    @AsmKtDsl
    public fun ireturn() {
        addInstruction(IRETURN)
    }

    /**
     * See [lreturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.lreturn).
     */
    @AsmKtDsl
    public fun lreturn() {
        addInstruction(LRETURN)
    }

    /**
     * See [freturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.freturn).
     */
    @AsmKtDsl
    public fun freturn() {
        addInstruction(FRETURN)
    }

    /**
     * See [dreturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.dreturn).
     */
    @AsmKtDsl
    public fun dreturn() {
        addInstruction(DRETURN)
    }

    /**
     * See [areturn](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.areturn).
     */
    @AsmKtDsl
    public fun areturn() {
        addInstruction(ARETURN)
    }

    /**
     * See [return](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.return).
     */
    @JvmName("return_")
    @AsmKtDsl
    public fun `return`() {
        addInstruction(RETURN)
    }

    // -- OBJECT CREATION -- \\
    /**
     * See [new](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.new).
     */
    @JvmName("new_")
    @AsmKtDsl
    public fun new(internalName: String) {
        addTypeInstruction(NEW, internalName)
    }

    // -- ARRAYS -- \\

    /**
     * See [newarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.newarray).
     */
    @AsmKtDsl
    public fun newarray(type: Int) {
        addIntInstruction(NEWARRAY, type)
    }

    /**
     * See [anewarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.anewarray).
     */
    @AsmKtDsl
    public fun anewarray(internalName: String) {
        addTypeInstruction(ANEWARRAY, internalName)
    }

    /**
     * See [arraylength](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.arraylength).
     */
    @AsmKtDsl
    public fun arraylength() {
        addInstruction(ARRAYLENGTH)
    }

    // -- THROWING -- \\
    /**
     * See [athrow](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.athrow).
     */
    @AsmKtDsl
    public fun athrow() {
        addInstruction(ATHROW)
    }

    // -- CHECKCAST -- \\
    /**
     * See [checkcast](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.checkcast).
     */
    @AsmKtDsl
    public fun checkcast(internalName: String) {
        addTypeInstruction(CHECKCAST, internalName)
    }

    // -- INSTANCEOF -- \\
    /**
     * See [instanceof](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.instanceof).
     */
    @JvmName("instanceof_")
    @AsmKtDsl
    public fun instanceof(internalName: String) {
        addTypeInstruction(INSTANCEOF, internalName)
    }

    // -- MONITOR -- \\
    /**
     * See [monitorenter](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.monitorenter).
     */
    @AsmKtDsl
    public fun monitorenter() {
        addInstruction(MONITORENTER)
    }

    /**
     * See [monitorexit](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.monitorexit).
     */
    @AsmKtDsl
    public fun monitorexit() {
        addInstruction(MONITOREXIT)
    }

    // -- MULTIANEWARRAY -- \\
    /**
     * See [multianewarray](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.multianewarray).
     */
    @AsmKtDsl
    public fun multianewarray(typeDescriptor: String, numDimensions: Int) {
        addMultiANewArrayInstruction(typeDescriptor, numDimensions)
    }

    // -- LABELS -- \\
    /**
     * See [label](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.label).
     */
    @AsmKtDsl
    public fun label(label: LabelElement = LabelElement()) {
        addLabel(label)
    }

    // -- INVOKE -- \\
    /**
     * See [invokevirtual](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokevirtual).
     */
    @AsmKtDsl
    public fun invokevirtual(owner: String, name: String, descriptor: String) {
        addMethodInstruction(INVOKEVIRTUAL, owner, name, descriptor, isInterface = false)
    }

    /**
     * See [invokespecial](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokespecial).
     */
    @AsmKtDsl
    public fun invokespecial(owner: String, name: String, descriptor: String) {
        addMethodInstruction(INVOKESPECIAL, owner, name, descriptor, isInterface = false)
    }

    /**
     * See [invokestatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokestatic).
     */
    @AsmKtDsl
    public fun invokestatic(owner: String, name: String, descriptor: String) {
        addMethodInstruction(INVOKESTATIC, owner, name, descriptor, isInterface = false)
    }

    /**
     * See [invokeinterface](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokeinterface).
     */
    @AsmKtDsl
    public fun invokeinterface(owner: String, name: String, descriptor: String) {
        addMethodInstruction(INVOKEINTERFACE, owner, name, descriptor, isInterface = true)
    }

    /**
     * See [invokedynamic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.invokedynamic).
     */
    @AsmKtDsl
    public fun invokedynamic(
        name: String,
        descriptor: String,
        method: AsmHandle,
        arguments: List<Any> = emptyList(),
    ) {
        addInstruction(
            InvokeDynamicInsnNode(
                name,
                descriptor,
                method,
                *(arguments.replaceTypes().toTypedArray()),
            )
        )
    }

    // -- FIELD -- \\
    /**
     * See [getfield](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.getfield).
     */
    @AsmKtDsl
    public fun getfield(owner: String, name: String, descriptor: String) {
        addFieldInstruction(GETFIELD, owner, name, descriptor)
    }

    /**
     * See [putfield](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.putfield).
     */
    @AsmKtDsl
    public fun putfield(owner: String, name: String, descriptor: String) {
        addFieldInstruction(PUTFIELD, owner, name, descriptor)
    }

    /**
     * See [getstatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.getstatic).
     */
    @AsmKtDsl
    public fun getstatic(owner: String, name: String, descriptor: String) {
        addFieldInstruction(GETSTATIC, owner, name, descriptor)
    }

    /**
     * See [putstatic](https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.putstatic).
     */
    @AsmKtDsl
    public fun putstatic(owner: String, name: String, descriptor: String) {
        addFieldInstruction(PUTSTATIC, owner, name, descriptor)
    }

    // -- LABEL -- \\
    /**
     * Binds the given [label] to the *next* instruction.
     *
     * @param [label] the label to bind
     *
     * @see [Label]
     * @see [LabelNode]
     */
    @AsmKtDsl
    public fun bindLabel(label: LabelElement) {
        addLabel(label)
    }

    /**
     * Sets the line number for the instruction at the given [label] to [line].
     *
     * By default, this sets the line number of the *next* instruction.
     *
     * @param [line] the line number
     * @param [start] the label of the first instruction at the line number
     *
     * @see [LineNumberNode]
     */
    @AsmKtDsl
    public fun lineNumber(line: Int, start: LabelElement = newBoundLabel()) {
        addLineNumber(line, start)
    }

    // -- FRAMES -- \\
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
    ) {
        addInstruction(FrameNode(type, numLocal, local.replaceLabels(), numStack, stack.replaceLabels()))
    }

    /**
     * Invokes [frame] with `numLocal` set to the size of [local] and `numStack` set to the size of [stack].
     *
     * @see [MethodVisitor.visitFrame]
     */
    @AsmKtDsl
    public fun frame(type: Int, local: Array<out Any>, stack: Array<out Any>) {
        frame(type, local.size, local, stack.size, stack)
    }

    // -- INSTRUCTIONS -- \\
    private fun checkInstruction(instruction: AbstractInsnNode) {
        when (instruction.opcode) {
            in IRETURN..RETURN -> returns = true
            ATHROW -> throws = true
        }
        when {
            isReachable -> isReachable = when (instruction.opcode) {
                in IRETURN..RETURN, GOTO, ATHROW, LOOKUPSWITCH, TABLESWITCH -> false
                else -> isReachable
            }
            instruction is LabelNode -> isReachable = true
        }
    }

    internal fun addInstruction(instruction: AbstractInsnNode) {
        checkInstruction(instruction)
        instructions.add(instruction)
    }

    internal fun addInstruction(opcode: Int) {
        addInstruction(InsnNode(opcode))
    }

    internal fun addVarInstruction(opcode: Int, index: Int) {
        addInstruction(VarInsnNode(opcode, index))
    }

    private fun addIntInstruction(opcode: Int, operand: Int) {
        addInstruction(IntInsnNode(opcode, operand))
    }

    private fun addTypeInstruction(opcode: Int, internalName: String) {
        addInstruction(TypeInsnNode(opcode, internalName))
    }

    private fun addFieldInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
    ) {
        addInstruction(FieldInsnNode(opcode, owner, name, descriptor))
    }

    private fun addMethodInstruction(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean,
    ) {
        addInstruction(MethodInsnNode(opcode, owner, name, descriptor, isInterface))
    }

    internal fun addJumpInstruction(opcode: Int, label: LabelElement) {
        addInstruction(JumpInsnNode(opcode, label))
    }

    private fun addLabel(label: LabelElement) {
        addInstruction(label)
    }

    private fun addLdcInstruction(value: Any) {
        addInstruction(LdcInsnNode(value))
    }

    private fun addIincInstruction(index: Int, increment: Int) {
        addInstruction(IincInsnNode(index, increment))
    }

    private fun addTableSwitchInstruction(min: Int, max: Int, default: LabelElement, labels: List<LabelElement>) {
        addInstruction(TableSwitchInsnNode(min, max, default, *labels.toNodeArray()))
    }

    private fun addLookupSwitchInstruction(default: LabelElement, keys: IntArray, labels: List<LabelElement>) {
        addInstruction(LookupSwitchInsnNode(default, keys, labels.toNodeArray()))
    }

    private fun addMultiANewArrayInstruction(descriptor: String, numDimensions: Int) {
        addInstruction(MultiANewArrayInsnNode(descriptor, numDimensions))
    }

    private fun addLineNumber(line: Int, start: LabelElement) {
        addInstruction(LineNumberNode(line, start))
    }

    @PublishedApi
    internal fun markStart() {
        bindLabel(startLabel)
    }

    @PublishedApi
    internal fun markEnd() {
        bindLabel(endLabel)
    }

    private fun Array<out Any>.replaceLabels(): Array<out Any> = Array(size) {
        val value = this[it]
        if (value is Label) value.asLabelNode() else value
    }

    override fun toString(): String =
        "CodeBuilder(returns=$returns, throws=$throws, instructions=[${instructions.size()}; ...])"
}