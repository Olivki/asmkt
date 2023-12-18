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

import net.ormr.asmkt.type.HandleType
import net.ormr.asmkt.type.ReferenceType


public typealias AsmHandle = org.objectweb.asm.Handle

/**
 * A type-safe wrapper around [AsmHandle][org.objectweb.asm.Handle].
 */
public sealed interface Handle {
    public val tag: HandleTag
    public val owner: ReferenceType
    public val name: String
    public val type: HandleType

    public val isInterface: Boolean
        get() = false

    public fun toAsmHandle(): AsmHandle = AsmHandle(tag.asInt(), owner.internalName, name, type.descriptor, isInterface)
}

public data class GetFieldHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.GET_FIELD
}

public data class GetStaticHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.GET_STATIC
}

public data class PutFieldHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.PUT_FIELD
}

public data class PutStaticHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.PUT_STATIC
}

public data class InvokeVirtualHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.INVOKE_VIRTUAL
}

public data class InvokeStaticHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.INVOKE_STATIC
}

public data class InvokeSpecialHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.INVOKE_SPECIAL
}

public data class NewInvokeSpecialHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.NEW_INVOKE_SPECIAL
}

public data class InvokeInterfaceHandle(
    override val owner: ReferenceType,
    override val name: String,
    override val type: HandleType,
) : Handle {
    override val tag: HandleTag
        get() = HandleTag.INVOKE_INTERFACE

    override val isInterface: Boolean
        get() = true
}