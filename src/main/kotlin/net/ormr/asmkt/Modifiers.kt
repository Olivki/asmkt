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

import org.objectweb.asm.Opcodes.*

/**
 * Contains all the opcodes that are used as modifiers, and combinations of them for ease of use.
 */
public object Modifiers {
    // -- MISC -- \\
    /**
     * Valid on classes.
     */
    public const val SUPER: Int = ACC_SUPER

    /**
     * Valid on methods.
     */
    public const val SYNCHRONIZED: Int = ACC_SYNCHRONIZED

    /**
     * Valid on modules.
     */
    public const val OPEN: Int = ACC_OPEN

    /**
     * Required on modules.
     */
    public const val TRANSITIVE: Int = ACC_TRANSITIVE

    /**
     * Valid on fields.
     */
    public const val VOLATILE: Int = ACC_VOLATILE

    /**
     * Valid on methods.
     */
    public const val BRIDGE: Int = ACC_BRIDGE

    /**
     * Required on modules.
     */
    public const val STATIC_PHASE: Int = ACC_STATIC_PHASE

    /**
     * Valid on methods.
     */
    public const val VARARGS: Int = ACC_VARARGS

    /**
     * Valid on fields.
     */
    public const val TRANSIENT: Int = ACC_TRANSIENT

    /**
     * Valid on methods.
     */
    public const val NATIVE: Int = ACC_NATIVE

    /**
     * Valid on classes.
     */
    public const val INTERFACE: Int = ACC_INTERFACE

    /**
     * Valid on methods.
     */
    public const val STRICT: Int = ACC_STRICT

    /**
     * Valid on classes.
     */
    public const val ANNOTATION: Int = ACC_ANNOTATION

    /**
     * Valid on inner field of a class.
     */
    public const val ENUM: Int = ACC_ENUM

    /**
     * Valid on classes, fields, methods, parameters and modules.
     *
     * An element marked as `mandated` must be an element that was *implicitly* added in by the compiler, i.e the
     * receiver parameter of a Kotlin extension function.
     */
    public const val MANDATED: Int = ACC_MANDATED

    /**
     * Valid on classes.
     */
    public const val MODULE: Int = ACC_MODULE

    // -- ASM SPECIFIC CONSTANTS -- \\
    /**
     * Valid on classes, fields and methods.
     */
    public const val DEPRECATED: Int = ACC_DEPRECATED

    /**
     * Valid on classes.
     */
    public const val RECORD: Int = ACC_RECORD

    // -- PACKAGE PRIVATE -- \\
    /**
     * Valid on classes and methods.
     */
    public const val ABSTRACT: Int = ACC_ABSTRACT

    /**
     * Valid on fields and methods.
     */
    public const val STATIC: Int = ACC_STATIC

    /**
     * Valid on classes, fields, methods and parameters.
     */
    public const val FINAL: Int = ACC_FINAL

    /**
     * Valid on classes, fields, methods, parameters and modules.
     */
    public const val SYNTHETIC: Int = ACC_SYNTHETIC

    /**
     * Valid on classes, fields, methods and parameters.
     */
    public const val FINAL_SYNTHETIC: Int = ACC_FINAL or ACC_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val FINAL_STATIC: Int = ACC_FINAL or ACC_STATIC

    /**
     * Valid on fields and methods.
     */
    public const val STATIC_SYNTHETIC: Int = ACC_STATIC or ACC_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val FINAL_STATIC_SYNTHETIC: Int = ACC_FINAL or ACC_STATIC or ACC_SYNTHETIC

    // -- PUBLIC -- \\
    /**
     * Valid on classes, fields and methods.
     */
    public const val PUBLIC: Int = ACC_PUBLIC

    /**
     * Valid on classes and methods.
     */
    public const val PUBLIC_ABSTRACT: Int = PUBLIC or ABSTRACT

    /**
     * Valid on fields and methods.
     */
    public const val PUBLIC_STATIC: Int = PUBLIC or STATIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PUBLIC_FINAL: Int = PUBLIC or FINAL

    /**
     * Valid on classes, fields and methods.
     */
    public const val PUBLIC_SYNTHETIC: Int = PUBLIC or SYNTHETIC

    /**
     * Valid on classes and methods.
     */
    public const val PUBLIC_ABSTRACT_SYNTHETIC: Int = PUBLIC_ABSTRACT or SYNTHETIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PUBLIC_FINAL_SYNTHETIC: Int = PUBLIC or FINAL_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PUBLIC_FINAL_STATIC: Int = PUBLIC or FINAL_STATIC

    /**
     * Valid on fields and methods.
     */
    public const val PUBLIC_STATIC_SYNTHETIC: Int = PUBLIC or STATIC_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PUBLIC_FINAL_STATIC_SYNTHETIC: Int = PUBLIC or FINAL_STATIC_SYNTHETIC

    // -- PROTECTED -- \\
    /**
     * Valid on classes, fields and methods.
     */
    public const val PROTECTED: Int = ACC_PROTECTED

    /**
     * Valid on classes and methods.
     */
    public const val PROTECTED_ABSTRACT: Int = PROTECTED or ABSTRACT

    /**
     * Valid on fields and methods.
     */
    public const val PROTECTED_STATIC: Int = PROTECTED or STATIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PROTECTED_FINAL: Int = PROTECTED or FINAL

    /**
     * Valid on classes, fields and methods.
     */
    public const val PROTECTED_SYNTHETIC: Int = PROTECTED or SYNTHETIC

    /**
     * Valid on classes and methods.
     */
    public const val PROTECTED_ABSTRACT_SYNTHETIC: Int = PROTECTED_ABSTRACT or SYNTHETIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PROTECTED_FINAL_SYNTHETIC: Int = PROTECTED or FINAL_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PROTECTED_FINAL_STATIC: Int = PROTECTED or FINAL_STATIC

    /**
     * Valid on fields and methods.
     */
    public const val PROTECTED_STATIC_SYNTHETIC: Int = PROTECTED or STATIC_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PROTECTED_FINAL_STATIC_SYNTHETIC: Int = PROTECTED or FINAL_STATIC_SYNTHETIC

    // -- PRIVATE -- \\
    /**
     * Valid on classes, fields and methods.
     */
    public const val PRIVATE: Int = ACC_PRIVATE

    /**
     * Valid on fields and methods.
     */
    public const val PRIVATE_STATIC: Int = PRIVATE or STATIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PRIVATE_FINAL: Int = PRIVATE or FINAL

    /**
     * Valid on classes, fields and methods.
     */
    public const val PRIVATE_SYNTHETIC: Int = PRIVATE or SYNTHETIC

    /**
     * Valid on classes, fields and methods.
     */
    public const val PRIVATE_FINAL_SYNTHETIC: Int = PRIVATE or FINAL_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PRIVATE_FINAL_STATIC: Int = PRIVATE or FINAL_STATIC

    /**
     * Valid on fields and methods.
     */
    public const val PRIVATE_STATIC_SYNTHETIC: Int = PRIVATE or STATIC_SYNTHETIC

    /**
     * Valid on fields and methods.
     */
    public const val PRIVATE_FINAL_STATIC_SYNTHETIC: Int = PRIVATE or FINAL_STATIC_SYNTHETIC

    // TODO: do these work like they should?
    /**
     * Returns the result of folding [first] against [rest], using a `or` operation on each value.
     */
    public fun orFold(first: Int, vararg rest: Int): Int = rest.fold(first) { acc, value -> acc or value }

    /**
     * Returns the result of folding [first] against [rest], using a `and` operation on each value.
     */
    public fun andFold(first: Int, vararg rest: Int): Int = rest.fold(first) { acc, value -> acc and value }

    // TODO: better documentation
    /**
     * Returns `true` if the given [code] is contained inside of [other], otherwise `false`.
     */
    public fun contains(code: Int, other: Int): Boolean = code and other != 0
}