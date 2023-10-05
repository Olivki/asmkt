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

public sealed interface TypeDesc {
    /**
     * The descriptor of the type.
     */
    public val descriptor: String

    /**
     * The size of the type.
     *
     * The semantics of the returned value is implementation specific.
     */
    public val size: Int

    /**
     * Returns the [AsmType][org.objectweb.asm.Type] that this type represents.
     */
    public fun asAsmType(): AsmType

    /**
     * Returns a string representation of this type.
     *
     * Note that the returned string is meant for debugging purposes *only*.
     */
    public fun asString(): String
}