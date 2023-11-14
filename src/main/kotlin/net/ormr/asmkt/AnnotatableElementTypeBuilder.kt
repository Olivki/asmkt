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

public sealed interface AnnotatableElementTypeBuilder : ElementBuilder {
    /**
     * Adds an annotation of type [type] to the `type` of the element.
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
     * @return a new [TypeAnnotationBuilder] instance used to build an annotation of type [type]
     *
     * @throws [IllegalArgumentException] if [allowRepeats] is `false` and the element is already annotated with an
     * annotation of the same type as [type]
     */
    public fun typeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        type: ReferenceType,
        isVisibleAtRuntime: Boolean = true,
        allowRepeats: Boolean = false,
    ): TypeAnnotationBuilder
}