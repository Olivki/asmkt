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

import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.Type
import net.ormr.asmkt.types.requireNotVoid
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import java.lang.invoke.ConstantBootstraps

/**
 * Returns a new [Handle] based on the given arguments.
 *
 * This function is more type-safe than creating a [Handle] directly.
 */
public fun handleOf(
    tag: Int,
    owner: ReferenceType,
    name: String,
    type: Type,
    isInterface: Boolean = false,
): Handle {
    requireNotVoid(type)
    return Handle(tag, owner.internalName, name, type.descriptor, isInterface)
}

/**
 * Returns a new [Handle] that points to a bootstrap method with the given [name] and [type] located in the
 * [ConstantBootstraps] class.
 */
public fun constantBootstrapsHandleOf(
    name: String,
    type: MethodType,
): Handle = handleOf(Opcodes.H_INVOKESTATIC, ReferenceType.CONSTANT_BOOTSTRAPS, name, type, false)