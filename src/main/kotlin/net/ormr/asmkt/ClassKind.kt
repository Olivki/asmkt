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

import java.util.*

public enum class ClassKind(public val flags: ClassAccessFlags?, public val flagName: String) {
    CLASS(flag = null, "class"),
    ABSTRACT_CLASS(AccessFlag.ABSTRACT, "abstract"),
    INTERFACE(AccessFlag.INTERFACE + AccessFlag.ABSTRACT, "interface"),
    ANNOTATION(AccessFlag.ANNOTATION + AccessFlag.ABSTRACT + AccessFlag.INTERFACE, "annotation"),
    ENUM(AccessFlag.ENUM, "enum"),
    RECORD(AccessFlag.RECORD, "record"),
    MODULE(AccessFlag.MODULE, "module");

    constructor(flag: ClassAccessFlag?, flagName: String) : this(flag?.asAccessFlags(), flagName)

    internal fun fold(flags: ClassAccessFlags, treatSuperSpecially: Boolean): Int = when (this) {
        RECORD, ABSTRACT_CLASS, CLASS, ENUM -> ((this + flags) + if (treatSuperSpecially) AccessFlag.SUPER else AccessFlag.NONE).asInt()
        MODULE, ANNOTATION, INTERFACE -> (this + flags).asInt()
    }

    internal companion object {
        @JvmField
        internal val INHERITABLE: Set<ClassKind> = EnumSet.of(CLASS, ABSTRACT_CLASS, INTERFACE)

        @JvmField
        internal val NO_FIELDS: Set<ClassKind> = EnumSet.of(ANNOTATION, MODULE)

        @JvmField
        internal val NO_METHODS: Set<ClassKind> = EnumSet.of(MODULE)

        @JvmField
        internal val WITH_ABSTRACT_METHODS: Set<ClassKind> = EnumSet.of(ABSTRACT_CLASS, INTERFACE)

        internal fun fromFlagsOrNull(flags: ClassAccessFlags): ClassKind? {
            for (entry in entries) {
                val flag = entry.flags ?: continue
                if (flag in flags) return entry
            }
            return null
        }
    }
}

internal operator fun ClassKind.plus(flags: ClassAccessFlags): ClassAccessFlags =
    this.flags?.let { it + flags } ?: flags

public val ClassKind.isInheritable: Boolean
    get() = this in ClassKind.INHERITABLE