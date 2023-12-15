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

public sealed class RootElementAnnotations<A : RootAnnotationElement<*>> {
    /**
     * A list of the annotations that are visible at runtime through reflection.
     */
    public abstract val visibleElements: List<A>

    /**
     * A list of the annotations that are not visible at runtime through reflection.
     */
    public abstract val invisibleElements: List<A>
}

/**
 * Represents a collection of annotations on an element.
 */
public data class ElementAnnotations(
    override val visibleElements: List<AnnotationElement>,
    override val invisibleElements: List<AnnotationElement>,
) : RootElementAnnotations<AnnotationElement>() {
    public companion object {
        @JvmField
        public val EMPTY: ElementAnnotations = ElementAnnotations(emptyList(), emptyList())
    }
}

/**
 * Represents a collection of annotations on an elements type.
 */
public data class ElementTypeAnnotations(
    override val visibleElements: List<TypeAnnotationElement>,
    override val invisibleElements: List<TypeAnnotationElement>,
) : RootElementAnnotations<TypeAnnotationElement>() {
    public companion object {
        @JvmField
        public val EMPTY: ElementTypeAnnotations = ElementTypeAnnotations(emptyList(), emptyList())
    }
}