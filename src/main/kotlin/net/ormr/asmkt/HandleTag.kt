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

@JvmInline
public value class HandleTag private constructor(private val code: Int) {
    public fun asInt(): Int = code

    public companion object {
        public val GET_FIELD: HandleTag = HandleTag(H_GETFIELD)
        public val GET_STATIC: HandleTag = HandleTag(H_GETSTATIC)
        public val PUT_FIELD: HandleTag = HandleTag(H_PUTFIELD)
        public val PUT_STATIC: HandleTag = HandleTag(H_PUTSTATIC)
        public val INVOKE_VIRTUAL: HandleTag = HandleTag(H_INVOKEVIRTUAL)
        public val INVOKE_STATIC: HandleTag = HandleTag(H_INVOKESTATIC)
        public val INVOKE_SPECIAL: HandleTag = HandleTag(H_INVOKESPECIAL)
        public val NEW_INVOKE_SPECIAL: HandleTag = HandleTag(H_NEWINVOKESPECIAL)
        public val INVOKE_INTERFACE: HandleTag = HandleTag(H_INVOKEINTERFACE)
    }
}