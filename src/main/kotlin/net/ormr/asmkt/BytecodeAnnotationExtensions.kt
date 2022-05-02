/*
 * Copyright 2020-2022 Oliver Berg
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

import net.ormr.asmkt.BytecodeAnnotation.ArrayBuilder
import net.ormr.asmkt.types.ArrayType
import net.ormr.asmkt.types.ReferenceType
import org.objectweb.asm.AnnotationVisitor

/**
 * Adds an `annotation` value to `this` annotation under the given [name].
 *
 * @param [A] the type of the desired annotation
 * @param [name] the name to store the annotation value under
 *
 * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
 *
 * @see [AnnotationVisitor.visitAnnotation]
 */
@AsmKtDsl
inline fun <reified A : Annotation> BytecodeAnnotation.annotation(
    name: String,
    scope: BytecodeAnnotation.() -> Unit = {},
) {
    annotation(name, ReferenceType<A>()).apply(scope)
}

/**
 * Adds a `array` value to `this` annotation under the given [name].
 *
 * @param [name] the name to store the array value under
 *
 * @return a new [ArrayBuilder] instance used to build the array
 *
 * @see [AnnotationVisitor.visitArray]
 */
@AsmKtDsl
inline fun BytecodeAnnotation.array(name: String, scope: ArrayBuilder.() -> Unit) {
    array(name).apply(scope)
}

/**
 * Adds an `annotation` value to `this` array.
 *
 * @param [A] the type of the desired annotation
 *
 * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
 *
 * @see [AnnotationVisitor.visitAnnotation]
 */
@AsmKtDsl
inline fun <reified A : Annotation> ArrayBuilder.annotation(scope: BytecodeAnnotation.() -> Unit) {
    annotation(ReferenceType<A>()).apply(scope)
}

/**
 * Adds an `annotation` value to `this` array.
 *
 * @param [type] the type of the annotation, no checks are done to verify that the type represents an annotation
 * class
 *
 * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
 *
 * @see [AnnotationVisitor.visitAnnotation]
 */
@AsmKtDsl
inline fun ArrayBuilder.annotation(type: ReferenceType, scope: BytecodeAnnotation.() -> Unit) {
    annotation(type).apply(scope)
}

internal fun isValidAnnotationValue(value: Any): Boolean = when (value) {
    is Boolean, is Char, is String, is Byte, is Short, is Int, is Long, is Float, is Double -> true
    is ReferenceType, is ArrayType -> true
    else -> false
}

internal fun isPrimitiveArray(value: Any): Boolean = when (value) {
    is BooleanArray, is CharArray, is ByteArray, is ShortArray, is IntArray, is LongArray, is FloatArray,
    is DoubleArray,
    -> true
    else -> false
}