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

package net.ormr.asmkt.type

/**
 * Represents a type that has an internal name.
 *
 * The only types that have an internal name are [ReferenceType] and [ArrayType].
 */
public sealed interface TypeWithInternalName : Type {
    /**
     * The internal name of the type.
     *
     * The internal name for a type is the fully qualified name of the type, with `.` replaced with `/`. For example,
     * the internal name for `java.lang.String` is `java/lang/String`.
     */
    public val internalName: String
}