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
import net.ormr.asmkt.types.ReferenceType
import org.objectweb.asm.TypePath
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.TypeAnnotationNode

@AsmKtDsl
data class BytecodeField internal constructor(
    val name: String,
    override val access: Int,
    val type: FieldType,
    val signature: String?,
    val value: Any?,
) : AccessibleBytecode, AnnotatableBytecode, AnnotatableTypeBytecode {
    init {
        require(type.isValidFieldType) { "'type' must not be a 'void' type." }
    }

    /**
     * Returns `true` if `this` field is [volatile][Modifiers.VOLATILE], otherwise `false`.
     */
    val isVolatile: Boolean
        get() = Modifiers.contains(access, Modifiers.VOLATILE)

    /**
     * Returns `true` if `this` field is [volatile][Modifiers.VOLATILE], otherwise `false`.
     */
    val isTransient: Boolean
        get() = Modifiers.contains(access, Modifiers.TRANSIENT)

    // annotations
    private val visibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    // type annotations
    private val visibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    @AsmKtDsl
    override fun defineAnnotation(type: ReferenceType, isVisible: Boolean, allowRepeats: Boolean): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type)
        return handleAnnotations(this, annotation, visibleAnnotations, invisibleAnnotations, isVisible, allowRepeats)
    }

    @AsmKtDsl
    override fun defineTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        annotationType: ReferenceType,
        isVisible: Boolean,
        allowRepeats: Boolean,
    ): BytecodeAnnotation {
        val annotation =
            BytecodeAnnotation(annotationType, TypeAnnotationNode(typeRef, typePath, annotationType.descriptor))
        return handleAnnotations(
            this,
            annotation,
            visibleTypeAnnotations,
            invisibleTypeAnnotations,
            isVisible,
            allowRepeats
        )
    }

    internal fun toNode(): FieldNode {
        val node = FieldNode(access, name, type.descriptor, signature, value)
        // annotations
        node.visibleAnnotations = visibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }
        node.invisibleAnnotations = invisibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }

        // type annotations
        node.visibleTypeAnnotations = visibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }
        node.invisibleTypeAnnotations = invisibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }

        return node
    }
}