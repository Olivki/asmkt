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
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ModuleExportNode
import org.objectweb.asm.tree.ModuleNode
import org.objectweb.asm.tree.ModuleOpenNode
import org.objectweb.asm.tree.ModuleProvideNode
import org.objectweb.asm.tree.ModuleRequireNode

@AsmKt
data class BytecodeModule internal constructor(
    val name: String,
    override val access: Int,
    val version: String?,
    val mainClass: BytecodeClass
) : AccessibleBytecode {
    private val packages: MutableList<String> = mutableListOf()
    private val requires: MutableList<ModuleRequireNode> = mutableListOf()
    private val exports: MutableList<ModuleExportNode> = mutableListOf()
    private val opens: MutableList<ModuleOpenNode> = mutableListOf()
    private val uses: MutableList<String> = mutableListOf()
    private val provides: MutableList<ModuleProvideNode> = mutableListOf()

    // TODO: unsure if when the documentation for the lists in 'ModuleNode' says 'internalName' they mean
    //       'internalName' from 'Type.getInternalName' or they mean 'className' from 'Type.getClassName'

    @AsmKt
    @JvmName("addPackage")
    fun `package`(pack: String): BytecodeModule = apply {
        packages += pack
    }

    @JvmOverloads
    @AsmKt
    @JvmName("addRequire")
    fun require(module: String, access: Int, version: String? = null): BytecodeModule = apply {
        requires += ModuleRequireNode(module, access, version)
    }

    // TODO: document the throws
    @AsmKt
    @JvmName("addExport")
    fun export(pack: String, access: Int, vararg modules: String): BytecodeModule = apply {
        exports += ModuleExportNode(pack, access, modules.toMutableList())
    }

    // TODO: document the throws
    @AsmKt
    @JvmName("addOpen")
    fun open(pack: String, access: Int, vararg modules: String): BytecodeModule = apply {
        opens += ModuleOpenNode(pack, access, modules.toMutableList())
    }

    @AsmKt
    @JvmName("addUse")
    fun use(service: ReferenceType): BytecodeModule = apply {
        uses += service.internalName
    }

    @AsmKt
    @JvmName("addProvide")
    fun provide(service: ReferenceType, vararg providers: ReferenceType): BytecodeModule = apply {
        provides += ModuleProvideNode(service.internalName, providers.mapTo(mutableListOf()) { it.internalName })
    }

    @JvmSynthetic
    internal fun toNode(): ModuleNode =
        ModuleNode(Opcodes.ASM8, name, access, version, requires, exports, opens, uses, provides)

    /**
     * Returns `true` if `this` module is [open][Modifiers.OPEN], otherwise `false`.
     */
    val isOpen: Boolean
        get() = access and Modifiers.OPEN != 0
}