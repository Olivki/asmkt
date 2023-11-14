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

import net.ormr.asmkt.type.FieldType
import net.ormr.asmkt.type.MethodType

/**
 * Pushes the instructions needed to compute the bitwise negation of the value currently at the top of the stack.
 *
 * Equivalent to the code `x ^ 1` where `x` is a boolean.
 */
context(MethodBodyBuilder)
@AsmKtDsl
public fun not() {
    withCode {
        iconst_1()
        ixor()
    }
}

// -- SELF INVOKE -- \\
context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfStatic(name: String, type: MethodType) {
    invokeStatic(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfSpecial(name: String, type: MethodType) {
    invokeSpecial(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfVirtual(name: String, type: MethodType) {
    invokeVirtual(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfInterface(name: String, type: MethodType) {
    invokeInterface(method.ownerType, name, type)
}

// -- SELF FIELD -- \\
context(MethodBodyBuilder)
@AsmKtDsl
public fun getSelfStaticField(name: String, type: FieldType) {
    getStaticField(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun setSelfStaticField(name: String, type: FieldType) {
    setStaticField(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun getSelfField(name: String, type: FieldType) {
    getField(method.ownerType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun setSelfField(name: String, type: FieldType) {
    setField(method.ownerType, name, type)
}