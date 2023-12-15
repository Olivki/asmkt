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

import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.TypePath
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@AsmKtDsl
public sealed class AbstractAnnotationElementBuilder<E : AbstractAnnotationElement<*>> :
    AnnotationValueConversionContext {
    public abstract val type: ReferenceType
    protected val values: MutableMap<String, AnnotationElementValue> = mutableMapOf()


    /**
     * Adds a value with the given [name] and [value] to the annotation.
     *
     * @param [name] the name of the value
     * @param [value] the value to add
     */
    public fun value(name: String, value: AnnotationElementValue) {
        values[name] = value
    }

    @PublishedApi
    internal abstract fun build(): E
}

@AsmKtDsl
public sealed class RootAnnotationElementBuilder<E : RootAnnotationElement<*>> :
    AbstractAnnotationElementBuilder<E>() {
    public abstract val isVisibleAtRuntime: Boolean
    public abstract val allowRepeats: Boolean
}

@AsmKtDsl
public class AnnotationElementBuilder @PublishedApi internal constructor(
    override val type: ReferenceType,
    override val isVisibleAtRuntime: Boolean,
    override val allowRepeats: Boolean,
) : RootAnnotationElementBuilder<AnnotationElement>() {
    @PublishedApi
    override fun build(): AnnotationElement = AnnotationElement(
        type = type,
        values = values.toMap(),
        isVisibleAtRuntime = isVisibleAtRuntime,
        allowRepeats = allowRepeats,
    )
}

@AsmKtDsl
public inline fun buildAnnotationElement(
    type: ReferenceType,
    isVisibleAtRuntime: Boolean = true,
    allowRepeats: Boolean = false,
    builder: AnnotationElementBuilder.() -> Unit = {},
): AnnotationElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return AnnotationElementBuilder(
        type = type,
        isVisibleAtRuntime = isVisibleAtRuntime,
        allowRepeats = allowRepeats
    ).apply(builder).build()
}

@AsmKtDsl
public class TypeAnnotationElementBuilder @PublishedApi internal constructor(
    public val typeRef: Int,
    public val typePath: TypePath?,
    override val type: ReferenceType,
    override val isVisibleAtRuntime: Boolean,
    override val allowRepeats: Boolean,
) : RootAnnotationElementBuilder<TypeAnnotationElement>() {
    @PublishedApi
    override fun build(): TypeAnnotationElement = TypeAnnotationElement(
        typeRef = typeRef,
        typePath = typePath,
        type = type,
        values = values.toMap(),
        isVisibleAtRuntime = isVisibleAtRuntime,
        allowRepeats = allowRepeats,
    )
}

@AsmKtDsl
public inline fun buildTypeAnnotationElement(
    typeRef: Int,
    typePath: TypePath? = null,
    type: ReferenceType,
    isVisibleAtRuntime: Boolean = true,
    allowRepeats: Boolean = false,
    builder: TypeAnnotationElementBuilder.() -> Unit = {},
): TypeAnnotationElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return TypeAnnotationElementBuilder(
        typeRef = typeRef,
        typePath = typePath,
        type = type,
        isVisibleAtRuntime = isVisibleAtRuntime,
        allowRepeats = allowRepeats
    ).apply(builder).build()
}

@AsmKtDsl
public class ChildAnnotationElementBuilder @PublishedApi internal constructor(
    override val type: ReferenceType,
) : AbstractAnnotationElementBuilder<ChildAnnotationElement>() {
    @PublishedApi
    override fun build(): ChildAnnotationElement = ChildAnnotationElement(
        type = type,
        values = values.toMap(),
    )
}

@AsmKtDsl
public inline fun buildChildAnnotationElement(
    type: ReferenceType,
    builder: ChildAnnotationElementBuilder.() -> Unit = {},
): ChildAnnotationElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return ChildAnnotationElementBuilder(type).apply(builder).build()
}