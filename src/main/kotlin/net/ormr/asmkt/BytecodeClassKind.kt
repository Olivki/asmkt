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

public enum class BytecodeClassKind(private val opcode: Int?, internal val displayName: String) {
    CLASS(null, "class"),
    ABSTRACT_CLASS(Modifiers.ABSTRACT, "abstract"),
    INTERFACE(Modifiers.INTERFACE, "interface"),
    ANNOTATION(Modifiers.ANNOTATION, "annotation"),
    ENUM(Modifiers.ENUM, "enum"),
    RECORD(Modifiers.RECORD, "record"),
    MODULE(Modifiers.MODULE, "module");

    public fun applyTo(opcode: Int): Int = this.opcode?.let { opcode or it } ?: opcode

    internal companion object {
        @JvmField
        internal val INHERITABLE: Set<BytecodeClassKind> = EnumSet.of(CLASS, ABSTRACT_CLASS, INTERFACE)

        @JvmField
        internal val NO_FIELDS: Set<BytecodeClassKind> = EnumSet.of(INTERFACE, ANNOTATION, MODULE)

        @JvmField
        internal val NO_METHODS: Set<BytecodeClassKind> = EnumSet.of(MODULE)

        @JvmField
        internal val WITH_ABSTRACT_METHODS: Set<BytecodeClassKind> = EnumSet.of(ABSTRACT_CLASS, INTERFACE)

        internal fun getByOpcodeOrNull(opcode: Int): BytecodeClassKind? =
            entries.firstOrNull { it.opcode?.let { c -> Modifiers.contains(c, opcode) } ?: false }
    }
}

internal val BytecodeClassKind.isInheritable: Boolean
    get() = this in BytecodeClassKind.INHERITABLE