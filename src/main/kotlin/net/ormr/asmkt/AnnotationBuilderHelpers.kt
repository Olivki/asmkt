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
import net.ormr.asmkt.type.toReferenceType
import org.objectweb.asm.TypePath
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

@AsmKtReflection
internal fun <A : Annotation> createTypeAnnotationBuilder(
    typeRef: Int,
    typePath: TypePath?,
    annotation: A,
): ElementTypeAnnotationBuilder = populateBuilderWith(
    ElementTypeAnnotationBuilder(
        typeRef = typeRef,
        typePath = typePath,
        type = annotation::class.toReferenceType(),
    ),
    annotation,
)

@AsmKtReflection
internal fun <A : Annotation> createElementAnnotationBuilder(annotation: A): ElementAnnotationBuilder =
    populateBuilderWith(ElementAnnotationBuilder(annotation::class.toReferenceType()), annotation)

// here be dragons
@Suppress("UNCHECKED_CAST")
@AsmKtReflection
internal fun <B : AnnotationBuilder<*>, A : Annotation> populateBuilderWith(builder: B, annotation: A): B {
    val annotationClass = annotation.annotationClass
    for (property in annotationClass.declaredMemberProperties) {
        val name = property.name
        when (val value = property.getter.call(annotation)) {
            is String -> builder.value(name, value)
            is Boolean -> builder.value(name, value)
            is Char -> builder.value(name, value)
            is Byte -> builder.value(name, value)
            is Short -> builder.value(name, value)
            is Int -> builder.value(name, value)
            is Long -> builder.value(name, value)
            is Float -> builder.value(name, value)
            is Double -> builder.value(name, value)
            is Class<*> -> builder.value(name, value.toReferenceType())
            is KClass<*> -> builder.value(name, value.toReferenceType())
            is Enum<*> -> builder.value(name, ForEnum(value.javaClass.toReferenceType(), value.name))
            is Annotation -> builder.value(name, ForBuilder(createElementAnnotationBuilder(value)))
            is BooleanArray -> builder.value(name, value)
            is CharArray -> builder.value(name, value)
            is ByteArray -> builder.value(name, value)
            is ShortArray -> builder.value(name, value)
            is IntArray -> builder.value(name, value)
            is LongArray -> builder.value(name, value)
            is FloatArray -> builder.value(name, value)
            is DoubleArray -> builder.value(name, value)
            is Array<*> -> when {
                value.isArrayOf<String>() -> builder.value(name, value as Array<String>)
                value.isArrayOf<Annotation>() -> builder.value(
                    name,
                    ForArray((value as Array<out Annotation>).map { ForBuilder(createElementAnnotationBuilder(it)) }),
                )
                value.isArrayOf<Class<*>>() -> builder.value(
                    name,
                    (value as Array<out Class<*>>).map { it.toReferenceType() },
                )
                value.isArrayOf<KClass<*>>() -> builder.value(
                    name,
                    (value as Array<out KClass<*>>).map { it.toReferenceType() },
                )
                value.isArrayOf<Enum<*>>() -> builder.value(
                    name,
                    ForArray((value as Array<out Enum<*>>).map { ForEnum(it.javaClass.toReferenceType(), it.name) }),
                )
                else -> throw IllegalArgumentException("Property '$name' has unsupported array component type: ${value::class}")
            }
            else -> throw IllegalArgumentException("Property '$name' has unsupported value: ${value?.let { "$it :: ${it::class}" } ?: "null"}")
        }
    }
    return builder
}

internal fun AnnotationArrayValue.getValue(): Any = when (this) {
    is ForString -> value
    is ForBoolean -> value
    is ForChar -> value
    is ForByte -> value
    is ForShort -> value
    is ForInt -> value
    is ForLong -> value
    is ForFloat -> value
    is ForDouble -> value
    is ForClass -> value.asAsmType()
    is ForEnum<*> -> arrayOf(type.descriptor, entryName)
    is ForBuilder -> value.buildNode()
}

internal fun AnnotationDefaultValue.getDefaultValue(): Any = when (this) {
    is AnnotationArrayValue -> (this as AnnotationArrayValue).getValue()
    is ForArray<*> -> value.map(AnnotationArrayValue::getValue)
}