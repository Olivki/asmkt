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

import net.ormr.asmkt.types.FieldType
import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.PrimitiveType
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.TypePath
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InnerClassNode
import org.objectweb.asm.tree.TypeAnnotationNode

@AsmKtDsl
public data class BytecodeClass(
    public val type: ReferenceType,
    override val version: BytecodeVersion,
    public val kind: BytecodeClassKind = BytecodeClassKind.CLASS,
    override val access: Int = Modifiers.PUBLIC,
    public val superType: ReferenceType = OBJECT,
    public val interfaces: List<ReferenceType> = emptyList(),
    public val permittedSubtypes: List<ReferenceType> = emptyList(),
    public val sourceFile: String? = null,
    public val sourceDebug: String? = null,
) : AccessibleBytecode, AnnotatableBytecode, AnnotatableTypeBytecode, BytecodeVersionContainer {
    /**
     * Returns `true` if `this` class is a [normal class][BytecodeClassKind.CLASS], otherwise `false`.
     */
    public val isClass: Boolean
        get() = kind == BytecodeClassKind.CLASS

    /**
     * Returns `true` if `this` class is an [abstract class][BytecodeClassKind.ABSTRACT_CLASS], otherwise `false`.
     */
    override val isAbstract: Boolean
        get() = kind == BytecodeClassKind.ABSTRACT_CLASS

    /**
     * Returns `true` if `this` class is a [interface][BytecodeClassKind.INTERFACE], otherwise `false`.
     */
    public val isInterface: Boolean
        get() = kind == BytecodeClassKind.INTERFACE

    /**
     * Returns `true` if `this` class is an [annotation][BytecodeClassKind.ANNOTATION], otherwise `false`.
     */
    public val isAnnotation: Boolean
        get() = kind == BytecodeClassKind.ANNOTATION

    /**
     * Returns `true` if `this` class is a [module][BytecodeClassKind.MODULE], otherwise `false`.
     */
    public val isModule: Boolean
        get() = kind == BytecodeClassKind.MODULE

    /**
     * Returns `true` if `this` class is a [record][BytecodeClassKind.RECORD], otherwise `false`.
     */
    public val isRecord: Boolean
        get() = kind == BytecodeClassKind.RECORD

    /**
     * Returns `true` if `this` class is an [enum][BytecodeClassKind.ENUM], otherwise `false`.
     */
    public val isEnum: Boolean
        get() = kind == BytecodeClassKind.ENUM

    /**
     * Returns `true` if `this` class represents a "sealed" type, otherwise `false`.
     */
    public val isSealed: Boolean
        get() = kind.canHavePermittedSubtypes && permittedSubtypes.isNotEmpty()

    /**
     * The [module][BytecodeModule] that belongs to `this` class, or `null` if no module is defined for `this` class.
     *
     * @see [defineModule]
     */
    public var module: BytecodeModule? = null
        private set

    /**
     * The [class][BytecodeClass] that `this` is an inner-class of, or `null` if `this` is not an inner-class.
     *
     * @see [defineInnerClass]
     */
    public var enclosingClass: BytecodeClass? = null
        private set

    /**
     * The method that this class belongs to, or `null` if this class does not belong to a method.
     */
    public var enclosingMethod: BytecodeMethod? = null
        internal set

    private val innerClasses: MutableMap<String, BytecodeClass> = mutableMapOf()
    private val methods: MutableSet<BytecodeMethod> = mutableSetOf()
    private val fields: MutableMap<String, BytecodeField> = mutableMapOf()
    private val recordComponents: MutableList<BytecodeRecordComponent> = mutableListOf()

    // annotations
    private val visibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    // type annotations
    private val visibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()
    private val invisibleTypeAnnotations: MutableList<BytecodeAnnotation> = mutableListOf()

    /**
     * The [internal name][ReferenceType.internalName] of this class.
     */
    public val internalName: String
        get() = type.internalName

    /**
     * The [class name][ReferenceType.className] of this class.
     */
    public val className: String
        get() = type.className

    /**
     * The [simple name][ReferenceType.simpleName] of this class.
     */
    public val simpleName: String
        get() = type.simpleName

    /**
     * Returns `true` if the [superType] of this class is [OBJECT][ReferenceType.OBJECT], otherwise `false`.
     */
    public val isDefaultSuperType: Boolean
        get() = superType == OBJECT

    /**
     * Whether `supercall` methods should be treated specially by the [invokeSpecial][BytecodeMethod.invokeSpecial]
     * instruction.
     *
     * If this is `true` then the [SUPER][Modifiers.SUPER] modifier is added to the [access] value when writing `this`
     * class [to a byte array][toByteArray].
     */
    public var treatSuperSpecially: Boolean = true

    init {
        checkAccess()
        if (isEnum) requireSuperType(ReferenceType.ENUM, "Enum classes")
        if (isModule) requireMinVersion(BytecodeVersion.JAVA_9, "Module")
        if (isRecord) {
            requireMinVersion(BytecodeVersion.JAVA_14, "Record classes")
            requireSuperType(ReferenceType.RECORD, "Record classes")
        }
        if (permittedSubtypes.isNotEmpty()) {
            requireMinVersion(BytecodeVersion.JAVA_16, "Permitted subtypes")
            requireOneKindOf(BytecodeClassKind.havePermittedSubtypes, "permitted subtypes")
        }
    }

    private fun checkAccess() {
        val foundKind = BytecodeClassKind.getByOpcodeOrNull(access)
        if (foundKind != null) {
            throw IllegalArgumentException("Opcode '${foundKind.opcodeName}' found in 'access', use 'kind = BytecodeClassKind.${foundKind.name}' instead.")
        }
    }

    @AsmKtDsl
    override fun defineAnnotation(type: ReferenceType, isVisible: Boolean, allowRepeats: Boolean): BytecodeAnnotation {
        val annotation = BytecodeAnnotation(type)
        return handleAnnotations(this, annotation, visibleAnnotations, invisibleAnnotations, isVisible, allowRepeats)
    }

    override fun defineTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        annotationType: ReferenceType,
        isVisible: Boolean,
        allowRepeats: Boolean,
    ): BytecodeAnnotation {
        val annotation =
            BytecodeAnnotation(annotationType, TypeAnnotationNode(typeRef, typePath, annotationType.descriptor))
        return handleAnnotations(
            this,
            annotation,
            visibleTypeAnnotations,
            invisibleTypeAnnotations,
            isVisible,
            allowRepeats
        )
    }

    @AsmKtDsl
    public fun defineModule(name: String, access: Int, version: String? = null): BytecodeModule {
        require(isModule) { "Can't define module for non module class '$className'" }
        val module = BytecodeModule(name, access, version, this)
        this.module = module
        return module
    }

    @AsmKtDsl
    public fun defineInnerClass(child: BytecodeClass) {
        var childName = child.internalName
        childName = when {
            '$' in childName -> childName.substringAfterLast('$')
            else -> childName.substringAfterLast('/')
        }

        defineInnerClass(childName.substringAfterLast('$'), child)
    }

    @AsmKtDsl
    public fun defineInnerClass(innerName: String, child: BytecodeClass) {
        child.enclosingClass = this
        innerClasses[innerName] = child
    }

    @AsmKtDsl
    public fun defineField(
        name: String,
        access: Int,
        type: FieldType,
        signature: String? = null,
        value: Any? = null,
    ): BytecodeField {
        requireNotOneKindOf(BytecodeClassKind.notHaveFields, "fields")
        val field = BytecodeField(name, access, type, signature, value)
        fields[name] = field
        return field
    }

    @AsmKtDsl
    public fun defineMethod(
        name: String,
        access: Int,
        type: MethodType,
        signature: String? = null,
        exceptions: List<ReferenceType> = emptyList(),
    ): BytecodeMethod {
        requireNotOneKindOf(BytecodeClassKind.notHaveMethods, "methods")
        val block = BytecodeMethod(name, access, type, signature, exceptions, this)
        methods += block
        return block
    }

    @AsmKtDsl
    public fun defineRecordComponent(
        name: String,
        type: FieldType,
        signature: String? = null,
    ): BytecodeRecordComponent {
        requireKind(BytecodeClassKind.MODULE, "record components")
        val component = BytecodeRecordComponent(name, type, signature)
        recordComponents += component
        return component
    }

    /**
     * Defines a skeleton implementation of a constructor for `this` class.
     */
    @AsmKtDsl
    public fun defineConstructor(access: Int = Modifiers.PUBLIC, type: MethodType = MethodType.VOID): BytecodeMethod {
        require(type.returnType is PrimitiveType.Void) { "return type of a constructor must be 'void', was '$type'." }
        return defineMethod("<init>", access, type)
    }

    /**
     * Defines a skeleton implementation of a `static` block for `this` class.
     */
    @AsmKtDsl
    public fun defineStaticInit(): BytecodeMethod = defineMethod("<clinit>", Modifiers.STATIC, MethodType.VOID)

    /**
     * Defines a basic constructor for `this` class with the given [access] that just invokes the no-arg constructor
     * if its [superType].
     */
    @AsmKtDsl
    public fun defineDefaultConstructor(
        access: Int = Modifiers.PUBLIC,
    ): BytecodeMethod = defineConstructor(access) {
        loadThis()
        invokeConstructor(this@BytecodeClass.superType)
        returnValue()
    }

    /**
     * Defines a constructor for `this` class that will throw a [UnsupportedOperationException] with the given
     * [message] when its invoked.
     */
    @AsmKtDsl
    public fun defineInaccessibleConstructor(
        access: Int = Modifiers.PRIVATE,
        message: String = "No $simpleName instances for you!",
    ): BytecodeMethod = defineConstructor(access) {
        loadThis()
        throwException(ReferenceType<UnsupportedOperationException>(), message)
        returnValue()
    }

    /**
     * Defines a skeleton implementation of the `equals` method for `this` class.
     */
    @AsmKtDsl
    public fun defineEquals(isFinal: Boolean = false): BytecodeMethod {
        val flags = if (isFinal) Modifiers.PUBLIC_FINAL else Modifiers.PUBLIC
        return defineMethod("equals", flags, MethodType.ofBoolean(OBJECT))
    }

    /**
     * Defines a skeleton implementation of the `hashCode` method for `this` class.
     */
    @AsmKtDsl
    public fun defineHashCode(isFinal: Boolean = false): BytecodeMethod {
        val flags = if (isFinal) Modifiers.PUBLIC_FINAL else Modifiers.PUBLIC
        return defineMethod("hashCode", flags, MethodType.INT)
    }

    /**
     * Defines a skeleton implementation of the `toString` method for `this` class.
     */
    @AsmKtDsl
    public fun defineToString(isFinal: Boolean = false): BytecodeMethod {
        val flags = if (isFinal) Modifiers.PUBLIC_FINAL else Modifiers.PUBLIC
        return defineMethod("toString", flags, MethodType.STRING)
    }

    public fun toByteArray(): ByteArray {
        check()
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val node = toClassNode()
        node.accept(writer)
        return writer.toByteArray()
    }

    private fun check() {
        for (method in methods) {
            if (method.isAbstract && !(isAbstract || isInterface)) {
                requireOneKindOf(BytecodeClassKind.haveAbstractMethods, "abstract methods")
            }
            if (method.isEmpty() && !method.isAbstract) {
                throw IllegalArgumentException("No instructions defined for non-abstract method '${method.toComponentString()}' in '$className'.")
            }
            if (method.isNotEmpty() && method.isAbstract) {
                throw IllegalArgumentException("Abstract method '${method.toComponentString()}' in '$className' contains instructions")
            }
        }
    }

    private fun toClassNode(): ClassNode {
        val node = ClassNode()

        node.version = version.opcode
        node.access = kind.applyTo(access) or if (treatSuperSpecially) Modifiers.SUPER else 0
        node.name = internalName
        node.superName = superType.internalName
        node.interfaces = interfaces.mapTo(mutableListOf()) { it.internalName }

        node.sourceFile = sourceFile
        node.sourceDebug = sourceDebug

        node.outerClass = enclosingClass?.internalName
        node.outerMethod = enclosingMethod?.name
        node.outerMethodDesc = enclosingMethod?.type?.descriptor

        node.module = module?.toNode()

        for ((innerName, clz) in innerClasses) {
            node.innerClasses.add(InnerClassNode(clz.internalName, internalName, innerName, clz.access))
        }

        for (method in methods) {
            node.methods.add(method.toMethodNode())
        }

        for ((_, field) in fields) {
            node.fields.add(field.toNode())
        }

        node.permittedSubclasses = permittedSubtypes
            .mapTo(mutableListOf()) { it.internalName }
            .ifEmpty { null }

        node.recordComponents = recordComponents
            .mapTo(mutableListOf(), BytecodeRecordComponent::toNode)
            .ifEmpty { null }

        node.visibleAnnotations = visibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }
        node.invisibleAnnotations = invisibleAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::node)
            .ifEmpty { null }

        node.visibleTypeAnnotations = visibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }
        node.invisibleTypeAnnotations = invisibleTypeAnnotations
            .mapTo(mutableListOf(), BytecodeAnnotation::asTypeNode)
            .ifEmpty { null }

        return node
    }

    private fun requireSuperType(superType: ReferenceType, feature: String) {
        require(this.superType == superType) { "$feature requires super type to be $superType, but super type was ${this.superType}." }
    }

    private fun requireKind(kind: BytecodeClassKind, feature: String) {
        require(this.kind == kind) { "Only a class of kind $kind can use $feature, but current kind is ${this.kind}" }
    }

    private fun requireOneKindOf(kinds: Set<BytecodeClassKind>, feature: String) {
        require(this.kind in kinds) { "Only classes of kind ${kinds.listOut()} are allowed to have $feature, but current kind is ${this.kind}" }
    }

    private fun requireNotKind(kind: BytecodeClassKind, feature: String) {
        require(this.kind != kind) { "Classes of kind $kind are not allowed to have $feature" }
    }

    private fun requireNotOneKindOf(kinds: Set<BytecodeClassKind>, feature: String) {
        require(this.kind !in kinds) { "Classes of kind ${this.kind} are not allowed to have $feature" }
    }

    private fun Set<BytecodeClassKind>.listOut(): String = buildString {
        val lastIndex = this@listOut.size - 1
        for ((i, kind) in this@listOut.withIndex()) {
            append(kind.name)
            if (i == lastIndex - 1) append(" or ")
            if (i < lastIndex - 1) append(", ")
        }
    }

    override fun toString(): String =
        "BytecodeClass(type='${type.className}', version=$version, access=$access, superType='${superType.className}', interfaces=$interfaces, sourceFile=$sourceFile, sourceDebug=$sourceDebug, enclosingClass=${enclosingClass?.type?.className})"
}