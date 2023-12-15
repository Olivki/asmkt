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
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A builder class for creating instances of [FieldElement].
 *
 * @property [owner] The [ClassElementBuilder] that owns this field.
 * @property [name] The name of the field.
 * @property [flags] The access flags of the field.
 * @property [type] The type of the field.
 * @property [signature] The optional signature of the field.
 */
@AsmKtDsl
public class FieldElementBuilder @PublishedApi internal constructor(
    public val owner: ClassElementBuilder,
    public val name: String,
    override val flags: FieldAccessFlags,
    public val type: FieldType,
    public val signature: String?,
) : ElementBuilder, Flaggable<FieldAccessFlag>, AnnotatableElementBuilder, AnnotatableElementTypeBuilder {
    override val annotations: ElementAnnotationsBuilder = ElementAnnotationsBuilder()
    override val typeAnnotations: ElementTypeAnnotationsBuilder = ElementTypeAnnotationsBuilder()

    /**
     * The initial value of the field, or `null` if the field has no initial value.
     *
     * This must be one of the following types: `Int`, `Long`, `Float`, `Double` or `String`.
     */
    public var initialValue: Any? = null
        set(value) {
            checkInitialFieldValue(value)
            field = value
        }

    @PublishedApi
    internal fun build(): FieldElement = FieldElement(
        owner = owner.type,
        name = name,
        flags = flags,
        type = type,
        initialValue = initialValue,
        signature = signature,
        annotations = annotations.build(),
        typeAnnotations = typeAnnotations.build(),
    )
}

@AsmKtDsl
public inline fun buildFieldElement(
    owner: ClassElementBuilder,
    name: String,
    flags: FieldAccessFlags,
    type: FieldType,
    signature: String? = null,
    builder: FieldElementBuilder.() -> Unit = {},
): FieldElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return FieldElementBuilder(
        owner = owner,
        name = name,
        flags = flags,
        type = type,
        signature = signature,
    ).apply(builder).build()
}