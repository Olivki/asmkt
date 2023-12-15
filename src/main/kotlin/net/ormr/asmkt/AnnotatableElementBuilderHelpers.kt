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

internal fun <A : RootAnnotationElement<*>> addAnnotation(
    element: A,
    annotations: RootElementAnnotationsBuilder<*, A>,
    isVisible: Boolean,
    allowRepeats: Boolean,
) {
    val type = element.type
    val (visible, invisible) = annotations

    if (isVisible) {
        visible += when {
            visible.containsType(type) -> when {
                allowRepeats -> element
                else -> disallowedRepeatingAnnotation(type)
            }
            else -> element
        }
    } else {
        invisible += when {
            invisible.containsType(type) -> when {
                allowRepeats -> element
                else -> disallowedRepeatingAnnotation(type)
            }
            else -> element
        }
    }
}

internal fun disallowedRepeatingAnnotation(type: ReferenceType): Nothing =
    throw IllegalArgumentException("Element is already annotated with annotation of type (${type.asString()})")

internal fun List<AbstractAnnotationElement<*>>.containsType(type: ReferenceType): Boolean =
    any { it.type == type }