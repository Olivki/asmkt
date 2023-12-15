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
import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.tree.FieldNode

/**
 * Represents a field element belonging to a [ClassElement].
 *
 * @property [owner] The reference type that owns this field.
 * @property [name] The name of the field.
 * @property [flags] The access flags of the field.
 * @property [type] The type of the field.
 * @property [initialValue] The initial value of the field, or `null` if the field has no initial value.
 *
 * This must be one of the following types: `Int`, `Long`, `Float`, `Double` or `String`.
 * @property [signature] The optional signature of the field.
 * @property [annotations] The annotations associated with the field.
 * @property [typeAnnotations] The annotations associated with the field's type.
 *
 * @constructor Creates a new instance of [FieldElement].
 */
public data class FieldElement(
    public val owner: ReferenceType,
    public val name: String,
    override val flags: FieldAccessFlags,
    public val type: FieldType,
    public val initialValue: Any?,
    public val signature: String?,
    public val annotations: ElementAnnotations,
    public val typeAnnotations: ElementTypeAnnotations,
) : Flaggable<FieldAccessFlag> {
    init {
        checkInitialFieldValue(initialValue)
    }

    public fun toAsmNode(): FieldNode {
        val node = FieldNode(flags.asInt(), name, type.descriptor, signature, initialValue)

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

        return node
    }
}