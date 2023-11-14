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

import net.ormr.asmkt.AnnotationValue.*
import net.ormr.asmkt.type.TypeWithInternalName
import net.ormr.asmkt.type.toReferenceType

/**
 * Marker interface for exposing conversion functions for annotation values.
 */
@AsmKtDsl
public sealed interface AnnotationValueConversionContext

// -- SINGLES -- \\
context(AnnotationValueConversionContext)
public fun String.asValue(): ForString = ForString(this)

context(AnnotationValueConversionContext)
public fun Boolean.asValue(): ForBoolean = ForBoolean(this)

context(AnnotationValueConversionContext)
public fun Char.asValue(): ForChar = ForChar(this)

context(AnnotationValueConversionContext)
public fun Byte.asValue(): ForByte = ForByte(this)

context(AnnotationValueConversionContext)
public fun Short.asValue(): ForShort = ForShort(this)

context(AnnotationValueConversionContext)
public fun Int.asValue(): ForInt = ForInt(this)

context(AnnotationValueConversionContext)
public fun Long.asValue(): ForLong = ForLong(this)

context(AnnotationValueConversionContext)
public fun Float.asValue(): ForFloat = ForFloat(this)

context(AnnotationValueConversionContext)
public fun Double.asValue(): ForDouble = ForDouble(this)

context(AnnotationValueConversionContext)
public fun TypeWithInternalName.asValue(): ForClass = ForClass(this)

context(AnnotationValueConversionContext)
public fun <E : Enum<E>> E.asValue(): ForEnum<E> = ForEnum(type = javaClass.toReferenceType(), entryName = name)

// -- ARRAYS -- \\
context(AnnotationValueConversionContext)
@JvmName("stringArrayToArrayValue")
public fun Array<String>.toArrayValue(): ForArray<ForString> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("stringIterableToArrayValue")
public fun Iterable<String>.toArrayValue(): ForArray<ForString> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("booleanArrayToArrayValue")
public fun BooleanArray.toArrayValue(): ForArray<ForBoolean> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("booleanIterableToArrayValue")
public fun Iterable<Boolean>.toArrayValue(): ForArray<ForBoolean> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("charArrayToArrayValue")
public fun CharArray.toArrayValue(): ForArray<ForChar> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("charIterableToArrayValue")
public fun Iterable<Char>.toArrayValue(): ForArray<ForChar> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("byteArrayToArrayValue")
public fun ByteArray.toArrayValue(): ForArray<ForByte> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("byteIterableToArrayValue")
public fun Iterable<Byte>.toArrayValue(): ForArray<ForByte> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("shortArrayToArrayValue")
public fun ShortArray.toArrayValue(): ForArray<ForShort> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("shortIterableToArrayValue")
public fun Iterable<Short>.toArrayValue(): ForArray<ForShort> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("intArrayToArrayValue")
public fun IntArray.toArrayValue(): ForArray<ForInt> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("intIterableToArrayValue")
public fun Iterable<Int>.toArrayValue(): ForArray<ForInt> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("longArrayToArrayValue")
public fun LongArray.toArrayValue(): ForArray<ForLong> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("longIterableToArrayValue")
public fun Iterable<Long>.toArrayValue(): ForArray<ForLong> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("floatArrayToArrayValue")
public fun FloatArray.toArrayValue(): ForArray<ForFloat> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("floatIterableToArrayValue")
public fun Iterable<Float>.toArrayValue(): ForArray<ForFloat> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("doubleArrayToArrayValue")
public fun DoubleArray.toArrayValue(): ForArray<ForDouble> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("doubleIterableToArrayValue")
public fun Iterable<Double>.toArrayValue(): ForArray<ForDouble> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("classArrayToArrayValue")
public fun Array<TypeWithInternalName>.toArrayValue(): ForArray<ForClass> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("classIterableToArrayValue")
public fun Iterable<TypeWithInternalName>.toArrayValue(): ForArray<ForClass> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("enumArrayToArrayValue")
public fun <E : Enum<E>> Array<E>.toArrayValue(): ForArray<ForEnum<E>> = ForArray(map { it.asValue() })

context(AnnotationValueConversionContext)
@JvmName("enumIterableToArrayValue")
public fun <E : Enum<E>> Iterable<E>.toArrayValue(): ForArray<ForEnum<E>> = ForArray(map { it.asValue() })