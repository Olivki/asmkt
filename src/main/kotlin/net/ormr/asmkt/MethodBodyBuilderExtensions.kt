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

import net.ormr.asmkt.type.FieldTypeDesc
import net.ormr.asmkt.type.MethodTypeDesc

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
public fun invokeSelfStatic(name: String, type: MethodTypeDesc) {
    invokeStatic(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfSpecial(name: String, type: MethodTypeDesc) {
    invokeSpecial(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfVirtual(name: String, type: MethodTypeDesc) {
    invokeVirtual(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun invokeSelfInterface(name: String, type: MethodTypeDesc) {
    invokeInterface(method.parentType, name, type)
}

// -- SELF FIELD -- \\
context(MethodBodyBuilder)
@AsmKtDsl
public fun getSelfStaticField(name: String, type: FieldTypeDesc) {
    getStaticField(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun setSelfStaticField(name: String, type: FieldTypeDesc) {
    setStaticField(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun getSelfField(name: String, type: FieldTypeDesc) {
    getField(method.parentType, name, type)
}

context(MethodBodyBuilder)
@AsmKtDsl
public fun setSelfField(name: String, type: FieldTypeDesc) {
    setField(method.parentType, name, type)
}