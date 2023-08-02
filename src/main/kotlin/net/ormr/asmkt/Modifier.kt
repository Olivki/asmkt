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

import org.objectweb.asm.Opcodes.*

/**
 * Represents various opcodes that are used as modifiers.
 *
 * Use the [plus] operator to combine multiple modifiers into one, for example, if `public static final` is needed, then
 * one can do `PUBLIC + STATIC + FINAL`, the order doesn't matter.
 */
@JvmInline
public value class Modifier(private val code: Int) {
    /**
     * Returns the result of combining `this` modifier with the [other] modifier.
     */
    public operator fun plus(other: Modifier): Modifier = when {
        this == NONE -> other
        other == NONE -> this
        else -> Modifier(code or other.code)
    }

    public operator fun contains(other: Modifier): Boolean =
        if (this == NONE || other == NONE) false else code and other.code != 0

    public fun asInt(): Int = code

    public companion object {
        /**
         * Not valid on any element. Used as a sentinel value.
         */
        public val NONE: Modifier = Modifier(-1)

        /**
         * Valid on [classes][BytecodeClass].
         */
        public val SUPER: Modifier = Modifier(ACC_SUPER)

        /**
         * Valid on [classes][BytecodeClass].
         */
        public val INTERFACE: Modifier = Modifier(ACC_INTERFACE)

        /**
         * Valid on [classes][BytecodeClass].
         */
        public val ANNOTATION: Modifier = Modifier(ACC_ANNOTATION)

        /**
         * Valid on [classes][BytecodeClass].
         */
        public val MODULE: Modifier = Modifier(ACC_MODULE)

        /**
         * Valid on [classes][BytecodeClass].
         */
        public val RECORD: Modifier = Modifier(ACC_RECORD)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val SYNCHRONIZED: Modifier = Modifier(ACC_SYNCHRONIZED)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val BRIDGE: Modifier = Modifier(ACC_BRIDGE)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val VARARGS: Modifier = Modifier(ACC_VARARGS)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val TRANSIENT: Modifier = Modifier(ACC_TRANSIENT)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val NATIVE: Modifier = Modifier(ACC_NATIVE)

        /**
         * Valid on [methods][BytecodeMethod].
         */
        public val STRICT: Modifier = Modifier(ACC_STRICT)

        /**
         * Valid on [fields][BytecodeField].
         */
        public val VOLATILE: Modifier = Modifier(ACC_VOLATILE)

        /**
         * Valid on inner [fields][BytecodeField] of a [class][BytecodeClass].
         */
        public val ENUM: Modifier = Modifier(ACC_ENUM)

        /**
         * Valid on [modules][BytecodeModule].
         */
        public val OPEN: Modifier = Modifier(ACC_OPEN)

        /**
         * Required on [modules][BytecodeModule].
         */
        public val TRANSITIVE: Modifier = Modifier(ACC_TRANSITIVE)

        /**
         * Required on [modules][BytecodeModule].
         */
        public val STATIC_PHASE: Modifier = Modifier(ACC_STATIC_PHASE)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], [methods][BytecodeMethod],
         * `parameters` and [modules][BytecodeModule].
         *
         * An element marked as `mandated` must be an element that was *implicitly* added in by the compiler, i.e the
         * receiver parameter of a Kotlin extension function.
         */
        public val MANDATED: Modifier = Modifier(ACC_MANDATED)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], [methods][BytecodeMethod], `parameters` and
         * [modules][BytecodeModule].
         */
        public val SYNTHETIC: Modifier = Modifier(ACC_SYNTHETIC)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField] and [methods][BytecodeMethod].
         */
        public val DEPRECATED: Modifier = Modifier(ACC_DEPRECATED)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField] and [methods][BytecodeMethod].
         */
        public val STATIC: Modifier = Modifier(ACC_STATIC)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], [methods][BytecodeMethod] and `parameters`.
         */
        public val FINAL: Modifier = Modifier(ACC_FINAL)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], and [methods][BytecodeMethod].
         */
        public val PUBLIC: Modifier = Modifier(ACC_PUBLIC)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], and [methods][BytecodeMethod].
         */
        public val PROTECTED: Modifier = Modifier(ACC_PROTECTED)

        /**
         * Valid on [classes][BytecodeClass], [fields][BytecodeField], and [methods][BytecodeMethod].
         */
        public val PRIVATE: Modifier = Modifier(ACC_PRIVATE)

        /**
         * Valid on [classes][BytecodeClass] and [methods][BytecodeMethod].
         */
        public val ABSTRACT: Modifier = Modifier(ACC_ABSTRACT)
    }
}