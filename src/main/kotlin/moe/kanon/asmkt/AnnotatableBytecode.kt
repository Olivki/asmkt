/*
 * Copyright 2020 Oliver Berg
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

package moe.kanon.asmkt

import moe.kanon.asmkt.types.ReferenceType


/**
 * Represents a type that can be annotated with basic annotations.
 */
interface AnnotatableBytecode {
    /**
     * Annotates `this` with an annotation of the given [type].
     *
     * The value of [isVisible] determines whether or not the annotation will be visible at runtime through reflection,
     * if it's `true` then it's visible at runtime, otherwise it's not.
     *
     * If [allowRepeats] is `false` and `this` is already annotated with an annotation of the same type as `type` then
     * an [UnsupportedOperationException] will be thrown, otherwise it will just be added as another annotation to
     * `this`.
     *
     * @param [type] the type of the annotation, no checks are done to verify that the type represents an annotation
     * class
     * @param [isVisible] whether or not the annotation should be visible at runtime
     * @param [allowRepeats] whether or not multiple annotations of the same [type] are allowed on `this`
     *
     * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
     */
    @AsmKt
    fun defineAnnotation(type: ReferenceType, isVisible: Boolean = true, allowRepeats: Boolean = false): BytecodeAnnotation
}