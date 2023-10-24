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

public sealed interface ElementWithVersion : ElementModel {
    /**
     * The JVM class version the element is set to compile to.
     */
    public val version: ClassFileVersion
}

internal inline fun ElementWithVersion.requireMinVersion(version: ClassFileVersion, feature: () -> String) {
    require(this.version >= version) { "$${feature()} requires at least version $version, but class version is set to ${this.version}." }
}