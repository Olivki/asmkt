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

import java.util.*

public enum class BytecodeClassKind(private val opcode: Int?, internal val opcodeName: String) {
    CLASS(null, "class"),
    ABSTRACT_CLASS(Modifiers.ABSTRACT, "abstract"),
    INTERFACE(Modifiers.INTERFACE, "interface"),
    ANNOTATION(Modifiers.ANNOTATION, "annotation"),
    ENUM(Modifiers.ENUM, "enum"),
    RECORD(Modifiers.RECORD, "record"),
    MODULE(Modifiers.MODULE, "module");

    public fun applyTo(opcode: Int): Int = this.opcode?.let { opcode or it } ?: opcode

    internal companion object {
        internal val havePermittedSubtypes: Set<BytecodeClassKind> = EnumSet.of(CLASS, ABSTRACT_CLASS, INTERFACE)
        internal val notHaveFields: Set<BytecodeClassKind> = EnumSet.of(INTERFACE, ANNOTATION, MODULE)
        internal val notHaveMethods: Set<BytecodeClassKind> = EnumSet.of(MODULE)
        internal val haveAbstractMethods: Set<BytecodeClassKind> = EnumSet.of(ABSTRACT_CLASS, INTERFACE)
        private val values = values().toList()

        internal fun getByOpcodeOrNull(opcode: Int): BytecodeClassKind? =
            values.firstOrNull { it.opcode?.let { c -> Modifiers.contains(c, opcode) } ?: false }
    }
}

internal val BytecodeClassKind.canHavePermittedSubtypes: Boolean
    get() = this in BytecodeClassKind.havePermittedSubtypes