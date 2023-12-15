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

public sealed class RootElementAnnotationsBuilder<T : RootElementAnnotations<A>, A : RootAnnotationElement<*>> {
    public val visibleElements: MutableList<A> = mutableListOf()
    public val invisibleElements: MutableList<A> = mutableListOf()

    @PublishedApi
    internal abstract fun build(): T

    public operator fun component1(): MutableList<A> = visibleElements

    public operator fun component2(): MutableList<A> = invisibleElements
}

public class ElementAnnotationsBuilder @PublishedApi internal constructor() :
    RootElementAnnotationsBuilder<ElementAnnotations, AnnotationElement>() {
    @PublishedApi
    override fun build(): ElementAnnotations = when {
        visibleElements.isEmpty() && invisibleElements.isEmpty() -> ElementAnnotations.EMPTY
        else -> ElementAnnotations(
            visibleElements = visibleElements.toList(),
            invisibleElements = invisibleElements.toList(),
        )
    }
}

public class ElementTypeAnnotationsBuilder @PublishedApi internal constructor() :
    RootElementAnnotationsBuilder<ElementTypeAnnotations, TypeAnnotationElement>() {
    @PublishedApi
    override fun build(): ElementTypeAnnotations = when {
        visibleElements.isEmpty() && invisibleElements.isEmpty() -> ElementTypeAnnotations.EMPTY
        else -> ElementTypeAnnotations(
            visibleElements = visibleElements.toList(),
            invisibleElements = invisibleElements.toList(),
        )
    }
}