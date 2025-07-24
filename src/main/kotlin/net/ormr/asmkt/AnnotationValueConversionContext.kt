/*
 * Copyright 2023-2025 Oliver Berg
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

import net.ormr.asmkt.AnnotationElementValue.*
import net.ormr.asmkt.type.TypeWithInternalName
import net.ormr.asmkt.type.toReferenceType

/**
 * Marker interface for exposing conversion functions for annotation values.
 */
@AsmKtDsl
public sealed interface AnnotationValueConversionContext

// -- SINGLES -- \\
context(_: AnnotationValueConversionContext)
public fun String.asValue(): ForString = ForString(this)

context(_: AnnotationValueConversionContext)
public fun Boolean.asValue(): ForBoolean = ForBoolean(this)

context(_: AnnotationValueConversionContext)
public fun Char.asValue(): ForChar = ForChar(this)

context(_: AnnotationValueConversionContext)
public fun Byte.asValue(): ForByte = ForByte(this)

context(_: AnnotationValueConversionContext)
public fun Short.asValue(): ForShort = ForShort(this)

context(_: AnnotationValueConversionContext)
public fun Int.asValue(): ForInt = ForInt(this)

context(_: AnnotationValueConversionContext)
public fun Long.asValue(): ForLong = ForLong(this)

context(_: AnnotationValueConversionContext)
public fun Float.asValue(): ForFloat = ForFloat(this)

context(_: AnnotationValueConversionContext)
public fun Double.asValue(): ForDouble = ForDouble(this)

context(_: AnnotationValueConversionContext)
public fun TypeWithInternalName.asValue(): ForClass = ForClass(this)

context(_: AnnotationValueConversionContext)
public fun <E : Enum<E>> E.asValue(): ForEnum<E> = ForEnum(type = javaClass.toReferenceType(), entryName = name)

context(_: AnnotationValueConversionContext)
public fun ChildAnnotationElement.asValue(): ForAnnotation = ForAnnotation(this)

// -- ARRAYS -- \\
context(_: AnnotationValueConversionContext)
@JvmName("stringArrayToArrayValue")
public fun Array<String>.toArrayValue(): ForArray<ForString> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("stringIterableToArrayValue")
public fun Iterable<String>.toArrayValue(): ForArray<ForString> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("booleanArrayToArrayValue")
public fun BooleanArray.toArrayValue(): ForBooleanArray = ForBooleanArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("booleanIterableToArrayValue")
public fun Iterable<Boolean>.toArrayValue(): ForBooleanArray = ForBooleanArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("charArrayToArrayValue")
public fun CharArray.toArrayValue(): ForCharArray = ForCharArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("charIterableToArrayValue")
public fun Iterable<Char>.toArrayValue(): ForCharArray = ForCharArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("byteArrayToArrayValue")
public fun ByteArray.toArrayValue(): ForByteArray = ForByteArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("byteIterableToArrayValue")
public fun Iterable<Byte>.toArrayValue(): ForByteArray = ForByteArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("shortArrayToArrayValue")
public fun ShortArray.toArrayValue(): ForShortArray = ForShortArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("shortIterableToArrayValue")
public fun Iterable<Short>.toArrayValue(): ForShortArray = ForShortArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("intArrayToArrayValue")
public fun IntArray.toArrayValue(): ForIntArray = ForIntArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("intIterableToArrayValue")
public fun Iterable<Int>.toArrayValue(): ForIntArray = ForIntArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("longArrayToArrayValue")
public fun LongArray.toArrayValue(): ForLongArray = ForLongArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("longIterableToArrayValue")
public fun Iterable<Long>.toArrayValue(): ForLongArray = ForLongArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("floatArrayToArrayValue")
public fun FloatArray.toArrayValue(): ForFloatArray = ForFloatArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("floatIterableToArrayValue")
public fun Iterable<Float>.toArrayValue(): ForFloatArray = ForFloatArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("doubleArrayToArrayValue")
public fun DoubleArray.toArrayValue(): ForDoubleArray = ForDoubleArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("doubleIterableToArrayValue")
public fun Iterable<Double>.toArrayValue(): ForDoubleArray = ForDoubleArray(map { it })

context(_: AnnotationValueConversionContext)
@JvmName("classArrayToArrayValue")
public fun Array<TypeWithInternalName>.toArrayValue(): ForArray<ForClass> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("classIterableToArrayValue")
public fun Iterable<TypeWithInternalName>.toArrayValue(): ForArray<ForClass> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("enumArrayToArrayValue")
public fun <E : Enum<E>> Array<E>.toArrayValue(): ForArray<ForEnum<E>> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("enumIterableToArrayValue")
public fun <E : Enum<E>> Iterable<E>.toArrayValue(): ForArray<ForEnum<E>> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("annotationArrayToArrayValue")
public fun Array<ChildAnnotationElement>.toArrayValue(): ForArray<ForAnnotation> = ForArray(map { it.asValue() })

context(_: AnnotationValueConversionContext)
@JvmName("annotationIterableToArrayValue")
public fun Iterable<ChildAnnotationElement>.toArrayValue(): ForArray<ForAnnotation> = ForArray(map { it.asValue() })