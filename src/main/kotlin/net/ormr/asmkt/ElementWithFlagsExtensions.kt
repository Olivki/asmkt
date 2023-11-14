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

// -- Class Access Flags -- \\
/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [SUPER], otherwise `false`.
 */
public val FlaggableElementBuilder<in SUPER>.isSuper: Boolean
    get() = SUPER in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [INTERFACE], otherwise `false`.
 */
public val FlaggableElementBuilder<in INTERFACE>.isInterface: Boolean
    get() = INTERFACE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [ANNOTATION], otherwise `false`.
 */
public val FlaggableElementBuilder<in ANNOTATION>.isAnnotation: Boolean
    get() = ANNOTATION in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [MODULE], otherwise `false`.
 */
public val FlaggableElementBuilder<in MODULE>.isModule: Boolean
    get() = MODULE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [RECORD], otherwise `false`.
 */
public val FlaggableElementBuilder<in RECORD>.isRecord: Boolean
    get() = RECORD in flags

// -- Method Access Flags -- \\
/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [SYNCHRONIZED], otherwise `false`.
 */
public val FlaggableElementBuilder<in SYNCHRONIZED>.isSynchronized: Boolean
    get() = SYNCHRONIZED in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [BRIDGE], otherwise `false`.
 */
public val FlaggableElementBuilder<in BRIDGE>.isBridge: Boolean
    get() = BRIDGE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [VARARGS], otherwise `false`.
 */
public val FlaggableElementBuilder<in VARARGS>.isVarargs: Boolean
    get() = VARARGS in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [TRANSIENT], otherwise `false`.
 */
public val FlaggableElementBuilder<in TRANSIENT>.isTransient: Boolean
    get() = TRANSIENT in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [NATIVE], otherwise `false`.
 */
public val FlaggableElementBuilder<in NATIVE>.isNative: Boolean
    get() = NATIVE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [INTERFACE], otherwise `false`.
 */
public val FlaggableElementBuilder<in STRICT>.isStrict: Boolean
    get() = STRICT in flags

// -- Field Access Flags -- \\
/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [VOLATILE], otherwise `false`.
 */
public val FlaggableElementBuilder<in VOLATILE>.isVolatile: Boolean
    get() = VOLATILE in flags

// -- Module Access Flags -- \\
/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [OPEN], otherwise `false`.
 */
public val FlaggableElementBuilder<in OPEN>.isOpen: Boolean
    get() = OPEN in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [STATIC_PHASE], otherwise `false`.
 */
public val FlaggableElementBuilder<in STATIC_PHASE>.isStaticPhase: Boolean
    get() = STATIC_PHASE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [TRANSITIVE], otherwise `false`.
 */
public val FlaggableElementBuilder<in TRANSITIVE>.isTransitive: Boolean
    get() = TRANSITIVE in flags

// -- Mixed Access Flags -- \\
/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [ENUM], otherwise `false`.
 */
public val FlaggableElementBuilder<in ENUM>.isEnum: Boolean
    get() = ENUM in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [MANDATED], otherwise `false`.
 */
public val FlaggableElementBuilder<in MANDATED>.isMandated: Boolean
    get() = MANDATED in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [SYNTHETIC], otherwise `false`.
 */
public val FlaggableElementBuilder<in SYNTHETIC>.isSynthetic: Boolean
    get() = SYNTHETIC in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [DEPRECATED], otherwise `false`.
 */
public val FlaggableElementBuilder<in DEPRECATED>.isDeprecated: Boolean
    get() = DEPRECATED in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [STATIC], otherwise `false`.
 */
public val FlaggableElementBuilder<in STATIC>.isStatic: Boolean
    get() = STATIC in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [FINAL], otherwise `false`.
 */
public val FlaggableElementBuilder<in FINAL>.isFinal: Boolean
    get() = FINAL in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [PUBLIC], otherwise `false`.
 */
public val FlaggableElementBuilder<in PUBLIC>.isPublic: Boolean
    get() = PUBLIC in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [PROTECTED], otherwise `false`.
 */
public val FlaggableElementBuilder<in PROTECTED>.isProtected: Boolean
    get() = PROTECTED in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [PRIVATE], otherwise `false`.
 */
public val FlaggableElementBuilder<in PRIVATE>.isPrivate: Boolean
    get() = PRIVATE in flags

/**
 * Returns `true` if `this` elements [flags][FlaggableElementBuilder.flags] contains [ABSTRACT], otherwise `false`.
 */
public val FlaggableElementBuilder<in ABSTRACT>.isAbstract: Boolean
    get() = ABSTRACT in flags