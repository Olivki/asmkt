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

package net.ormr.asmkt.type

import net.ormr.asmkt.AsmKtReflection

public sealed interface ReturnableTypeDesc : TypeDesc {
    /**
     * The size of the type.
     *
     * Note that the size of a type is *not* the size of the type in bytes, but rather how many slots it takes up.
     * For example, an `int` type has a size of `1`, while a `long` type has a size of `2`, and void has a size of `0`.
     * Following this, an `int` requires a `pop` instruction to get it off the stack, while a long requires
     * `popx2`, and void requires no pop instruction at all.
     */
    override val size: Int
        get() = asAsmType().size

    /**
     * The binary name of the type.
     */
    public val name: String

    /**
     * The simple name of the type, as it would have been declared in the source code.
     */
    public val simpleName: String

    /**
     * Returns the [Class] representation of the type.
     *
     * Whether an attempt to load the class is made is implementation specific.
     */
    @AsmKtReflection
    public fun getOrLoadClass(): Class<*>
}