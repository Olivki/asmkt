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
import org.objectweb.asm.TypePath
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Annotates `this` with an annotation of type [A].
 *
 * The value of [isVisible] determines whether the annotation will be visible at runtime through reflection,
 * if it's `true` then it's visible at runtime, otherwise it's not.
 *
 * If [allowRepeats] is `false` and `this` is already annotated with an annotation of the same type as `type` then
 * an [UnsupportedOperationException] will be thrown, otherwise it will just be added as another annotation to
 * `this`.
 *
 * @param [A] the type of the desired annotation
 * @param [isVisible] whether or not the annotation should be visible at runtime
 * @param [allowRepeats] whether or not multiple annotations of the same [type] are allowed on `this`
 *
 * @return a new [BytecodeAnnotation] instance used to build an annotation of type [type]
 */
@AsmKtDsl
public inline fun <reified A : Annotation> AnnotatableBytecode.defineAnnotation(
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit = {},
): BytecodeAnnotation {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineAnnotation(ReferenceType<A>(), isVisible, allowRepeats).apply(scope)
}

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
@AsmKtDsl
public inline fun AnnotatableBytecode.defineAnnotation(
    type: ReferenceType,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit,
): BytecodeAnnotation {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineAnnotation(type, isVisible, allowRepeats).apply(scope)
}

// TODO: documentation
@AsmKtDsl
public inline fun <reified A : Annotation> AnnotatableTypeBytecode.defineTypeAnnotation(
    typeRef: Int,
    typePath: TypePath?,
    isVisible: Boolean = true,
    allowRepeats: Boolean = false,
    scope: BytecodeAnnotation.() -> Unit,
): BytecodeAnnotation {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    return defineTypeAnnotation(typeRef, typePath, ReferenceType<A>(), isVisible, allowRepeats).apply(scope)
}

internal fun handleAnnotations(
    target: Any,
    annotation: BytecodeAnnotation,
    visible: MutableList<BytecodeAnnotation>,
    invisible: MutableList<BytecodeAnnotation>,
    isVisible: Boolean,
    allowRepeats: Boolean,
): BytecodeAnnotation {
    val type = annotation.type

    if (isVisible) {
        visible += when {
            visible.containsType(type) -> when {
                allowRepeats -> annotation
                else -> disallowedRepeatingAnnotation(target, type)
            }
            else -> annotation
        }
    } else {
        invisible += when {
            invisible.containsType(type) -> when {
                allowRepeats -> annotation
                else -> disallowedRepeatingAnnotation(target, type)
            }
            else -> annotation
        }
    }

    return annotation
}

internal fun disallowedRepeatingAnnotation(target: Any, type: ReferenceType): Nothing =
    throw UnsupportedOperationException("'$target' is already annotated with an annotation of type '${type.className}'.")

internal fun List<BytecodeAnnotation>.containsType(type: ReferenceType): Boolean = any { it.type == type }