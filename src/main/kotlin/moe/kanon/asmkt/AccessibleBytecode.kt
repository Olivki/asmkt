/*
 * Copyright 2020 Oliver Berg
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

package moe.kanon.asmkt

import moe.kanon.asmkt.Modifiers.ABSTRACT
import moe.kanon.asmkt.Modifiers.FINAL
import moe.kanon.asmkt.Modifiers.MANDATED
import moe.kanon.asmkt.Modifiers.PRIVATE
import moe.kanon.asmkt.Modifiers.PROTECTED
import moe.kanon.asmkt.Modifiers.PUBLIC
import moe.kanon.asmkt.Modifiers.STATIC
import moe.kanon.asmkt.Modifiers.SYNTHETIC

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

    // -- UTIL FUNCTIONS -- \\
    // these properties are never overridden, but they are defined inside of here rather than as extension functions
    // to make usage of them cleaner from the Java side. The properties defined here are also just ones that effect
    // more than one general implementation, more specific ones are defined in the respective classes

    /**
     * Returns `true` if `this` element is [static][Modifiers.STATIC], otherwise `false`.
     */
    @JvmDefault
    val isStatic: Boolean
        get() = access and STATIC != 0

    /**
     * Returns `true` if `this` element is [final][Modifiers.FINAL], otherwise `false`.
     */
    @JvmDefault
    val isFinal: Boolean
        get() = access and FINAL != 0

    /**
     * Returns `true` if `this` element is [abstract][Modifiers.ABSTRACT], otherwise `false`.
     */
    @JvmDefault
    val isAbstract: Boolean
        get() = access and ABSTRACT != 0

    /**
     * Returns `true` if `this` element is [public][Modifiers.PUBLIC], otherwise `false`.
     */
    @JvmDefault
    val isPublic: Boolean
        get() = access and PUBLIC != 0

    /**
     * Returns `true` if `this` element is [protected][Modifiers.PROTECTED], otherwise `false`.
     */
    @JvmDefault
    val isProtected: Boolean
        get() = access and PROTECTED != 0

    /**
     * Returns `true` if `this` element is [private][Modifiers.PRIVATE], otherwise `false`.
     */
    @JvmDefault
    val isPrivate: Boolean
        get() = access and PRIVATE != 0

    /**
     * Returns `true` if `this` element is [mandated][Modifiers.MANDATED], otherwise `false`.
     */
    @JvmDefault
    val isMandated: Boolean
        get() = access and MANDATED != 0

    /**
     * Returns `true` if `this` element is [synthetic][Modifiers.SYNTHETIC], otherwise `false`.
     */
    @JvmDefault
    val isSynthetic: Boolean
        get() = access and SYNTHETIC != 0
}