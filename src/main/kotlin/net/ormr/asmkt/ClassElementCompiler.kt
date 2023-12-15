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

public sealed interface ClassElementCompiler {
    /**
     * Returns a [ByteArray] containing the JVM bytecode for the given [element].
     */
    public fun compileToBytes(element: ClassElement): ByteArray

    public fun compileToClassFile(element: ClassElement): ClassFile = ClassFile(
        version = element.version,
        type = element.type,
        bytes = compileToBytes(element),
    )

    public companion object Default : ClassElementCompiler {
        override fun compileToBytes(element: ClassElement): ByteArray {
            val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val node = element.toAsmNode()
            node.accept(writer)
            return writer.toByteArray()
        }
    }
}