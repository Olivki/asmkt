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

public enum class BytecodeClassKind(public val modifier: Modifier, public val modifierName: String) {
    CLASS(Modifier.NONE, "class"),
    ABSTRACT_CLASS(Modifier.ABSTRACT, "abstract"),
    INTERFACE(Modifier.INTERFACE, "interface"),
    ANNOTATION(Modifier.ANNOTATION, "annotation"),
    ENUM(Modifier.ENUM, "enum"),
    RECORD(Modifier.RECORD, "record"),
    MODULE(Modifier.MODULE, "module");

    internal companion object {
        @JvmField
        internal val INHERITABLE: Set<BytecodeClassKind> = EnumSet.of(CLASS, ABSTRACT_CLASS, INTERFACE)

        @JvmField
        internal val NO_FIELDS: Set<BytecodeClassKind> = EnumSet.of(INTERFACE, ANNOTATION, MODULE)

        @JvmField
        internal val NO_METHODS: Set<BytecodeClassKind> = EnumSet.of(MODULE)

        @JvmField
        internal val WITH_ABSTRACT_METHODS: Set<BytecodeClassKind> = EnumSet.of(ABSTRACT_CLASS, INTERFACE)

        internal fun getByModifierOrNull(modifier: Modifier): BytecodeClassKind? =
            entries.firstOrNull { modifier in it.modifier }
    }
}

public operator fun BytecodeClassKind.plus(modifier: Modifier): Modifier = this.modifier + modifier

public val BytecodeClassKind.isInheritable: Boolean
    get() = this in BytecodeClassKind.INHERITABLE