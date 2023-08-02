/*
 * Copyright 2020-2023 Oliver Berg
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

public enum class HandleKind(private val code: Int) {
    GET_FIELD(H_GETFIELD),
    GET_STATIC(H_GETSTATIC),
    PUT_FIELD(H_PUTFIELD),
    PUT_STATIC(H_PUTSTATIC),
    INVOKE_VIRTUAL(H_INVOKEVIRTUAL),
    INVOKE_STATIC(H_INVOKESTATIC),
    INVOKE_SPECIAL(H_INVOKESPECIAL),
    NEW_INVOKE_SPECIAL(H_NEWINVOKESPECIAL),
    INVOKE_INTERFACE(H_INVOKEINTERFACE);

    public fun asInt(): Int = code
}