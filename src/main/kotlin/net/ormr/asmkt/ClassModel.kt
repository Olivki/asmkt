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

@AsmKtDsl
public class ClassModel(
    override val version: ClassFileVersion,
    public val kind: ClassKind,
    public val type: ClassDesc,
    override val flags: SimpleClassAccessFlags = AccessFlag.PUBLIC.asAccessFlags(),
    public val signature: String? = null,
    public val superType: ClassDesc = ClassDesc.OBJECT,
    public val interfaces: List<ClassDesc> = emptyList(),
    public val permittedSubtypes: List<ClassDesc> = emptyList(),
    public val sourceFile: String? = null,
    public val sourceDebug: String? = null,
) : ElementModel, ElementWithFlags<SimpleClassAccessFlag>, ElementWithVersion {
    /**
     * The method that this class belongs to, or `null` if this class does not belong to a method.
     */
    public var enclosingMethod: MethodModel? = null
        internal set

    public fun method(
        name: String,
        flags: MethodAccessFlags,
        type: MethodTypeDesc,
        signature: String?,
        exceptions: List<ClassDesc>,
    ): MethodModel {
        val model = MethodModel(this, name, flags, type, signature, exceptions)
        val dab = ((kind.plus(this.flags)))
        TODO("add model and stuff to class model")
    }
}