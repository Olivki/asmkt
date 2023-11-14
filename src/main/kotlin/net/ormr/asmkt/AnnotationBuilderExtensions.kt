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
import kotlin.experimental.ExperimentalTypeInference

// TODO: add support for adding arrays/iterables of annotations

// -- ANNOTATION -- \\
/**
 * Adds an `annotation` value to `this` annotation under the given [name].
 *
 * @param [name] the name to store the annotation value under
 * @param [type] the type of the desired annotation
 *
 * @return a new [AnnotationBuilder] instance used to build an annotation of type [type]
 */
public fun AnnotationBuilder<*>.annotation(name: String, type: ReferenceType): ElementAnnotationBuilder {
    val builder = ElementAnnotationBuilder(type)
    value(name, AnnotationValue.ForBuilder(builder))
    return builder
}

/**
 * Adds the given [instance] as an `annotation` value to `this` annotation under the given [name].
 *
 * Note that this uses reflection to retrieve the values from [instance] and store them in the builder.
 *
 * @param [name] the name to store the annotation value under
 * @param [instance] the annotation instance to store
 *
 * @return a new [AnnotationBuilder] instance populated with the values from [instance]
 */
@AsmKtReflection
public fun AnnotationBuilder<*>.annotation(name: String, instance: Annotation): ElementAnnotationBuilder {
    val builder = createElementAnnotationBuilder(instance)
    value(name, AnnotationValue.ForBuilder(builder))
    return builder
}

/**
 * Adds an `annotation` value to `this` annotation under the given [name].
 *
 * This function is a shorthand for [annotation] that allows for a builder to be passed in.
 *
 * @param [name] the name to store the annotation value under
 * @param [type] the type of the desired annotation
 * @param [builder] the builder to use to build the annotation
 *
 * @return a new [AnnotationBuilder] instance used to build an annotation of type [type]
 */
@AsmKtDsl
public inline fun AnnotationBuilder<*>.annotation(
    name: String,
    type: ReferenceType,
    builder: ElementAnnotationBuilder.() -> Unit,
): ElementAnnotationBuilder {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return annotation(name, type).apply(builder)
}

// -- ARRAYS -- \\
/**
 * Adds a `array` value to `this` annotation under the given [name].
 *
 * @param [name] the name to store the array value under
 *
 * @return a new [AnnotationArrayBuilder] instance used to build the array
 */
public fun <T : AnnotationArrayValue> AnnotationBuilder<*>.array(name: String): AnnotationArrayBuilder<T> {
    val builder = AnnotationArrayBuilder<T>(name)
    value(name, AnnotationValue.ForArrayBuilder(builder))
    return builder
}

/**
 * Adds a `array` value to `this` annotation under the given [name].
 *
 * This function is a shorthand for [array] that allows for a builder to be passed in.
 *
 * @param [name] the name to store the array value under
 * @param [builder] the builder to use to build the array
 *
 * @return a new [AnnotationArrayBuilder] instance used to build the array
 */
@AsmKtDsl
@OptIn(ExperimentalTypeInference::class)
public inline fun <T : AnnotationArrayValue> AnnotationBuilder<*>.array(
    name: String,
    @BuilderInference builder: AnnotationArrayBuilder<T>.() -> Unit,
): AnnotationArrayBuilder<T> {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return array<T>(name).apply(builder)
}

// -- VALUES -- \\
// --- SINGLES --- \\
@AsmKtDsl
@JvmName("stringValue")
public fun AnnotationBuilder<*>.value(name: String, value: String) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("booleanValue")
public fun AnnotationBuilder<*>.value(name: String, value: Boolean) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("charValue")
public fun AnnotationBuilder<*>.value(name: String, value: Char) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("byteValue")
public fun AnnotationBuilder<*>.value(name: String, value: Byte) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("shortValue")
public fun AnnotationBuilder<*>.value(name: String, value: Short) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("intValue")
public fun AnnotationBuilder<*>.value(name: String, value: Int) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("longValue")
public fun AnnotationBuilder<*>.value(name: String, value: Long) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("floatValue")
public fun AnnotationBuilder<*>.value(name: String, value: Float) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("doubleValue")
public fun AnnotationBuilder<*>.value(name: String, value: Double) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("classValue")
public fun AnnotationBuilder<*>.value(name: String, value: TypeWithInternalName) {
    value(name, value.asValue())
}

@AsmKtDsl
@JvmName("enumValue")
public fun <E : Enum<E>> AnnotationBuilder<*>.value(name: String, value: E) {
    value(name, value.asValue())
}

// --- ARRAYS --- \\
@AsmKtDsl
@JvmName("stringArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: Array<String>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("stringIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<String>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("booleanArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: BooleanArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("booleanIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Boolean>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("charArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: CharArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("charIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Char>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("byteArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: ByteArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("byteIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Byte>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("shortArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: ShortArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("shortIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Short>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("intArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: IntArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("intIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Int>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("longArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: LongArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("longIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Long>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("floatArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: FloatArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("floatIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Float>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("doubleArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: DoubleArray) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("doubleIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<Double>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("classArrayValue")
public fun AnnotationBuilder<*>.value(name: String, value: Array<TypeWithInternalName>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("classIterableValue")
public fun AnnotationBuilder<*>.value(name: String, value: Iterable<TypeWithInternalName>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("enumArrayValue")
public fun <E : Enum<E>> AnnotationBuilder<*>.value(name: String, value: Array<E>) {
    value(name, value.toArrayValue())
}

@AsmKtDsl
@JvmName("enumIterableValue")
public fun <E : Enum<E>> AnnotationBuilder<*>.value(name: String, value: Iterable<E>) {
    value(name, value.toArrayValue())
}