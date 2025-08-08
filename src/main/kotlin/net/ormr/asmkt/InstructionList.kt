/*
 * Copyright 2023-2025 Oliver Berg
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

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

/**
 * A read-only [List] view of a [InsnList] instance.
 */
public sealed interface InstructionList : List<AbstractInsnNode> {
    /**
     * The first node in the list, or `null` if the list is empty.
     */
    public val first: AbstractInsnNode?

    /**
     * The last node in the list, or `null` if the list is empty.
     */
    public val last: AbstractInsnNode?

    /**
     * Returns the underlying [InsnList] that the list is wrapped around.
     *
     * Note that this is marked as [UnsafeAsmKt] as using the underlying `InsnList` can violate assumptions that
     * AsmKt makes about the mutability of the `InstructionList`. It is therefore recommended to instead use
     * [toInsnList] unless one *specifically* needs the underlying `InsnList` instance.
     *
     * @see [toInsnList]
     */
    @UnsafeAsmKt
    public fun getBackingInsnList(): InsnList

    /**
     * Returns a new [InsnList] instance containing all instructions specified in the list.
     *
     * Unlike the [getBackingInsnList] function, this function will return a new `InsnList` every time it's invoked.
     */
    public fun toInsnList(): InsnList
}

/**
 * Returns a read-only [List] view of `this` [InsnList].
 *
 * The returned [InstructionList] wraps around the `InsnList` *directly*, meaning that any changes done to `this`
 * `InsnList` instance will be reflected in the returned `InstructionList`.
 *
 * @see [toInstructionList]
 */
public fun InsnList.asInstructionList(): InstructionList = InstructionListImpl(this)

/**
 * Returns a read-only [List] view of `this` [InsnList].
 *
 * The returned [InstructionList] wraps around a *copy* of the `InsnList`, meaning that any changes done to `this`
 * `InsnList` instance will *not* be reflected in the returned `InstructionList`.
 *
 * @see [asInstructionList]
 */
public fun InsnList.toInstructionList(): InstructionList = InstructionListImpl(this.copy())

internal fun InsnList.copy(): InsnList {
    val newList = InsnList()
    newList.add(this)
    return newList
}

private class InstructionListImpl(private val delegate: InsnList) : AbstractList<AbstractInsnNode>(), InstructionList {
    override val first: AbstractInsnNode?
        get() = delegate.first
    override val last: AbstractInsnNode?
        get() = delegate.last

    override val size: Int
        get() = delegate.size()

    override fun get(index: Int): AbstractInsnNode = delegate[index]

    override fun indexOf(element: AbstractInsnNode): Int = delegate.indexOf(element)

    override fun contains(element: AbstractInsnNode): Boolean = delegate.contains(element)

    override fun iterator(): Iterator<AbstractInsnNode> = delegate.iterator()

    override fun listIterator(): ListIterator<AbstractInsnNode> = delegate.iterator()

    @UnsafeAsmKt
    override fun getBackingInsnList(): InsnList = delegate

    override fun toInsnList(): InsnList = delegate.copy()
}