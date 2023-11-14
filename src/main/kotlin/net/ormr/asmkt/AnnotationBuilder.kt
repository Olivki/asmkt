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
import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.TypePath
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.TypeAnnotationNode

@AsmKtDsl
public sealed class AnnotationBuilder<N : AnnotationNode> : AnnotationValueConversionContext {
    public abstract val type: ReferenceType
    private val entries = mutableMapOf<String, AnnotationValue>()

    /**
     * Adds a value with the given [name] and [value] to the annotation.
     *
     * @param [name] the name of the value
     * @param [value] the value to add
     */
    @AsmKtDsl
    public fun value(name: String, value: AnnotationValue) {
        entries[name] = value
    }

    protected fun populateNode(node: N): N {
        for ((name, wrapper) in entries) {
            when (wrapper) {
                is ForString -> node.visit(name, wrapper.value)
                is ForBoolean -> node.visit(name, wrapper.value)
                is ForChar -> node.visit(name, wrapper.value)
                is ForByte -> node.visit(name, wrapper.value)
                is ForShort -> node.visit(name, wrapper.value)
                is ForInt -> node.visit(name, wrapper.value)
                is ForLong -> node.visit(name, wrapper.value)
                is ForFloat -> node.visit(name, wrapper.value)
                is ForDouble -> node.visit(name, wrapper.value)
                is ForClass -> node.visit(name, wrapper.value)
                is ForEnum<*> -> node.visitEnum(name, wrapper.type.descriptor, wrapper.entryName)
                is ForArray<*> -> node.visit(name, wrapper.value.map(AnnotationArrayValue::getValue))
                is ForBuilder -> node.visit(name, wrapper.value.buildNode())
                is ForArrayBuilder<*> -> node.visit(name, wrapper.value.entries.map(AnnotationArrayValue::getValue))
            }
        }
        return node
    }

    protected fun formatValues(): String = entries
        .entries
        .joinToString(prefix = "{", postfix = "}") { (name, value) ->
            "'$name' = ${value.asString()}"
        }

    internal abstract fun buildNode(): N
}

@AsmKtDsl
public class ElementAnnotationBuilder @PublishedApi internal constructor(
    override val type: ReferenceType,
) : AnnotationBuilder<AnnotationNode>() {
    override fun buildNode(): AnnotationNode = populateNode(AnnotationNode(type.descriptor))

    override fun toString(): String = "ElementAnnotationBuilder(type=${type.asString()}, values=${formatValues()})"
}

@AsmKtDsl
public class TypeAnnotationBuilder @PublishedApi internal constructor(
    public val typeRef: Int,
    public val typePath: TypePath?,
    override val type: ReferenceType,
) : AnnotationBuilder<TypeAnnotationNode>() {
    override fun buildNode(): TypeAnnotationNode = populateNode(TypeAnnotationNode(typeRef, typePath, type.descriptor))

    override fun toString(): String =
        "TypeAnnotationBuilder(typeRef=$typeRef, typePath=$typePath, type=${type.asString()}, values=${formatValues()})"
}

@AsmKtDsl
public class AnnotationArrayBuilder<T : AnnotationArrayValue> @PublishedApi internal constructor(
    public val name: String,
) : AnnotationValueConversionContext {
    internal val entries = mutableListOf<T>()

    /**
     * Adds the given [value] to the array.
     *
     * @param [value] the value to add
     */
    @AsmKtDsl
    public fun add(value: T) {
        entries += value
    }

    override fun toString(): String = "AnnotationArrayBuilder(name='$name', values=${
        entries.joinToString(prefix = "[", postfix = "]") { it.asString() }
    })"
}