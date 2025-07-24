/*
 * Copyright 2023-2025 Oliver Berg
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

import net.ormr.asmkt.type.ReferenceType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A builder class for creating [ClassElement] instances.
 *
 * @property [version] The version of the class file.
 * @property [kind] The kind of the class.
 * @property [type] The reference type of the class.
 * @property [flags] The access flags of the class.
 * @property [signature] The *(generic)* signature of the class.
 * @property [supertype] The super type of the class.
 * @property [interfaces] The list of interface types implemented by the class.
 * @property [sourceFile] The source file of the class.
 * @property [sourceDebug] The debug information of the class.
 * @property [enclosingClass] The type of the enclosing class of the inner class, or `null` if the class is not an inner
 * class.
 * @property [nestHost] The type of the nest host of the class, or `null` if the class isn't a part of a nest.
 */
@AsmKtDsl
public class ClassElementBuilder @PublishedApi internal constructor(
    override val version: ClassFileVersion,
    public val type: ReferenceType,
    override val flags: SimpleClassAccessFlags,
    public val kind: ClassKind,
    public val signature: String?,
    public val supertype: ReferenceType,
    public val interfaces: List<ReferenceType>,
    public val sourceFile: String?,
    public val sourceDebug: String?,
    public val enclosingClass: ReferenceType?,
    public val nestHost: ReferenceType?,
) : ElementBuilder, Flaggable<SimpleClassAccessFlag>, VersionedElementBuilder, AnnotatableElementBuilder,
    AnnotatableElementTypeBuilder {
    /**
     * The method that the class belongs to, or `null` if the class does not belong to a method.
     */
    public var enclosingMethod: MethodElement? = null
        internal set

    /**
     * Whether `supercall` methods should be treated specially by the `invokespecial` instruction.
     *
     * If this is `true` then the [SUPER][AccessFlag.SUPER] flag is added when the class is serialized to bytecode.
     *
     * By default, this is `true`.
     */
    public var treatSuperSpecially: Boolean = true

    /**
     * A mutable list of permitted subtypes.
     *
     * If this list is *not* empty then the class will be marked as a `sealed` type when serialized to bytecode.
     */
    public val permittedSubtypes: MutableList<ReferenceType> = mutableListOf()

    /**
     * A mutable list of all nest members of the class.
     *
     * Note that if the elements of the list do not have their [nestHost] set to the [type] of `this` class then
     * errors will be encountered at runtime.
     */
    public val nestMates: MutableList<ReferenceType> = mutableListOf()

    /**
     * A mutable list of all inner classes of the class.
     *
     * Note that if the elements of the list do not have their [enclosingClass] set to the [type] of `this` class then
     * faulty metadata will be generated, and errors may be encountered at runtime.
     */
    public val innerClasses: MutableList<InnerClassElement> = mutableListOf()

    public val methods: MutableList<MethodElement> = mutableListOf()

    public val fields: MutableMap<String, FieldElement> = mutableMapOf()

    override val annotations: ElementAnnotationsBuilder = ElementAnnotationsBuilder()

    override val typeAnnotations: ElementTypeAnnotationsBuilder = ElementTypeAnnotationsBuilder()

    init {
        verifyState()
    }

    public fun field(field: FieldElement): FieldElement {
        requireNotOneKindOf(ClassKind.NO_FIELDS) { "fields" }
        checkInterfaceField(field)
        fields[field.name] = field
        return field
    }

    public fun method(element: MethodElement): MethodElement {
        requireNotOneKindOf(ClassKind.NO_METHODS) { "methods" }
        methods += element
        return element
    }

    @PublishedApi
    internal fun build(): ClassElement {
        verifyStateBeforeBuild()
        return ClassElement(
            version = version,
            kind = kind,
            type = type,
            flags = flags,
            signature = signature,
            supertype = supertype,
            interfaces = interfaces,
            sourceFile = sourceFile,
            sourceDebug = sourceDebug,
            permittedSubtypes = permittedSubtypes.toList(),
            enclosingMethod = enclosingMethod,
            enclosingClass = enclosingClass,
            innerClasses = innerClasses.toList(),
            nestHost = nestHost,
            nestMates = nestMates.toList(),
            fields = fields.toMap(),
            methods = methods.toList(),
            treatSuperSpecially = treatSuperSpecially,
            annotations = annotations.build(),
            typeAnnotations = typeAnnotations.build(),
        )
    }
}

@AsmKtDsl
public inline fun ClassElement(
    version: ClassFileVersion,
    type: ReferenceType,
    flags: SimpleClassAccessFlags = AccessFlag.PUBLIC.asAccessFlags(),
    kind: ClassKind = ClassKind.CLASS,
    signature: String? = null,
    supertype: ReferenceType = ReferenceType.OBJECT,
    interfaces: List<ReferenceType> = emptyList(),
    sourceFile: String? = null,
    sourceDebug: String? = null,
    enclosingClass: ReferenceType? = null,
    nestHost: ReferenceType? = null,
    builder: ClassElementBuilder.() -> Unit = {},
): ClassElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return ClassElementBuilder(
        version = version,
        type = type,
        flags = flags,
        kind = kind,
        signature = signature,
        supertype = supertype,
        interfaces = interfaces,
        sourceFile = sourceFile,
        sourceDebug = sourceDebug,
        enclosingClass = enclosingClass,
        nestHost = nestHost
    ).apply(builder).build()
}

@AsmKtDsl
public inline fun ClassElement(
    version: ClassFileVersion,
    type: ReferenceType,
    flags: SimpleClassAccessFlag,
    kind: ClassKind = ClassKind.CLASS,
    signature: String? = null,
    supertype: ReferenceType = ReferenceType.OBJECT,
    interfaces: List<ReferenceType> = emptyList(),
    sourceFile: String? = null,
    sourceDebug: String? = null,
    enclosingClass: ReferenceType? = null,
    nestHost: ReferenceType? = null,
    builder: ClassElementBuilder.() -> Unit = {},
): ClassElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return ClassElement(
        version = version,
        type = type,
        flags = flags.asAccessFlags(),
        kind = kind,
        signature = signature,
        supertype = supertype,
        interfaces = interfaces,
        sourceFile = sourceFile,
        sourceDebug = sourceDebug,
        enclosingClass = enclosingClass,
        nestHost = nestHost,
        builder = builder,
    )
}
