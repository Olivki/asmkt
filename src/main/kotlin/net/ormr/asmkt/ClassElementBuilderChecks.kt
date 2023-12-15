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

@file:Suppress("NOTHING_TO_INLINE")

package net.ormr.asmkt

import net.ormr.asmkt.type.ReferenceType
import org.objectweb.asm.tree.LabelNode

internal inline fun ClassElementBuilder.verifyState() {
    checkFlags()
    if (isEnum) requireSuperType(ReferenceType.ENUM) { "Enum classes" }
    if (isModule) requireMinVersion(ClassFileVersion.RELEASE_9) { "Module" }
    if (isRecord) {
        requireMinVersion(ClassFileVersion.RELEASE_14) { "Record classes" }
        requireSuperType(ReferenceType.RECORD) { "Record classes" }
    }
}

internal inline fun ClassElementBuilder.verifyStateBeforeBuild() {
    if (permittedSubtypes.isNotEmpty()) {
        requireMinVersion(ClassFileVersion.RELEASE_16) { "Permitted subtypes" }
        requireOneKindOf(ClassKind.INHERITABLE) { "permitted subtypes" }
    }

    if (fields.isNotEmpty()) {
        requireNotOneKindOf(ClassKind.NO_FIELDS) { "fields" }

        for ((_, field) in fields) {
            checkInterfaceField(field)
        }
    }

    if (kind == ClassKind.INTERFACE) {
        require(hasDefaultSupertype) { "Supertype (${supertype.asString()}) of interface (${type.asString()}) must be 'java.lang.Object'" }
    }

    if (kind == ClassKind.RECORD) {
        require(supertype == ReferenceType.RECORD) { "Supertype (${supertype.asString()}) of record (${type.asString()}) must be 'java.lang.Record'" }
    }

    if (methods.isNotEmpty()) {
        requireNotOneKindOf(ClassKind.NO_METHODS) { "methods" }

        for (method in methods) {
            if (method.isAbstract && !(isAbstract || isInterface)) {
                requireOneKindOf(ClassKind.WITH_ABSTRACT_METHODS) { "abstract methods" }
            }

            if (method.body.isEmpty() && !method.isAbstract) {
                throw IllegalArgumentException("No instructions defined for non-abstract method (${method.asString()}) in ${type.asString()}.")
            }

            if (method.body.isNotEmpty() && method.isAbstract) {
                if (!method.body.instructions.all { it is LabelNode }) {
                    throw IllegalArgumentException("Abstract method (${method.asString()}) in ${type.asString()} contains instructions")
                }
            }
        }
    }
}

context(ClassElementBuilder)
private fun checkFlags() {
    val kind = ClassKind.fromFlagsOrNull(flags)
    if (kind != null) {
        throw IllegalArgumentException("Flag '${kind.flagName}' found in 'flags', use 'kind = BytecodeClassKind.${kind.name}' instead.")
    }
}

context(ClassElementBuilder)
internal inline fun requireSuperType(otherSuperType: ReferenceType, feature: () -> String) {
    require(supertype == otherSuperType) { "${feature()} requires super type to be ${otherSuperType.asString()}, but super type was ${supertype.asString()}." }
}

context(ClassElementBuilder)
internal inline fun requireKind(otherKind: ClassKind, feature: () -> String) {
    require(kind == otherKind) { "Only a class of kind $otherKind can use ${feature()}, but current kind is $kind" }
}

context(ClassElementBuilder)
internal inline fun requireOneKindOf(kinds: Set<ClassKind>, feature: () -> String) {
    require(kind in kinds) { "Only classes of kind ${kinds.listOut()} are allowed to have ${feature()}, but current kind is $kind" }
}

context(ClassElementBuilder)
internal inline fun requireNotKind(otherKind: ClassKind, feature: () -> String) {
    require(kind != otherKind) { "Classes of kind $otherKind are not allowed to have ${feature()}" }
}

context(ClassElementBuilder)
internal inline fun requireNotOneKindOf(kinds: Set<ClassKind>, feature: () -> String) {
    require(kind !in kinds) { "Classes of kind $kind are not allowed to have ${feature()}" }
}

internal fun ClassElementBuilder.checkInterfaceField(field: FieldElement) {
    if (isInterface) {
        require(field.isPublic) { "Interface fields must be public, but field '${field.name}' is not." }
        require(field.isStatic) { "Interface fields must be static, but field '${field.name}' is not." }
        require(field.isFinal) { "Interface fields must be final, but field '${field.name}' is not." }
    }
}

private fun Set<ClassKind>.listOut(): String = buildString {
    val lastIndex = this@listOut.size - 1
    for ((i, kind) in this@listOut.withIndex()) {
        append(kind.name)
        if (i == lastIndex - 1) append(" or ")
        if (i < lastIndex - 1) append(", ")
    }
}