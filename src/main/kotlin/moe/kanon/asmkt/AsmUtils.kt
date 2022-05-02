/*
 * Copyright 2020 Oliver Berg
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

@file:JvmName("AsmUtils")

package moe.kanon.asmkt

import moe.kanon.asmkt.types.MethodType
import moe.kanon.asmkt.types.ReferenceType
import moe.kanon.asmkt.types.PrimitiveVoid
import moe.kanon.asmkt.types.Type
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes

@JvmOverloads
fun handleOf(
    tag: Int,
    owner: ReferenceType,
    name: String,
    type: Type,
    isInterface: Boolean = false
): Handle {
    require(type !is PrimitiveVoid) { "'type' must not be 'void'" }
    return Handle(tag, owner.internalName, name, type.descriptor, isInterface)
}

/**
 * Returns a new [Handle] that points to a bootstrap method with the given [name] and [type] located in the
 * [ConstantBootstraps] class.
 */
fun constantBootstrapsHandleOf(
    name: String,
    type: MethodType
): Handle = handleOf(Opcodes.H_INVOKESTATIC, ReferenceType.CONSTANT_BOOTSTRAPS, name, type, false)