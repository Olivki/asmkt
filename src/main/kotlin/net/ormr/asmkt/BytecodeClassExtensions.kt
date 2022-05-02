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

@file:JvmName("BytecodeClassUtils")

package net.ormr.asmkt

import net.ormr.asmkt.Modifiers.STATIC
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.ReferenceType
import org.objectweb.asm.Opcodes

// -- BYTECODE CLASS -- \\
@JvmSynthetic
@AsmKtDsl
inline fun defineClass(
    type: ReferenceType,
    access: Int = Opcodes.ACC_PUBLIC,
    superType: ReferenceType = OBJECT,
    interfaces: List<ReferenceType> = emptyList(),
    sourceFile: String? = null,
    sourceDebug: String? = null,
    scope: BytecodeClass.() -> Unit
): BytecodeClass = BytecodeClass(type, access, superType, interfaces, sourceFile, sourceDebug).apply(scope)

// -- MODULES -- \\
// TODO: document the throws
@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineModule(
    name: String,
    access: Int,
    version: String? = null,
    scope: BytecodeModule.() -> Unit
): BytecodeModule = defineModule(name, access, version).apply(scope)

// -- FIELDS -- \\
@JvmSynthetic
@AsmKtDsl
fun BytecodeClass.defineField(
    name: String,
    access: Int,
    type: ReferenceType,
    signature: String? = null,
    value: Any? = null,
    scope: BytecodeField.() -> Unit
): BytecodeField = defineField(name, access, type, signature, value).apply(scope)

// -- METHODS -- \\
// TODO: document the throws
@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineMethod(
    name: String,
    access: Int,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    scope: BytecodeMethod.() -> Unit
): BytecodeMethod = defineMethod(name, access, type, signature, exceptions).apply(scope)

@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineConstructor(
    access: Int = Modifiers.PUBLIC,
    descriptor: MethodType = MethodType.VOID,
    body: BytecodeMethod.() -> Unit
): BytecodeMethod = defineConstructor(access, descriptor).apply(body)

@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineStaticInit(body: BytecodeMethod.() -> Unit = {}): BytecodeMethod =
    defineMethod("<clinit>", STATIC, MethodType.VOID).apply(body)

/**
 * Defines a skeleton implementation of the `equals` method for `this` class.
 */
@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineEquals(isFinal: Boolean = false, body: BytecodeMethod.() -> Unit): BytecodeMethod =
    defineEquals(isFinal).apply(body)

/**
 * Defines a skeleton implementation of the `hashCode` method for `this` class.
 */
@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineHashCode(isFinal: Boolean = false, body: BytecodeMethod.() -> Unit): BytecodeMethod =
    defineHashCode(isFinal).apply(body)

/**
 * Defines a skeleton implementation of the `toString` method for `this` class.
 */
@JvmSynthetic
@AsmKtDsl
inline fun BytecodeClass.defineToString(isFinal: Boolean = false, body: BytecodeMethod.() -> Unit): BytecodeMethod =
    defineToString(isFinal).apply(body)