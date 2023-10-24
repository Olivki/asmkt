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

/**
 * Represents a type that can be returned from a method.
 *
 * This includes all types that are not [method type][MethodType]s.
 */
public sealed interface ReturnableType : Type {
    /**
     * How many slots this type takes up on the stack.
     */
    override val slotSize: Int
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