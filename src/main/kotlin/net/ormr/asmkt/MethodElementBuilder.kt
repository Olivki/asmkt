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

import net.ormr.asmkt.type.MethodType
import net.ormr.asmkt.type.ReferenceType
import net.ormr.asmkt.type.ReturnableType
import org.objectweb.asm.Label
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.ParameterNode
import org.objectweb.asm.tree.TryCatchBlockNode
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents a method.
 *
 * @property [owner] The class that owns the method.
 * @property [name] The name of the method.
 * @property [flags] The access flags of the method.
 * @property [type] The type of the method.
 * @property [signature] The generic signature of the method, or `null` if the method does not have a signature.
 * @property [exceptions] The exceptions that the method can throw.
 */
@AsmKtDsl
public class MethodElementBuilder internal constructor(
    public val owner: ClassElementBuilder,
    public val name: String,
    override val flags: MethodAccessFlags,
    public val type: MethodType,
    public val signature: String?,
    public val exceptions: List<ReferenceType>,
) : ElementBuilder, FlaggableElementBuilder<MethodAccessFlag>, VersionedElementBuilder,
    AnnotationValueConversionContext {
    public val body: MethodBodyBuilder = MethodBodyBuilder(this)

    override val version: ClassFileVersion
        get() = owner.version

    /**
     * The type of the class that owns the method.
     *
     * @see [owner]
     */
    public val ownerType: ReferenceType
        get() = owner.type

    /**
     * The return type of the method.
     *
     * @see [type]
     */
    public val returnType: ReturnableType
        get() = type.returnType

    private val tryCatchBlocks = mutableListOf<TryCatchBlockNode>()
    private val localVariables = mutableListOf<LocalVariableNode>()
    private val parameters = mutableListOf<ParameterNode>()

    /**
     * The default value for the annotation property, or `null` if the method is not an annotation property, or if
     * the annotation property does not have a default value.
     *
     * @throws [IllegalArgumentException] *(on set)* if the [owner] of the method is not an annotation
     */
    public var defaultAnnotationValue: AnnotationDefaultValue? = null
        set(value) {
            if (value != null) {
                require(owner.isAnnotation) { "Owner ($owner) of method ($this) is not an annotation" }
            }
            field = value
        }

    /**
     * Sets the access flags for the parameter with the given [name] to the given [flag].
     *
     * @param [name] the name of the parameter to set the flags for
     * @param [flag] the flag to set for the parameter
     */
    @AsmKtDsl
    public fun parameterFlag(name: String, flag: ParameterAccessFlag) {
        parameterFlag(name, flag.asAccessFlags())
    }

    /**
     * Sets the access flags for the parameter with the given [name] to the given [flags].
     *
     * @param [name] the name of the parameter to set the flags for
     * @param [flags] the flags to set for the parameter
     */
    @AsmKtDsl
    public fun parameterFlag(name: String, flags: ParameterAccessFlags) {
        parameters.add(ParameterNode(name, flags.asInt()))
    }

    // -- SCOPING -- \\
    public inline fun withBody(block: MethodBodyBuilder.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        block(body)
    }

    public inline fun withCode(block: CodeBuilder.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        block(body.code)
    }

    // -- INTERNALS -- \\
    internal fun addTryCatchBlock(start: Label, end: Label, handler: Label, exceptionInternalName: String?) {
        tryCatchBlocks.add(
            TryCatchBlockNode(
                start.asLabelNode(),
                end.asLabelNode(),
                handler.asLabelNode(),
                exceptionInternalName,
            )
        )
    }

    internal fun addLocalVariable(
        name: String,
        descriptor: String,
        signature: String?,
        start: Label,
        end: Label,
        index: Int,
    ) {
        localVariables.add(
            LocalVariableNode(
                name,
                descriptor,
                signature,
                start.asLabelNode(),
                end.asLabelNode(),
                index,
            )
        )
    }
}