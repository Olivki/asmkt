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

import net.ormr.asmkt.Modifiers.ABSTRACT
import net.ormr.asmkt.Modifiers.FINAL
import net.ormr.asmkt.Modifiers.MANDATED
import net.ormr.asmkt.Modifiers.PRIVATE
import net.ormr.asmkt.Modifiers.PROTECTED
import net.ormr.asmkt.Modifiers.PUBLIC
import net.ormr.asmkt.Modifiers.STATIC
import net.ormr.asmkt.Modifiers.SYNTHETIC

/**
 * Represents a type that has an `access` value defined.
 */
interface AccessibleBytecode {
    /**
     * The access code of `this` element.
     *
     * See the values defined in [Modifiers] for a list of potentially valid codes, which values are valid depends on the
     * type of `this` element.
     */
    val access: Int
}

/**
 * Returns `true` if `this` element is [static][Modifiers.STATIC], otherwise `false`.
 */
val AccessibleBytecode.isStatic: Boolean
    get() = Modifiers.contains(access, STATIC)

/**
 * Returns `true` if `this` element is [final][Modifiers.FINAL], otherwise `false`.
 */
val AccessibleBytecode.isFinal: Boolean
    get() = Modifiers.contains(access, FINAL)

/**
 * Returns `true` if `this` element is [abstract][Modifiers.ABSTRACT], otherwise `false`.
 */
val AccessibleBytecode.isAbstract: Boolean
    get() = Modifiers.contains(access, ABSTRACT)

/**
 * Returns `true` if `this` element is [public][Modifiers.PUBLIC], otherwise `false`.
 */
val AccessibleBytecode.isPublic: Boolean
    get() = Modifiers.contains(access, PUBLIC)

/**
 * Returns `true` if `this` element is [protected][Modifiers.PROTECTED], otherwise `false`.
 */
val AccessibleBytecode.isProtected: Boolean
    get() = Modifiers.contains(access, PROTECTED)

/**
 * Returns `true` if `this` element is [private][Modifiers.PRIVATE], otherwise `false`.
 */
val AccessibleBytecode.isPrivate: Boolean
    get() = Modifiers.contains(access, PRIVATE)

/**
 * Returns `true` if `this` element is [mandated][Modifiers.MANDATED], otherwise `false`.
 */
val AccessibleBytecode.isMandated: Boolean
    get() = Modifiers.contains(access, MANDATED)

/**
 * Returns `true` if `this` element is [synthetic][Modifiers.SYNTHETIC], otherwise `false`.
 */
val AccessibleBytecode.isSynthetic: Boolean
    get() = Modifiers.contains(access, SYNTHETIC)