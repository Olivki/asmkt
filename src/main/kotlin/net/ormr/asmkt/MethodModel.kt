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

import net.ormr.asmkt.type.ClassDesc
import net.ormr.asmkt.type.MethodTypeDesc
import net.ormr.asmkt.type.ReturnableTypeDesc
import org.objectweb.asm.Label
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.ParameterNode
import org.objectweb.asm.tree.TryCatchBlockNode
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@AsmKtDsl
public class MethodModel(
    public val parent: ClassModel,
    public val name: String,
    override val flags: MethodAccessFlags,
    public val type: MethodTypeDesc,
    public val signature: String?,
    public val exceptions: List<ClassDesc>,
) : ElementModel, ElementWithFlags<MethodAccessFlag>, ElementWithVersion {
    public val body: MethodBodyBuilder = MethodBodyBuilder(this)

    override val version: ClassFileVersion
        get() = parent.version

    public val parentType: ClassDesc
        get() = parent.type

    public val returnType: ReturnableTypeDesc
        get() = type.returnType

    private val tryCatchBlocks = mutableListOf<TryCatchBlockNode>()
    private val localVariables = mutableListOf<LocalVariableNode>()
    private val parameters = mutableListOf<ParameterNode>()

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