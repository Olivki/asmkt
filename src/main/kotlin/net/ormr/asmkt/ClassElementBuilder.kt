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
import net.ormr.asmkt.type.MethodType
import net.ormr.asmkt.type.ReferenceType

@AsmKtDsl
public class ClassElementBuilder(
    override val version: ClassFileVersion,
    public val kind: ClassKind,
    public val type: ReferenceType,
    override val flags: SimpleClassAccessFlags = AccessFlag.PUBLIC.asAccessFlags(),
    public val signature: String? = null,
    public val superType: ReferenceType = ReferenceType.OBJECT,
    public val interfaces: List<ReferenceType> = emptyList(),
    public val permittedSubtypes: List<ReferenceType> = emptyList(),
    public val sourceFile: String? = null,
    public val sourceDebug: String? = null,
) : ElementBuilder, Flaggable<SimpleClassAccessFlag>, VersionedElementBuilder, AnnotatableElementBuilder,
    AnnotatableElementTypeBuilder {
    /**
     * The method that the class belongs to, or `null` if the class does not belong to a method.
     */
    public var enclosingMethod: MethodElementBuilder? = null
        internal set

    /**
     * Whether `supercall` methods should be treated specially by the `invokespecial` instruction.
     *
     * If this is `true` then the [SUPER][AccessFlag.SUPER] flag is added when the class is serialized to bytecode.
     *
     * By default, this is `true`.
     */
    public var treatSuperSpecially: Boolean = true

    internal val nestMembers = mutableListOf<ClassElementBuilder>()
    internal val innerClasses = mutableListOf<InnerClassElement>()
    internal val methods = mutableSetOf<MethodElementBuilder>()
    internal val fields = mutableMapOf<String, FieldElementBuilder>()
    internal val visibleAnnotations = mutableListOf<AnnotationElement>()
    internal val invisibleAnnotations = mutableListOf<AnnotationElement>()
    internal val visibleTypeAnnotations = mutableListOf<TypeAnnotationElement>()
    internal val invisibleTypeAnnotations = mutableListOf<TypeAnnotationElement>()

    init {
        verifyState()
    }

    override fun annotation(element: AnnotationElement) {
        addAnnotation(
            element = element,
            visible = visibleAnnotations,
            invisible = invisibleAnnotations,
            isVisible = element.isVisibleAtRuntime,
            allowRepeats = element.allowRepeats,
        )
    }

    override fun typeAnnotation(element: TypeAnnotationElement) {
        addAnnotation(
            element = element,
            visible = visibleTypeAnnotations,
            invisible = invisibleTypeAnnotations,
            isVisible = element.isVisibleAtRuntime,
            allowRepeats = element.allowRepeats,
        )
    }

    public fun field(
        name: String,
        flags: FieldAccessFlags,
        type: FieldType,
        signature: String? = null,
    ): FieldElementBuilder {
        requireNotOneKindOf(ClassKind.NO_FIELDS) { "fields" }
        val builder = FieldElementBuilder(
            owner = this,
            name = name,
            flags = flags,
            type = type,
            signature = signature,
        )
        checkInterfaceField(builder)
        fields[name] = builder
        return builder
    }

    public fun field(
        name: String,
        flag: FieldAccessFlag,
        type: FieldType,
        signature: String? = null,
    ): FieldElementBuilder = field(
        name = name,
        flags = flag.asAccessFlags(),
        type = type,
        signature = signature,
    )

    public fun method(
        name: String,
        flags: MethodAccessFlags,
        type: MethodType,
        signature: String? = null,
        exceptions: List<ReferenceType> = emptyList(),
    ): MethodElementBuilder {
        requireNotOneKindOf(ClassKind.NO_METHODS) { "methods" }
        val builder = MethodElementBuilder(
            owner = this,
            name = name,
            flags = flags,
            type = type,
            signature = signature,
            exceptions = exceptions,
        )
        methods += builder
        return builder
    }

    public fun method(
        name: String,
        flag: MethodAccessFlag,
        type: MethodType,
        signature: String? = null,
        exceptions: List<ReferenceType> = emptyList(),
    ): MethodElementBuilder = method(
        name = name,
        flags = flag.asAccessFlags(),
        type = type,
        signature = signature,
        exceptions = exceptions,
    )
}