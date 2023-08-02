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

/**
 * Represents a type that has an `access` value defined.
 */
public sealed interface AccessibleBytecode {
    /**
     * The access code of `this` element.
     *
     * See the values defined in [Modifier] for a list of potentially valid codes, which values are valid depends on the
     * type of `this` element.
     */
    public val access: Modifier

    /**
     * Returns `true` if `this` element is [static][Modifier.STATIC], otherwise `false`.
     */
    public val isStatic: Boolean
        get() = Modifier.STATIC in access

    /**
     * Returns `true` if `this` element is [final][Modifier.FINAL], otherwise `false`.
     */
    public val isFinal: Boolean
        get() = Modifier.FINAL in access

    /**
     * Returns `true` if `this` element is [abstract][Modifier.ABSTRACT], otherwise `false`.
     */
    public val isAbstract: Boolean
        get() = Modifier.ABSTRACT in access

    /**
     * Returns `true` if `this` element is [public][Modifier.PUBLIC], otherwise `false`.
     */
    public val isPublic: Boolean
        get() = Modifier.PUBLIC in access

    /**
     * Returns `true` if `this` element is [protected][Modifier.PROTECTED], otherwise `false`.
     */
    public val isProtected: Boolean
        get() = Modifier.PROTECTED in access

    /**
     * Returns `true` if `this` element is [private][Modifier.PRIVATE], otherwise `false`.
     */
    public val isPrivate: Boolean
        get() = Modifier.PRIVATE in access

    /**
     * Returns `true` if `this` element is [mandated][Modifier.MANDATED], otherwise `false`.
     */
    public val isMandated: Boolean
        get() = Modifier.MANDATED in access

    /**
     * Returns `true` if `this` element is [synthetic][Modifier.SYNTHETIC], otherwise `false`.
     */
    public val isSynthetic: Boolean
        get() = Modifier.SYNTHETIC in access
}