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

import net.ormr.asmkt.type.Type
import org.objectweb.asm.Label
import org.objectweb.asm.tree.LabelNode

internal fun Any?.isValidInitialFieldValue(): Boolean = when (this) {
    null, is Int, is Long, is Float, is Double, is String -> true
    else -> false
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun checkInitialFieldValue(value: Any?) {
    require(value.isValidInitialFieldValue()) {
        val name = value?.let { "$it :: ${it::class}" } ?: "null"
        "Value ($name) is not a valid initial field value"
    }
}

internal fun Any.convertToAsmConstant(): Any = when (this) {
    is Type -> asAsmType()
    is Handle -> toAsmHandle()
    is ConstantDynamic -> toAsmConstantDynamic()
    else -> this
}

internal fun List<Any>.replaceTypes(): List<Any> = when {
    isEmpty() -> this
    else -> map { convertToAsmConstant() }
}

internal fun Array<out Any>.replaceTypes(): Array<out Any> = when {
    isEmpty() -> this
    else -> Array(size) {
        val value = this[it]
        if (value is Type) value.asAsmType() else value
    }
}

internal fun Label.asLabelNode(): LabelNode = LabelNode(this)

internal fun List<LabelElement>.toNodeArray(): Array<out LabelElement> = toTypedArray()