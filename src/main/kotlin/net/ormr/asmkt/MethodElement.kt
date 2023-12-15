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

import net.ormr.asmkt.type.MethodType
import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.tree.MethodNode

public data class MethodElement(
    public val owner: ReferenceType,
    public val name: String,
    override val flags: MethodAccessFlags,
    public val type: MethodType,
    public val signature: String?,
    public val exceptions: List<ReferenceType>,
    public val parameters: List<ParameterElement>,
    public val localVariables: List<LocalVariableElement>,
    public val tryCatchBlocks: List<TryCatchBlockElement>,
    public val defaultAnnotationValue: AnnotationElementDefaultValue?,
    public val annotations: ElementAnnotations,
    public val typeAnnotations: ElementTypeAnnotations,
    public val body: MethodBody,
) : Flaggable<MethodAccessFlag> {
    @OptIn(UnsafeAsmKt::class)
    public fun toAsmNode(): MethodNode {
        val node = MethodNode(flags.asInt(), name, type.descriptor, signature, null)

        node.annotationDefault = defaultAnnotationValue?.getDefaultValue()

        node.exceptions = exceptions
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.internalName }

        node.visibleAnnotations = annotations
            .visibleElements
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }
        node.invisibleAnnotations = annotations
            .invisibleElements
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }

        node.visibleTypeAnnotations = typeAnnotations
            .visibleElements
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }
        node.invisibleTypeAnnotations = typeAnnotations
            .invisibleElements
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }

        node.parameters = parameters
            .mapTo(mutableListOf()) { it.toAsmNode() }
        for (parameter in parameters) {
            val annotations = parameter.annotations
            for (annotation in annotations.visibleElements) {
                annotation
                    .toAsmNode()
                    .accept(node.visitParameterAnnotation(parameter.index, annotation.type.descriptor, true))
            }
            for (annotation in annotations.invisibleElements) {
                annotation
                    .toAsmNode()
                    .accept(node.visitParameterAnnotation(parameter.index, annotation.type.descriptor, false))
            }

            val typeAnnotations = parameter.typeAnnotations
            for (annotation in typeAnnotations.visibleElements) {
                annotation
                    .toAsmNode()
                    .accept(
                        node.visitTypeAnnotation(
                            annotation.typeRef,
                            annotation.typePath,
                            annotation.type.descriptor,
                            true,
                        )
                    )
            }
            for (annotation in typeAnnotations.invisibleElements) {
                annotation
                    .toAsmNode()
                    .accept(
                        node.visitTypeAnnotation(
                            annotation.typeRef,
                            annotation.typePath,
                            annotation.type.descriptor,
                            false,
                        )
                    )
            }
        }

        node.tryCatchBlocks = tryCatchBlocks
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }

        node.localVariables = localVariables
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.toAsmNode() }

        node.instructions.add(body.instructions.getBackingInsnList())

        return node
    }

    public fun asString(): String = buildString {
        append(owner.asString())
        append("::")
        append(name)
        append('(')
        val mappedParameters = parameters.associateByTo(hashMapOf()) { it.index }
        type
            .argumentTypes
            .asSequence()
            .withIndex()
            .joinTo(this) { (i, type) ->
                val name = mappedParameters[i]?.name ?: "param$i"
                append(name)
                append(": ")
                append(type.asString())
            }
        append(") -> ")
        append(type.returnType.asString())
    }
}
