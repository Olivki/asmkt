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

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

public sealed interface ClassFileSerializer {
    public fun encodeToClassNode(model: ClassBuilder): ClassNode

    public fun encodeToBytes(model: ClassBuilder): ByteArray

    public fun encodeToClassFile(model: ClassBuilder): ClassFile = ClassFile(
        version = model.version,
        type = model.type,
        bytes = encodeToBytes(model),
    )

    public companion object Default : ClassFileSerializer {
        override fun encodeToClassNode(model: ClassBuilder): ClassNode {
            TODO("Not yet implemented")
        }

        override fun encodeToBytes(model: ClassBuilder): ByteArray {
            val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val node = encodeToClassNode(model)
            node.accept(writer)
            return writer.toByteArray()
        }
    }
}