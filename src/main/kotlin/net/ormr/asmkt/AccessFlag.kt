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

@file:Suppress("ClassName")

package net.ormr.asmkt

import org.objectweb.asm.Opcodes

public sealed interface AccessFlag {
    public fun asInt(): Int

    /**
     * Not an actual access flag, but used as a sentinel value.
     */
    public data object NONE : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ParameterAccessFlag, ConstructorAccessFlag {
        override fun asInt(): Int = 0
    }

    // -- Class Access Flags -- \\
    public data object SUPER : AccessFlag, ClassAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_SUPER
    }

    public data object INTERFACE : AccessFlag, ClassAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_INTERFACE
    }

    public data object ANNOTATION : AccessFlag, ClassAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_ANNOTATION
    }

    public data object MODULE : AccessFlag, ClassAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_MODULE
    }

    public data object RECORD : AccessFlag, ClassAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_RECORD
    }

    // -- Method Access Flags -- \\
    public data object SYNCHRONIZED : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_SYNCHRONIZED
    }

    public data object BRIDGE : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_BRIDGE
    }

    public data object VARARGS : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_VARARGS
    }

    public data object TRANSIENT : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_TRANSIENT
    }

    public data object NATIVE : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_NATIVE
    }

    public data object STRICT : AccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_STRICT
    }

    // -- Field Access Flags -- \\
    public data object VOLATILE : AccessFlag, FieldAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_VOLATILE
    }

    // -- Module Access Flags -- \\
    public data object OPEN : AccessFlag, ModuleAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_OPEN
    }

    public data object TRANSITIVE : AccessFlag, ModuleAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_TRANSITIVE
    }

    public data object STATIC_PHASE : AccessFlag, ModuleAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_STATIC_PHASE
    }

    // -- Mixed Access Flags -- \\
    public data object ENUM : AccessFlag, ClassAccessFlag, FieldAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_ENUM
    }

    public data object MANDATED : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ParameterAccessFlag, ModuleAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_MANDATED
    }

    public data object SYNTHETIC : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ParameterAccessFlag, ModuleAccessFlag, ConstructorAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_SYNTHETIC
    }

    public data object DEPRECATED : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ConstructorAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_DEPRECATED
    }

    public data object STATIC : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_STATIC
    }

    public data object FINAL : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ParameterAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_FINAL
    }

    public data object PUBLIC : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ConstructorAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_PUBLIC
    }

    public data object PROTECTED : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ConstructorAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_PROTECTED
    }

    public data object PRIVATE : AccessFlag, SimpleClassAccessFlag, MethodAccessFlag, FieldAccessFlag,
        ConstructorAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_PRIVATE
    }

    public data object ABSTRACT : AccessFlag, ClassAccessFlag, MethodAccessFlag {
        override fun asInt(): Int = Opcodes.ACC_ABSTRACT
    }
}

// TODO: InnerClass?

public sealed interface ClassAccessFlag : AccessFlag

// TODO: better name
public sealed interface SimpleClassAccessFlag : ClassAccessFlag

public sealed interface MethodAccessFlag : AccessFlag

public sealed interface ConstructorAccessFlag : AccessFlag, MethodAccessFlag

public sealed interface FieldAccessFlag : AccessFlag

public sealed interface ModuleAccessFlag : AccessFlag

public sealed interface ParameterAccessFlag : AccessFlag