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

import net.ormr.asmkt.AnnotationElementValue.*
import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.TypePath
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.TypeAnnotationNode

public sealed class AbstractAnnotationElement<N : AnnotationNode> {
    /**
     * The type of the annotation.
     */
    public abstract val type: ReferenceType

    /**
     * The values of the annotation.
     */
    public abstract val values: Map<String, AnnotationElementValue>

    protected fun populateAsmNode(node: N): N {
        for ((name, wrapper) in values) {
            when (wrapper) {
                is ForString -> node.visit(name, wrapper.value)
                is ForBoolean -> node.visit(name, wrapper.value)
                is ForChar -> node.visit(name, wrapper.value)
                is ForByte -> node.visit(name, wrapper.value)
                is ForShort -> node.visit(name, wrapper.value)
                is ForInt -> node.visit(name, wrapper.value)
                is ForLong -> node.visit(name, wrapper.value)
                is ForFloat -> node.visit(name, wrapper.value)
                is ForDouble -> node.visit(name, wrapper.value)
                is ForClass -> node.visit(name, wrapper.value)
                is ForEnum<*> -> node.visitEnum(name, wrapper.type.descriptor, wrapper.entryName)
                is ForAnnotation -> node.visit(name, wrapper.value.toAsmNode())
                is ForArray<*> -> node.visit(name, wrapper.value.map(AnnotationElementArrayValue::getValue))
                is ForBooleanArray -> node.visit(name, wrapper.value)
                is ForCharArray -> node.visit(name, wrapper.value)
                is ForByteArray -> node.visit(name, wrapper.value)
                is ForShortArray -> node.visit(name, wrapper.value)
                is ForIntArray -> node.visit(name, wrapper.value)
                is ForLongArray -> node.visit(name, wrapper.value)
                is ForFloatArray -> node.visit(name, wrapper.value)
                is ForDoubleArray -> node.visit(name, wrapper.value)
            }
        }
        return node
    }

    public abstract fun toAsmNode(): N
}

public sealed class RootAnnotationElement<N : AnnotationNode> : AbstractAnnotationElement<N>() {
    /**
     * Whether the annotation is visible at runtime *(via reflection)*.
     *
     * This is `true` by default.
     */
    public abstract val isVisibleAtRuntime: Boolean

    /**
     * Whether multiple annotations of the same [type] are allowed on an element.
     *
     * This is `false` by default.
     */
    public abstract val allowRepeats: Boolean
}

/**
 * An annotation on an element.
 */
public data class AnnotationElement(
    override val type: ReferenceType,
    override val values: Map<String, AnnotationElementValue>,
    override val isVisibleAtRuntime: Boolean = true,
    override val allowRepeats: Boolean = false,
) : RootAnnotationElement<AnnotationNode>() {
    override fun toAsmNode(): AnnotationNode = populateAsmNode(AnnotationNode(type.descriptor))
}

/**
 * An annotation on a type.
 *
 * @property [typeRef] The reference type of the annotation.
 * @property [typePath] The type path of the annotation.
 */
public data class TypeAnnotationElement(
    public val typeRef: Int,
    public val typePath: TypePath?,
    override val type: ReferenceType,
    override val values: Map<String, AnnotationElementValue>,
    override val isVisibleAtRuntime: Boolean = true,
    override val allowRepeats: Boolean = false,
) : RootAnnotationElement<TypeAnnotationNode>() {
    override fun toAsmNode(): TypeAnnotationNode =
        populateAsmNode(TypeAnnotationNode(typeRef, typePath, type.descriptor))
}

/**
 * An annotation belonging to another annotation.
 */
public data class ChildAnnotationElement(
    override val type: ReferenceType,
    override val values: Map<String, AnnotationElementValue>,
) : AbstractAnnotationElement<AnnotationNode>() {
    override fun toAsmNode(): AnnotationNode = populateAsmNode(AnnotationNode(type.descriptor))
}