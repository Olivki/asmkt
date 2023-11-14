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

import net.ormr.asmkt.type.FieldType

// for now we're not exposing the 'value' property, as it's basically a trap for the unwary
// as it only supports very simple values, and you probably want to set the value in the
// init / cinit of the class manually anyway
@AsmKtDsl
public class FieldBuilder internal constructor(
    public val owner: ClassBuilder,
    public val name: String,
    override val flags: AccessFlags<FieldAccessFlag>,
    public val type: FieldType,
    public val signature: String?,
) : ElementBuilder, FlaggableElementBuilder<FieldAccessFlag> {
    // TODO: annotations
}