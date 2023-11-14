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
import net.ormr.asmkt.type.toReferenceType
import org.objectweb.asm.TypePath
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Adds an annotation of type [type] to the `type` of the element.
 *
 * This function is a shorthand for [annotation][AnnotatableElementTypeBuilder.typeAnnotation] that allows for a builder to
 * be passed in.
 *
 * Note that no validation is done to ensure that the annotation is valid for the element, nor that the `type` is
 * a valid annotation type.
 *
 * @param [typeRef] the reference type of the annotation
 * @param [typePath] the type path of the annotation
 * @param [type] the type of the desired annotation
 * @param [isVisibleAtRuntime] whether or not the annotation should be visible at runtime *(via reflection)*
 * @param [allowRepeats] whether or not multiple annotations of the same [type] are allowed on the element
 *
 * @return a new [ElementTypeAnnotationBuilder] instance used to build an annotation of type [type]
 *
 * @throws [IllegalArgumentException] if [allowRepeats] is `false` and the element is already annotated with an
 * annotation of the same type as [type]
 */
@AsmKtDsl
public inline fun AnnotatableElementTypeBuilder.typeAnnotation(
    typeRef: Int,
    typePath: TypePath?,
    type: ReferenceType,
    isVisibleAtRuntime: Boolean = true,
    allowRepeats: Boolean = false,
    builder: ElementTypeAnnotationBuilder.() -> Unit = {},
): ElementTypeAnnotationBuilder {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return typeAnnotation(typeRef, typePath, type, isVisibleAtRuntime, allowRepeats).apply(builder)
}

/**
 * Adds the annotation [instance] to the `type` of the element.
 *
 * This uses reflection to retrieve the values from [instance] and store them in the builder.
 *
 * Note that no validation is done to ensure that the annotation is valid for the element.
 *
 * @param [typeRef] the reference type of the annotation
 * @param [typePath] the type path of the annotation
 * @param [instance] the annotation instance to add
 * @param [isVisibleAtRuntime] whether or not the annotation should be visible at runtime *(via reflection)*
 * @param [allowRepeats] whether or not multiple annotations of the same [instance] are allowed on the element
 *
 * @return a new [ElementTypeAnnotationBuilder] instance used to build an annotation of type [instance]
 *
 * @throws [IllegalArgumentException] if [allowRepeats] is `false` and the element is already annotated with an
 * annotation of the same type as [instance]
 */
@AsmKtDsl
@AsmKtReflection
public fun AnnotatableElementTypeBuilder.typeAnnotation(
    typeRef: Int,
    typePath: TypePath?,
    instance: Annotation,
    isVisibleAtRuntime: Boolean = true,
    allowRepeats: Boolean = false,
): ElementTypeAnnotationBuilder {
    val type = instance.annotationClass.toReferenceType()
    val builder = typeAnnotation(
        typeRef = typeRef,
        typePath = typePath,
        type = type,
        isVisibleAtRuntime = isVisibleAtRuntime,
        allowRepeats = allowRepeats,
    )
    return populateBuilderWith(builder, instance)
}