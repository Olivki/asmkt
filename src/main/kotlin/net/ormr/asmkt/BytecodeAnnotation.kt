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

import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.Type
import net.ormr.asmkt.types.toReferenceType
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.TypeAnnotationNode

/**
 * Represents an [Annotation] in bytecode form.
 *
 * This class serves as *fluent* builder to create annotations.
 *
 * @property [type] The type of `this` annotation.
 */
@AsmKt
class BytecodeAnnotation internal constructor(
    val type: ReferenceType,
    @get:JvmSynthetic
    internal val node: AnnotationNode = AnnotationNode(type.descriptor)
) {
    @JvmSynthetic
    internal fun asTypeNode(): TypeAnnotationNode = node as TypeAnnotationNode

    /**
     * Adds the given [value] to `this` annotation under the given [name].
     *
     * @param [name] the name to store [value] under
     * @param [value] the actual value, allowed types are; [Boolean], [BooleanArray], [Char], [CharArray], [String],
     * [Byte], [ByteArray], [Short], [ShortArray], [Int], [IntArray], [Long], [LongArray], [Float], [FloatArray],
     * [Double], [DoubleArray], and a [Type] of sort [OBJECT][Type.OBJECT] or [ARRAY][Type.ARRAY].
     *
     * @return `this` *(for chaining)*
     *
     * @throws [IllegalArgumentException] if [value] is not one of the allowed types
     *
     * @see [AnnotationVisitor.visit]
     */
    @AsmKt
    @JvmName("addValue")
    fun value(name: String, value: Any): BytecodeAnnotation = apply {
        require(isValidAnnotationValue(value) || isPrimitiveArray(value)) { "'value' is not a valid annotation value, '$value' (${value.javaClass.name})." }
        node.visit(name, value)
    }

    /**
     * Adds the given enum [value] to `this` annotation under the given [name].
     *
     * @param [name] the name to store [value] under
     * @param [value] the actual enum entry
     *
     * @return `this` *(for chaining)*
     *
     * @see [AnnotationVisitor.visitEnum]
     */
    @AsmKt
    @JvmName("addEnum")
    fun enum(name: String, value: Enum<*>): BytecodeAnnotation = enum(name, value.javaClass.toReferenceType(), value.name)

    /**
     * Adds an `enum` value to `this` annotation under the given [name].
     *
     * @param [name] the name to store the enum value under
     * @param [type] the type representing the enum class, no checks are done to verify if this is an actual enum class
     * @param [entryName] the [name][Enum.name] of the enum entry to add to `this` annotation
     *
     * @return `this` *(for chaining)*
     *
     * @see [AnnotationVisitor.visitEnum]
     */
    @AsmKt
    @JvmName("addEnum")
    fun enum(name: String, type: Type, entryName: String): BytecodeAnnotation = apply {
        node.visitEnum(name, type.descriptor, entryName)
    }

    /**
     * Adds an `annotation` value to `this` annotation under the given [name].
     *
     * @param [name] the name to store the annotation value under
     * @param [type] the type of the annotation, no checks are done to verify that the type represents an annotation
     * class
     *
     * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
     *
     * @see [AnnotationVisitor.visitAnnotation]
     */
    @AsmKt
    @JvmName("addAnnotation")
    fun annotation(name: String, type: ReferenceType): BytecodeAnnotation =
        BytecodeAnnotation(type, node.visitAnnotation(name, type.descriptor) as AnnotationNode)

    /**
     * Adds a `array` value to `this` annotation under the given [name].
     *
     * If the goal is to just add values of a primitive nature *(`byte`, `short`, `int`, etc..)* then the [value]
     * function also accepts normal primitive arrays.
     *
     * @param [name] the name to store the array value under
     *
     * @return a new [ArrayBuilder] instance used to build the array
     *
     * @see [AnnotationVisitor.visitArray]
     */
    @AsmKt
    @JvmName("addArray")
    fun array(name: String): ArrayBuilder = ArrayBuilder(name, node.visitArray(name) as AnnotationNode)

    /**
     * Adds a `array` value to `this` annotation under the given [name] with some [default values][values] already
     * added.
     *
     * If the goal is to just add values of a primitive nature *(`byte`, `short`, `int`, etc..)* then the [value]
     * function also accepts normal primitive arrays.
     *
     * @param [name] the name to store the array value under
     * @param [values] the values to add to the [ArrayBuilder] before returning it, allowed types are; [Boolean],
     * [Char], [String], [Byte], [Short], [Int], [Long], [Float], [Double], [Enum], and a [Type] of sort
     * [OBJECT][Type.OBJECT] or [ARRAY][Type.ARRAY].
     *
     * @return a new [ArrayBuilder] instance used to build the array
     *
     * @throws [IllegalArgumentException] if [values] contains a non-allowed type
     *
     * @see [AnnotationVisitor.visitArray]
     */
    @AsmKt
    @JvmName("addArray")
    fun array(name: String, values: Iterable<Any>): ArrayBuilder {
        val builder = array(name)

        for (value in values) {
            require(isValidAnnotationValue(value) || value is Enum<*>) { "'value' is not a valid annotation value, '$value' (${value.javaClass.name})." }
            when (value) {
                is Enum<*> -> builder.enum(value)
                else -> builder.value(value)
            }
        }

        return builder
    }

    /**
     * Adds a `array` value to `this` annotation under the given [name] with some [default values][values] already
     * added.
     *
     * If the goal is to just add values of a primitive nature *(`byte`, `short`, `int`, etc..)* then the [value]
     * function also accepts normal primitive arrays.
     *
     * @param [name] the name to store the array value under
     * @param [values] the values to add to the [ArrayBuilder] before returning it, allowed types are; [Boolean],
     * [Char], [String], [Byte], [Short], [Int], [Long], [Float], [Double], [Enum], and a [Type] of sort
     * [OBJECT][Type.OBJECT] or [ARRAY][Type.ARRAY].
     *
     * @return a new [ArrayBuilder] instance used to build the array
     *
     * @throws [IllegalArgumentException] if [values] contains a non-allowed type
     *
     * @see [AnnotationVisitor.visitArray]
     */
    @AsmKt
    @JvmName("addArray")
    fun array(name: String, vararg values: Any): ArrayBuilder = array(name, values.asIterable())

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BytecodeAnnotation -> false
        type != other.type -> false
        else -> true
    }

    override fun hashCode(): Int = type.hashCode()

    override fun toString(): String = "BytecodeAnnotation(type='${type.className}')"

    @JvmSynthetic
    operator fun component1(): Type = type

    /**
     * A builder used to construct arrays of values for a [BytecodeAnnotation].
     */
    @AsmKt
    class ArrayBuilder internal constructor(val name: String, private val node: AnnotationNode) {
        /**
         * Adds the given [value] to `this` array.
         *
         * @param [value] the actual value, allowed types are; [Boolean], [Char], [String], [Byte], [Short], [Int],
         * [Long], [Float], [Double], [ReferenceType] and [ArrayType].
         *
         * @return `this` *(for chaining)*
         *
         * @throws [IllegalArgumentException] if [value] is not one of the allowed types
         *
         * @see [AnnotationVisitor.visit]
         */
        @AsmKt
        @JvmName("addValue")
        fun value(value: Any): ArrayBuilder = apply {
            require(isValidAnnotationValue(value)) { "'value' is not a valid annotation value, '$value' (${value.javaClass.name})." }
            node.visit(name, value)
        }

        /**
         * Adds the given enum [value] to `this` array.
         *
         * @param [value] the actual enum entry
         *
         * @return `this` *(for chaining)*
         *
         * @see [AnnotationVisitor.visitEnum]
         */
        @AsmKt
        @JvmName("addEnum")
        fun enum(value: Enum<*>): ArrayBuilder = apply {
            node.visitEnum(name, value.javaClass.toReferenceType().descriptor, value.name)
        }

        /**
         * Adds an `enum` value to `this` array.
         *
         * @param [type] the type representing the enum class, no checks are done to verify if this is an actual enum class
         * @param [entryName] the [name][Enum.name] of the enum entry to add to `this` annotation
         *
         * @return `this` *(for chaining)*
         *
         * @see [AnnotationVisitor.visitEnum]
         */
        @AsmKt
        @JvmName("addEnum")
        fun enum(name: String, type: Type, entryName: String): ArrayBuilder = apply {
            node.visitEnum(name, type.descriptor, entryName)
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
        @AsmKt
        @JvmName("addAnnotation")
        fun annotation(type: ReferenceType): BytecodeAnnotation =
            BytecodeAnnotation(type, node.visitAnnotation(name, type.descriptor) as AnnotationNode)
    }
}