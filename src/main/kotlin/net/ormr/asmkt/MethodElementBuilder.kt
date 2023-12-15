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
import net.ormr.asmkt.type.ReturnableType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents a method.
 *
 * @property [owner] The class that owns the method.
 * @property [name] The name of the method.
 * @property [flags] The access flags of the method.
 * @property [type] The type of the method.
 * @property [signature] The generic signature of the method, or `null` if the method does not have a signature.
 * @property [exceptions] The exceptions that the method can throw.
 */
@AsmKtDsl
public class MethodElementBuilder @PublishedApi internal constructor(
    public val owner: ClassElementBuilder,
    public val name: String,
    override val flags: MethodAccessFlags,
    public val type: MethodType,
    public val signature: String?,
    public val exceptions: List<ReferenceType>,
) : ElementBuilder, Flaggable<MethodAccessFlag>, VersionedElementBuilder,
    AnnotationValueConversionContext, AnnotatableElementBuilder, AnnotatableElementTypeBuilder {
    public val body: MethodBodyBuilder = MethodBodyBuilder(this)

    override val annotations: ElementAnnotationsBuilder = ElementAnnotationsBuilder()
    override val typeAnnotations: ElementTypeAnnotationsBuilder = ElementTypeAnnotationsBuilder()

    override val version: ClassFileVersion
        get() = owner.version

    /**
     * The type of the class that owns the method.
     *
     * @see [owner]
     */
    public val ownerType: ReferenceType
        get() = owner.type

    /**
     * The return type of the method.
     *
     * @see [type]
     */
    public val returnType: ReturnableType
        get() = type.returnType

    public val tryCatchBlocks: MutableList<TryCatchBlockElement> = mutableListOf()
    public val localVariables: MutableList<LocalVariableElement> = mutableListOf()
    public val parameters: MutableList<ParameterElement> = mutableListOf()

    /**
     * The default value for the annotation property, or `null` if the method is not an annotation property, or if
     * the annotation property does not have a default value.
     *
     * @throws [IllegalArgumentException] *(on set)* if the [owner] of the method is not an annotation
     */
    public var defaultAnnotationValue: AnnotationElementDefaultValue? = null
        set(value) {
            if (value != null) {
                require(owner.isAnnotation) { "Owner ($owner) of method ($this) is not an annotation" }
            }
            field = value
        }

    public fun parameter(element: ParameterElement): ParameterElement {
        parameters.add(element)
        return element
    }

    // -- SCOPING -- \\
    public inline fun withBody(block: MethodBodyBuilder.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        block(body)
    }

    public inline fun withCodeChunk(block: CodeChunkBuilder.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        block(body.codeChunk)
    }

    @PublishedApi
    internal fun build(): MethodElement = MethodElement(
        owner = owner.type,
        name = name,
        flags = flags,
        type = type,
        signature = signature,
        exceptions = exceptions,
        parameters = parameters.toList(),
        localVariables = localVariables.toList(),
        tryCatchBlocks = tryCatchBlocks.toList(),
        defaultAnnotationValue = defaultAnnotationValue,
        annotations = annotations.build(),
        typeAnnotations = typeAnnotations.build(),
        body = body.build(),
    )
}