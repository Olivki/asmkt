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

import net.ormr.asmkt.AnnotationElementValue.*
import net.ormr.asmkt.type.toReferenceType
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

@AsmKtReflection
internal fun <A : Annotation> createChildElementAnnotation(annotation: A): ChildAnnotationElement =
    ChildAnnotationElementBuilder(annotation.annotationClass.toReferenceType())
        .populateWith(annotation)
        .build()

// here be dragons
@Suppress("UNCHECKED_CAST")
@AsmKtReflection
internal fun <B : AbstractAnnotationElementBuilder<*>, A : Annotation> B.populateWith(
    annotation: A,
): B {
    val annotationClass = annotation.annotationClass
    for (property in annotationClass.declaredMemberProperties) {
        val name = property.name
        when (val value = property.getter.call(annotation)) {
            is String -> value(name, value)
            is Boolean -> value(name, value)
            is Char -> value(name, value)
            is Byte -> value(name, value)
            is Short -> value(name, value)
            is Int -> value(name, value)
            is Long -> value(name, value)
            is Float -> value(name, value)
            is Double -> value(name, value)
            is Class<*> -> value(name, value.toReferenceType())
            is KClass<*> -> value(name, value.toReferenceType())
            is Enum<*> -> value(name, ForEnum(value.javaClass.toReferenceType(), value.name))
            is Annotation -> value(name, ForAnnotation(createChildElementAnnotation(value)))
            is BooleanArray -> value(name, value)
            is CharArray -> value(name, value)
            is ByteArray -> value(name, value)
            is ShortArray -> value(name, value)
            is IntArray -> value(name, value)
            is LongArray -> value(name, value)
            is FloatArray -> value(name, value)
            is DoubleArray -> value(name, value)
            is Array<*> -> when {
                value.isArrayOf<String>() -> value(name, value as Array<String>)
                value.isArrayOf<Annotation>() -> value(
                    name,
                    ForArray((value as Array<out Annotation>).map { ForAnnotation(createChildElementAnnotation(it)) }),
                )
                value.isArrayOf<Class<*>>() -> value(
                    name,
                    (value as Array<out Class<*>>).map { it.toReferenceType() },
                )
                value.isArrayOf<KClass<*>>() -> value(
                    name,
                    (value as Array<out KClass<*>>).map { it.toReferenceType() },
                )
                value.isArrayOf<Enum<*>>() -> value(
                    name,
                    ForArray((value as Array<out Enum<*>>).map { ForEnum(it.javaClass.toReferenceType(), it.name) }),
                )
                else -> throw IllegalArgumentException("Property '$name' has unsupported array component type: ${value::class}")
            }
            else -> throw IllegalArgumentException("Property '$name' has unsupported value: ${value?.let { "$it :: ${it::class}" } ?: "null"}")
        }
    }
    return this
}

internal fun AnnotationElementArrayValue.getValue(): Any = when (this) {
    is ForString -> value
    is ForClass -> value.asAsmType()
    is ForEnum<*> -> arrayOf(type.descriptor, entryName)
    is ForAnnotation -> value.toAsmNode()
}

internal fun AnnotationElementDefaultValue.getDefaultValue(): Any = when (this) {
    is AnnotationElementArrayValue -> (this as AnnotationElementArrayValue).getValue()
    is ForBoolean -> value
    is ForChar -> value
    is ForByte -> value
    is ForShort -> value
    is ForInt -> value
    is ForLong -> value
    is ForFloat -> value
    is ForDouble -> value
    is ForArray<*> -> value.map(AnnotationElementArrayValue::getValue)
    is ForBooleanArray -> value.toBooleanArray()
    is ForByteArray -> value.toByteArray()
    is ForCharArray -> value.toCharArray()
    is ForDoubleArray -> value.toDoubleArray()
    is ForFloatArray -> value.toFloatArray()
    is ForIntArray -> value.toIntArray()
    is ForLongArray -> value.toLongArray()
    is ForShortArray -> value.toShortArray()
}