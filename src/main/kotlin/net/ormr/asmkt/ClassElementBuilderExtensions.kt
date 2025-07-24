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

import net.ormr.asmkt.AccessFlag.*
import net.ormr.asmkt.type.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns `true` if `this` class is a [normal class][ClassKind.CLASS], otherwise `false`.
 */
public val ClassElementBuilder.isClass: Boolean
    get() = kind == ClassKind.CLASS

/**
 * Returns `true` if `this` class is an [abstract class][ClassKind.ABSTRACT_CLASS], otherwise `false`.
 */
public val ClassElementBuilder.isAbstract: Boolean
    get() = kind == ClassKind.ABSTRACT_CLASS

/**
 * Returns `true` if `this` class is an [interface][ClassKind.INTERFACE], otherwise `false`.
 */
public val ClassElementBuilder.isInterface: Boolean
    get() = kind == ClassKind.INTERFACE

/**
 * Returns `true` if `this` class is an [annotation][ClassKind.ANNOTATION], otherwise `false`.
 */
public val ClassElementBuilder.isAnnotation: Boolean
    get() = kind == ClassKind.ANNOTATION

/**
 * Returns `true` if `this` class is a [module][ClassKind.MODULE], otherwise `false`.
 */
public val ClassElementBuilder.isModule: Boolean
    get() = kind == ClassKind.MODULE

/**
 * Returns `true` if `this` class is a [record][ClassKind.RECORD], otherwise `false`.
 */
public val ClassElementBuilder.isRecord: Boolean
    get() = kind == ClassKind.RECORD

/**
 * Returns `true` if `this` class is an [enum][ClassKind.ENUM], otherwise `false`.
 */
public val ClassElementBuilder.isEnum: Boolean
    get() = kind == ClassKind.ENUM

/**
 * Returns `true` if `this` class represents a "sealed" type, otherwise `false`.
 */
public val ClassElementBuilder.isSealed: Boolean
    get() = kind.isInheritable && permittedSubtypes.isNotEmpty()

/**
 * Returns `true` if the [superType][ClassElementBuilder.supertype] of the class is [OBJECT][ReferenceType.OBJECT], otherwise
 * `false`.
 */
public val ClassElementBuilder.hasDefaultSupertype: Boolean
    get() = supertype == ReferenceType.OBJECT

// -- FIELDS -- \\
@AsmKtDsl
public inline fun ClassElementBuilder.field(
    name: String,
    flags: FieldAccessFlags,
    type: FieldType,
    signature: String? = null,
    builder: FieldElementBuilder.() -> Unit = {},
): FieldElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return field(FieldElement(this, name, flags, type, signature, builder))
}

@AsmKtDsl
public inline fun ClassElementBuilder.field(
    name: String,
    flags: FieldAccessFlag,
    type: FieldType,
    signature: String? = null,
    builder: FieldElementBuilder.() -> Unit = {},
): FieldElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return field(name, flags.asAccessFlags(), type, signature, builder)
}

// -- METHODS -- \\
@AsmKtDsl
public inline fun ClassElementBuilder.method(
    name: String,
    flags: MethodAccessFlags,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    builder: MethodElementBuilder.() -> Unit = {},
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        MethodElementBuilder(
            owner = this,
            name = name,
            flags = flags,
            type = type,
            signature = signature,
            exceptions = exceptions,
        ).apply(builder).build(),
    )
}

@AsmKtDsl
public inline fun ClassElementBuilder.method(
    name: String,
    flags: MethodAccessFlag,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    builder: MethodElementBuilder.() -> Unit = {},
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        name = name,
        flags = flags.asAccessFlags(),
        type = type,
        signature = signature,
        exceptions = exceptions,
        builder = builder,
    )
}

// -- CONSTRUCTORS -- \\
@AsmKtDsl
public inline fun ClassElementBuilder.constructor(
    flags: ConstructorAccessFlags,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    builder: MethodElementBuilder.() -> Unit = {},
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    require(type.returnType is VoidType) { "Return type (${type.returnType.asString()}) must be void" }

    return method(
        name = "<init>",
        flags = flags,
        type = type,
        signature = signature,
        exceptions = exceptions,
        builder = builder,
    )
}

@AsmKtDsl
public inline fun ClassElementBuilder.constructor(
    flags: ConstructorAccessFlag,
    type: MethodType,
    signature: String? = null,
    exceptions: List<ReferenceType> = emptyList(),
    builder: MethodElementBuilder.() -> Unit = {},
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return constructor(
        flags = flags.asAccessFlags(),
        type = type,
        signature = signature,
        exceptions = exceptions,
        builder = builder,
    )
}

@AsmKtDsl
public fun ClassElementBuilder.defaultConstructor(flags: ConstructorAccessFlags): MethodElement {
    require(hasDefaultSupertype) { "Expected supertype (java/lang/Object) but got (${supertype.asString()}) in (${type.asString()}) for default constructor" }
    return constructor(flags = flags, type = MethodType(VoidType)) {
        withBody {
            loadThis()
            invokeConstructor(method.owner.supertype, MethodType(VoidType))
            returnValue()
        }
    }
}

@AsmKtDsl
public fun ClassElementBuilder.defaultConstructor(
    flags: ConstructorAccessFlag = PUBLIC,
): MethodElement = defaultConstructor(flags.asAccessFlags())

// -- STATIC -- \\
@AsmKtDsl
public inline fun ClassElementBuilder.staticInit(
    builder: MethodElementBuilder.() -> Unit = {},
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        name = "<clinit>",
        flags = STATIC,
        type = MethodType(VoidType),
        builder = builder,
    )
}

// -- MISC -- \\
@AsmKtDsl
public inline fun ClassElementBuilder.equals(
    isFinal: Boolean = false,
    builder: MethodElementBuilder.() -> Unit,
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        name = "equals",
        flags = PUBLIC + (if (isFinal) FINAL else NONE),
        type = MethodType(BooleanType, ReferenceType.OBJECT),
        builder = builder,
    )
}

@AsmKtDsl
public inline fun ClassElementBuilder.hashCode(
    isFinal: Boolean = false,
    builder: MethodElementBuilder.() -> Unit,
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        name = "hashCode",
        flags = PUBLIC + (if (isFinal) FINAL else NONE),
        type = MethodType(IntType),
        builder = builder,
    )
}

@AsmKtDsl
public inline fun ClassElementBuilder.toString(
    isFinal: Boolean = false,
    builder: MethodElementBuilder.() -> Unit,
): MethodElement {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return method(
        name = "toString",
        flags = PUBLIC + (if (isFinal) FINAL else NONE),
        type = MethodType(ReferenceType.STRING),
        builder = builder,
    )
}