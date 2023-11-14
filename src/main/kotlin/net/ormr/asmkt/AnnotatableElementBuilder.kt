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

public sealed interface AnnotatableElementBuilder : ElementBuilder {
    /**
     * Adds the given annotation [element] to the element.
     *
     * Note that no validation is done to ensure that the annotation is valid for the element.
     *
     * @param [element] the annotation to add
     *
     * @throws [IllegalArgumentException] if [allowRepeats][AnnotationElement.allowRepeats] is `false` and the
     * element is already annotated with an annotation of the same type as [type][AnnotationElement.type]
     */
    public fun annotation(element: AnnotationElement)
}