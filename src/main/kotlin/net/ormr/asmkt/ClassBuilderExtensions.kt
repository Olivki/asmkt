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

import net.ormr.asmkt.type.ReferenceType

/**
 * Returns `true` if `this` class is a [normal class][ClassKind.CLASS], otherwise `false`.
 */
public val ClassBuilder.isClass: Boolean
    get() = kind == ClassKind.CLASS

/**
 * Returns `true` if `this` class is an [abstract class][ClassKind.ABSTRACT_CLASS], otherwise `false`.
 */
public val ClassBuilder.isAbstract: Boolean
    get() = kind == ClassKind.ABSTRACT_CLASS

/**
 * Returns `true` if `this` class is an [interface][ClassKind.INTERFACE], otherwise `false`.
 */
public val ClassBuilder.isInterface: Boolean
    get() = kind == ClassKind.INTERFACE

/**
 * Returns `true` if `this` class is an [annotation][ClassKind.ANNOTATION], otherwise `false`.
 */
public val ClassBuilder.isAnnotation: Boolean
    get() = kind == ClassKind.ANNOTATION

/**
 * Returns `true` if `this` class is a [module][ClassKind.MODULE], otherwise `false`.
 */
public val ClassBuilder.isModule: Boolean
    get() = kind == ClassKind.MODULE

/**
 * Returns `true` if `this` class is a [record][ClassKind.RECORD], otherwise `false`.
 */
public val ClassBuilder.isRecord: Boolean
    get() = kind == ClassKind.RECORD

/**
 * Returns `true` if `this` class is an [enum][ClassKind.ENUM], otherwise `false`.
 */
public val ClassBuilder.isEnum: Boolean
    get() = kind == ClassKind.ENUM

/**
 * Returns `true` if `this` class represents a "sealed" type, otherwise `false`.
 */
public val ClassBuilder.isSealed: Boolean
    get() = kind.isInheritable && permittedSubtypes.isNotEmpty()

/**
 * Returns `true` if the [superType][ClassBuilder.superType] of the class is [OBJECT][ReferenceType.OBJECT], otherwise
 * `false`.
 */
public val ClassBuilder.hasDefaultSuperType: Boolean
    get() = superType == ReferenceType.OBJECT