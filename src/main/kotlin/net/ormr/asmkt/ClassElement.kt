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

import net.ormr.asmkt.AccessFlag.NONE
import net.ormr.asmkt.AccessFlag.SUPER
import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.tree.ClassNode

public data class ClassElement(
    public val version: ClassFileVersion,
    public val kind: ClassKind,
    public val type: ReferenceType,
    override val flags: SimpleClassAccessFlags,
    public val signature: String?,
    public val supertype: ReferenceType,
    public val interfaces: List<ReferenceType>,
    public val sourceFile: String?,
    public val sourceDebug: String?,
    public val permittedSubtypes: List<ReferenceType>,
    public val enclosingMethod: MethodElement?,
    public val enclosingClass: ReferenceType?,
    public val innerClasses: List<InnerClassElement>,
    public val nestHost: ReferenceType?,
    public val nestMates: List<ReferenceType>,
    public val fields: Map<String, FieldElement>,
    public val methods: List<MethodElement>,
    public val treatSuperSpecially: Boolean,
    public val annotations: ElementAnnotations,
    public val typeAnnotations: ElementTypeAnnotations,
) : Flaggable<SimpleClassAccessFlag> {
    public fun toAsmNode(): ClassNode {
        val node = ClassNode()

        node.version = version.opcode
        node.access = ((kind + flags) + if (treatSuperSpecially) SUPER else NONE).asInt()
        node.name = type.internalName
        node.superName = supertype.internalName
        node.interfaces = interfaces.mapTo(mutableListOf()) { it.internalName }
        node.sourceFile = sourceFile
        node.sourceDebug = sourceDebug

        node.outerClass = enclosingClass?.internalName
        node.outerMethod = enclosingMethod?.name
        node.outerMethodDesc = enclosingMethod?.type?.descriptor

        node.nestHostClass = nestHost?.internalName
        node.nestMembers = nestMates
            .ifEmpty { null }
            ?.mapTo(mutableListOf()) { it.internalName }

        // TODO: node.module = module?.toAsmNode()

        for (method in methods) {
            node.methods.add(method.toAsmNode())
        }

        for ((_, field) in fields) {
            node.fields.add(field.toAsmNode())
        }

        // TODO: node.recordComponents = ...

        node.permittedSubclasses = permittedSubtypes
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

        return node
    }
}