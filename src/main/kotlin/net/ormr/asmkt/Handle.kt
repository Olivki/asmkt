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

@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package net.ormr.asmkt

import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.requireNotVoid
import org.objectweb.asm.Handle

/**
 * Returns a new [Handle] based on the given arguments.
 *
 * This function is more type-safe than creating a [Handle] directly.
 */
public fun Handle(
    kind: HandleKind,
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle {
    requireNotVoid(type)
    return Handle(kind.asInt(), owner.internalName, name, type.descriptor, isInterface)
}

public inline fun GetFieldHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.GET_FIELD, owner, name, type, isInterface)

public inline fun GetStaticHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.GET_STATIC, owner, name, type, isInterface)

public inline fun PutFieldHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.PUT_FIELD, owner, name, type, isInterface)

public inline fun PutStaticHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.PUT_STATIC, owner, name, type, isInterface)

public inline fun InvokeVirtualHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.INVOKE_VIRTUAL, owner, name, type, isInterface)

public inline fun InvokeStaticHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.INVOKE_STATIC, owner, name, type, isInterface)

public inline fun InvokeSpecialHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.INVOKE_SPECIAL, owner, name, type, isInterface)

public inline fun NewInvokeSpecialHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.NEW_INVOKE_SPECIAL, owner, name, type, isInterface)

public inline fun InvokeInterfaceHandle(
    owner: ReferenceType,
    name: String,
    type: MethodType,
    isInterface: Boolean = false,
): Handle = Handle(HandleKind.INVOKE_INTERFACE, owner, name, type, isInterface)