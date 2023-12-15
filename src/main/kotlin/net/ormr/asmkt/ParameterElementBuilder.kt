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

public class ParameterElementBuilder @PublishedApi internal constructor(
    private val index: Int,
    private val name: String,
    private val flags: AccessFlags<ParameterAccessFlag>,
) : ElementBuilder, AnnotatableElementBuilder, AnnotatableElementTypeBuilder {
    override val annotations: ElementAnnotationsBuilder = ElementAnnotationsBuilder()
    override val typeAnnotations: ElementTypeAnnotationsBuilder = ElementTypeAnnotationsBuilder()

    @PublishedApi
    internal fun build(): ParameterElement = ParameterElement(
        index = index,
        name = name,
        flags = flags,
        annotations = annotations.build(),
        typeAnnotations = typeAnnotations.build(),
    )
}

@AsmKtDsl
public inline fun buildParameterElement(
    index: Int,
    name: String,
    flags: AccessFlags<ParameterAccessFlag>,
    builder: ParameterElementBuilder.() -> Unit,
): ParameterElement = ParameterElementBuilder(index, name, flags).apply(builder).build()