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
import net.ormr.asmkt.type.TypeWithInternalName
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// -- ANNOTATION -- \\
/**
 * Adds an `annotation` value to the annotation under the given [name].
 *
 * @param [name] the name to store the annotation value under
 * @param [element] the annotation element to store
 */
public fun AbstractAnnotationElementBuilder<*>.annotation(
    name: String,
    element: ChildAnnotationElement,
) {
    value(name, AnnotationElementValue.ForAnnotation(element))
}

/**
 * Adds the given [instance] as an `annotation` value to the annotation under the given [name].
 *
 * Note that this uses reflection to retrieve the values from [instance] and store them in the builder.
 *
 * @param [name] the name to store the annotation value under
 * @param [instance] the annotation instance to store
 *
 * @return a new [AbstractAnnotationElementBuilder] instance populated with the values from [instance]
 */
@AsmKtReflection
public fun AbstractAnnotationElementBuilder<*>.annotation(
    name: String,
    instance: Annotation,
) {
    val element = createChildElementAnnotation(instance)
    value(name, element)
}

/**
 * Adds an `annotation` value to the annotation under the given [name].
 *
 * This function is a shorthand for [annotation] that allows for a builder to be passed in.
 *
 * @param [name] the name to store the annotation value under
 * @param [type] the type of the desired annotation
 * @param [builder] the builder to use to build the annotation
 */
@AsmKtDsl
public inline fun AbstractAnnotationElementBuilder<*>.annotation(
    name: String,
    type: ReferenceType,
    builder: ChildAnnotationElementBuilder.() -> Unit,
) {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    annotation(name, ChildAnnotationElement(type, builder))
}

// -- VALUES -- \\
// --- SINGLES --- \\
@AsmKtDsl
@JvmName("stringValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: String) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("booleanValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Boolean) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("charValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Char) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("byteValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Byte) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("shortValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Short) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("intValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Int) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("longValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Long) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("floatValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Float) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("doubleValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Double) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("classValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: TypeWithInternalName) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("enumValue")
public fun <E : Enum<E>> AbstractAnnotationElementBuilder<*>.value(name: String, value: E) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("annotationValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: ChildAnnotationElement) {
    value(name, value.asValue())
}

// --- ARRAYS --- \\
@AsmKtDsl
@JvmName("stringArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Array<String>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("stringIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<String>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("booleanArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: BooleanArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("booleanIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Boolean>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("charArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: CharArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("charIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Char>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("byteArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: ByteArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("byteIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Byte>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("shortArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: ShortArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("shortIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Short>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("intArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: IntArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("intIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Int>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("longArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: LongArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("longIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Long>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("floatArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: FloatArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("floatIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Float>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("doubleArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: DoubleArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("doubleIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<Double>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("classArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Array<TypeWithInternalName>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("classIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<TypeWithInternalName>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("enumArrayValue")
public fun <E : Enum<E>> AbstractAnnotationElementBuilder<*>.value(name: String, value: Array<E>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("enumIterableValue")
public fun <E : Enum<E>> AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<E>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("annotationArrayValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Array<ChildAnnotationElement>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("annotationIterableValue")
public fun AbstractAnnotationElementBuilder<*>.value(name: String, value: Iterable<ChildAnnotationElement>) {
    value(name, value.toArrayValue())
}