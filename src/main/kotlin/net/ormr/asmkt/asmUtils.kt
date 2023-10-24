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

internal inline fun <K, V> Map<K, List<V>>.doEach(action: (K, V) -> Unit) {
    for ((i, entries) in this) {
        for (entry in entries) {
            action(i, entry)
        }
    }
}

internal fun List<Any>.replaceTypes(): List<Any> = when {
    isEmpty() -> this
    any { it is Type } -> map { if (it is Type) it.asAsmType() else it }
    else -> this
}

internal fun Array<out Any>.replaceTypes(): Array<out Any> = when {
    isEmpty() -> this
    any { it is Type } -> Array(size) {
        val value = this[it]
        if (value is Type) value.asAsmType() else value
    }
    else -> this
}

internal fun Label.asLabelNode(): LabelNode = LabelNode(this)

internal fun Array<out Label>.toNodeArray(): Array<out LabelNode> = Array(size) { this[it].asLabelNode() }

internal fun List<Label>.toNodeArray(): Array<out LabelNode> = Array(size) { this[it].asLabelNode() }