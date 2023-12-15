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

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@AsmKtDsl
public inline fun MethodElementBuilder.parameter(
    index: Int,
    name: String,
    flags: ParameterAccessFlags = AccessFlags.none(),
    builder: ParameterElementBuilder.() -> Unit = {},
): ParameterElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return parameter(buildParameterElement(index, name, flags, builder))
}

@AsmKtDsl
public inline fun MethodElementBuilder.parameter(
    index: Int,
    name: String,
    flag: ParameterAccessFlag,
    builder: ParameterElementBuilder.() -> Unit = {},
): ParameterElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return parameter(index, name, flag.asAccessFlags(), builder)
}