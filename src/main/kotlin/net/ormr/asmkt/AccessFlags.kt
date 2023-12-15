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

public typealias ClassAccessFlags = AccessFlags<ClassAccessFlag>
public typealias SimpleClassAccessFlags = AccessFlags<SimpleClassAccessFlag>
public typealias MethodAccessFlags = AccessFlags<MethodAccessFlag>
public typealias ConstructorAccessFlags = AccessFlags<ConstructorAccessFlag>
public typealias FieldAccessFlags = AccessFlags<FieldAccessFlag>
public typealias ModuleAccessFlags = AccessFlags<ModuleAccessFlag>
public typealias ParameterAccessFlags = AccessFlags<ParameterAccessFlag>

@JvmInline
public value class AccessFlags<out Flag : AccessFlag> internal constructor(private val mask: Int) {
    public operator fun plus(other: AccessFlags<@UnsafeVariance Flag>): AccessFlags<Flag> =
        AccessFlags(mask or other.mask)

    public operator fun plus(flag: @UnsafeVariance Flag): AccessFlags<Flag> = when (flag) {
        AccessFlag.NONE -> this
        else -> AccessFlags(mask or flag.asInt())
    }

    public operator fun contains(flag: @UnsafeVariance Flag): Boolean = when (flag) {
        AccessFlag.NONE -> false
        else -> mask and flag.asInt() != 0
    }

    public operator fun contains(flags: AccessFlags<@UnsafeVariance Flag>): Boolean = mask and flags.mask != 0

    public fun asInt(): Int = mask

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = "0x${mask.toHexString(HexFormat.UpperCase)}"

    public companion object {
        public fun <Flag : AccessFlag> none(): AccessFlags<Flag> = AccessFlags(AccessFlag.NONE.asInt())
    }
}

public fun <Flag : AccessFlag> Flag.asAccessFlags(): AccessFlags<Flag> = AccessFlags(asInt())

public operator fun <Flag : AccessFlag> Flag.plus(other: Flag): AccessFlags<Flag> = when (this) {
    AccessFlag.NONE -> other.asAccessFlags()
    else -> AccessFlags(asInt() or other.asInt())
}

public operator fun <Flag : AccessFlag> Flag.plus(flags: AccessFlags<Flag>): AccessFlags<Flag> = when (this) {
    AccessFlag.NONE -> flags
    else -> AccessFlags(asInt() or flags.asInt())
}

