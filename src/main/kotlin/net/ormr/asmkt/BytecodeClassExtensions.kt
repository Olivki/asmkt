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

import net.ormr.asmkt.types.FieldType
import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// -- BYTECODE CLASS -- \\
@AsmKtDsl
public inline fun defineClass(
    type: ReferenceType,
    version: BytecodeVersion,
    kind: BytecodeClassKind = BytecodeClassKind.CLASS,
    access: Int = Modifiers.PUBLIC,
    superType: ReferenceType = OBJECT,
    interfaces: List<ReferenceType> = emptyList(),
    permittedSubtypes: List<ReferenceType> = emptyList(),
    sourceFile: String? = null,
    sourceDebug: String? = null,
    scope: BytecodeClass.() -> Unit,
): BytecodeClass {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return BytecodeClass(
        type,
        version,
        kind,
        access,
        superType,
        interfaces,
        permittedSubtypes,
        sourceFile,
        sourceDebug,
    ).apply(scope)
}

// -- MODULES -- \\
// TODO: document the throws
@AsmKtDsl
public inline fun BytecodeClass.defineModule(
    name: String,
    access: Int,
    version: String? = null,
    scope: BytecodeModule.() -> Unit,
): BytecodeModule {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineModule(name, access, version).apply(scope)
}

// -- FIELDS -- \\
@AsmKtDsl
public fun BytecodeClass.defineField(
    name: String,
    access: Int,
    type: ReferenceType,
    signature: String? = null,
    value: Any? = null,
    scope: BytecodeField.() -> Unit,
): BytecodeField {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineField(name, access, type, signature, value).apply(scope)
}

// -- METHODS -- \\
// TODO: document the throws
@AsmKtDsl
public inline fun BytecodeClass.defineMethod(
    name: String,
    access: Int,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    scope: BytecodeMethod.() -> Unit,
): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineMethod(name, access, type, signature, exceptions).apply(scope)
}

@AsmKtDsl
public inline fun BytecodeClass.defineRecordComponent(
    name: String,
    type: FieldType,
    signature: String? = null,
    scope: BytecodeRecordComponent.() -> Unit,
): BytecodeRecordComponent {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineRecordComponent(name, type, signature).apply(scope)
}

@AsmKtDsl
public inline fun BytecodeClass.defineConstructor(
    access: Int = Modifiers.PUBLIC,
    descriptor: MethodType = MethodType.VOID,
    scope: BytecodeMethod.() -> Unit,
): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineConstructor(access, descriptor).apply(scope)
}

@AsmKtDsl
public inline fun BytecodeClass.defineStaticInit(scope: BytecodeMethod.() -> Unit): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineStaticInit().apply(scope)
}

/**
 * Defines a skeleton implementation of the `equals` method for `this` class.
 */
@AsmKtDsl
public inline fun BytecodeClass.defineEquals(
    isFinal: Boolean = false,
    scope: BytecodeMethod.() -> Unit,
): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineEquals(isFinal).apply(scope)
}

/**
 * Defines a skeleton implementation of the `hashCode` method for `this` class.
 */
@AsmKtDsl
public inline fun BytecodeClass.defineHashCode(
    isFinal: Boolean = false,
    scope: BytecodeMethod.() -> Unit,
): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineHashCode(isFinal).apply(scope)
}

/**
 * Defines a skeleton implementation of the `toString` method for `this` class.
 */
@AsmKtDsl
public inline fun BytecodeClass.defineToString(
    isFinal: Boolean = false,
    scope: BytecodeMethod.() -> Unit,
): BytecodeMethod {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineToString(isFinal).apply(scope)
}