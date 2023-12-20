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

public typealias AsmConstantDynamic = org.objectweb.asm.ConstantDynamic

/**
 * A type-safe wrapper around [AsmConstantDynamic][org.objectweb.asm.ConstantDynamic].
 */
public data class ConstantDynamic(
    public val name: String,
    public val type: FieldType,
    public val method: Handle,
    public val arguments: List<Any>,
) {
    public fun toAsmConstantDynamic(): AsmConstantDynamic = AsmConstantDynamic(
        name,
        type.descriptor,
        method.toAsmHandle(),
        *(Array(arguments.size) { toAsmConstant(arguments[it]) }),
    )
}