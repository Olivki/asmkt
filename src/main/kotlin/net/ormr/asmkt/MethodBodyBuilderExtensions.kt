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

import net.ormr.asmkt.type.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Pushes the instructions needed to compute the bitwise negation of the value currently at the top of the stack.
 *
 * Equivalent to the code `x ^ 1` where `x` is a boolean.
 */
@AsmKtDsl
public fun MethodBodyBuilder.not() {
    withCodeChunk {
        iconst_1()
        ixor()
    }
}

// -- BLOCKS -- \\
public sealed interface BlockBuilderContext {
    /**
     * A label that points to the exit from the current block to its parent.
     */
    public val exitLabel: LabelElement
}

@PublishedApi
internal class BlockBuilderContextImpl(override val exitLabel: LabelElement) : BlockBuilderContext

// TODO: re-enable inlining of the builders once context receivers are more stable
//       right now there's an issue when inlining the functions where the order of the
//       function parameters gets put in the wrong order, aka
//       "context(BlockBuilderContext) MethodBodyBuilder.() -> Unit" should accept parameters
//       in the order "(MethodBodyBuilder, BlockBuilderContext)" but it's expecting them as
//       "(BlockBuilderContext, MethodBodyBuilder)" when it gets compiled for the JVM
//       which means we fail with a ClassCastException at runtime.
//       They work fine if we do not inline.

@AsmKtDsl
public /*inline*/ fun MethodBodyBuilder.block(
    blockBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
) {
    contract {
        callsInPlace(blockBuilder, InvocationKind.EXACTLY_ONCE)
    }

    val exitLabel = newLabel()
    val context = BlockBuilderContextImpl(exitLabel)
    val body = newChild()
    withChildBuilder(body) { blockBuilder(context, this) }
    bindLabel(exitLabel)
}

@AsmKtDsl
public /*inline*/ fun MethodBodyBuilder.ifThen(
    thenBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
) {
    contract {
        callsInPlace(thenBuilder, InvocationKind.EXACTLY_ONCE)
    }

    val exitLabel = newLabel()
    val context = BlockBuilderContextImpl(exitLabel)
    val thenBody = newChild()
    ifFalse(thenBody.endLabel)
    withChildBuilder(thenBody) { thenBuilder(context, this) }
    bindLabel(exitLabel)
}

@AsmKtDsl
public /*inline*/ fun MethodBodyBuilder.ifThenElse(
    thenBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    elseBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
) {
    contract {
        callsInPlace(thenBuilder, InvocationKind.EXACTLY_ONCE)
        callsInPlace(elseBuilder, InvocationKind.EXACTLY_ONCE)
    }

    val exitLabel = newLabel()
    val context = BlockBuilderContextImpl(exitLabel)
    val thenBody = newChild()
    val elseBody = newChild()
    ifFalse(elseBody.startLabel)
    withChildBuilder(thenBody) {
        thenBuilder(context, this)
        if (thenBody.codeChunk.isReachable) {
            goto(exitLabel)
        }
    }
    withChildBuilder(elseBody) { elseBuilder(context, this) }
    bindLabel(exitLabel)
}

// -- TRYING -- \\
public sealed interface TryingBuilderContext {
    @AsmKtDsl
    public fun MethodBodyBuilder.catching(
        exception: ReferenceType,
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    )

    @AsmKtDsl
    public fun MethodBodyBuilder.catchingMulti(
        exceptions: List<ReferenceType>,
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    )

    @AsmKtDsl
    public fun MethodBodyBuilder.catchingAll(
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    )
}

@PublishedApi
internal class TryingBuilderContextImpl(
    val tryBody: MethodBodyBuilder,
    val exitLabel: LabelElement,
) : TryingBuilderContext {
    private val registeredExceptions: MutableSet<ReferenceType> = hashSetOf()
    private var currentCatchBody: MethodBodyBuilder? = null

    @AsmKtDsl
    override fun MethodBodyBuilder.catching(
        exception: ReferenceType,
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    ) {
        this.catchingMulti(listOf(exception), catchBuilder)
    }

    @AsmKtDsl
    override fun MethodBodyBuilder.catchingMulti(
        exceptions: List<ReferenceType>,
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    ) {
        val oldBody = currentCatchBody
        if (oldBody == null) {
            if (tryBody.codeChunk.isReachable) {
                goto(exitLabel)
            }
        }

        for (exception in exceptions) {
            require(exception !in registeredExceptions) { "Exception (${exception.asString()}) is already handled" }
            registeredExceptions += exception
        }

        if (oldBody != null) {
            oldBody.markEnd()
            addBody(oldBody.build())
            if (oldBody.codeChunk.isReachable) {
                goto(exitLabel)
            }
        }

        val catchContext = BlockBuilderContextImpl(exitLabel)
        val catchBody = newChild()
        val tryStart = tryBody.startLabel
        val tryEnd = tryBody.endLabel
        if (exceptions.isNotEmpty()) {
            for (exception in exceptions) {
                catchBody.tryCatch(tryStart, tryEnd, catchBody.startLabel, exception)
            }
        } else {
            catchBody.tryCatch(tryStart, tryEnd, catchBody.startLabel, exceptionType = null)
        }
        catchBody.markStart()
        catchBuilder(catchContext, catchBody)
    }

    @AsmKtDsl
    override fun MethodBodyBuilder.catchingAll(
        catchBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    ) {
        catchingMulti(emptyList(), catchBuilder)
    }

    @PublishedApi
    internal fun closeContext(builder: MethodBodyBuilder) {
        val block = currentCatchBody
        if (block != null) {
            block.markEnd()
            builder.addBody(block.build())
        }
        builder.bindLabel(exitLabel)
    }
}

@AsmKtDsl
public /*inline*/ fun MethodBodyBuilder.trying(
    tryBuilder: context(BlockBuilderContext) MethodBodyBuilder.() -> Unit,
    catchesBuilder: TryingBuilderContext.() -> Unit,
) {
    contract {
        callsInPlace(tryBuilder, InvocationKind.EXACTLY_ONCE)
        callsInPlace(catchesBuilder, InvocationKind.EXACTLY_ONCE)
    }

    val exitLabel = newLabel()
    val tryContext = BlockBuilderContextImpl(exitLabel)
    val tryBody = newChild()
    withChildBuilder(tryBody) { tryBuilder(tryContext, tryBody) }
    require(tryBody.codeChunk.isNotEmpty()) { "'try' block body may not be empty" }

    val catchContext = TryingBuilderContextImpl(tryBody, exitLabel)
    catchesBuilder(catchContext)
    catchContext.closeContext(this)
}


// -- SELF INVOKE -- \\
@AsmKtDsl
public fun MethodBodyBuilder.invokeSelfStatic(name: String, type: MethodType) {
    invokeStatic(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.invokeSelfSpecial(name: String, type: MethodType) {
    invokeSpecial(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.invokeSelfVirtual(name: String, type: MethodType) {
    invokeVirtual(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.invokeSelfInterface(name: String, type: MethodType) {
    invokeInterface(method.ownerType, name, type)
}

// -- SELF FIELD -- \\
@AsmKtDsl
public fun MethodBodyBuilder.getSelfStaticField(name: String, type: FieldType) {
    getStaticField(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.setSelfStaticField(name: String, type: FieldType) {
    setStaticField(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.getSelfField(name: String, type: FieldType) {
    getField(method.ownerType, name, type)
}

@AsmKtDsl
public fun MethodBodyBuilder.setSelfField(name: String, type: FieldType) {
    setField(method.ownerType, name, type)
}

// -- MISC -- \\
/**
 * Pushes the instructions required to create a new array of type [type] with size [size] onto the stack.
 *
 * @param [type] the type of the array
 * @param [size] the size of the array
 *
 * @throws [IllegalArgumentException] if [size] is negative
 */
@AsmKtDsl
public fun MethodBodyBuilder.newArray(type: FieldType, size: Int) {
    require(size >= 0) { "Size ($size) must not be negative" }

    pushInt(size)
    newArray(type)
}

/**
 * Pushes the instructions required to push the `this` pointer onto the stack.
 *
 * This is equivalent to `loadLocal(0, method.ownerType)`, but will fail if `method` is not static.
 *
 * @throws [IllegalArgumentException] if [method][MethodBodyBuilder.method] is not static
 */
@AsmKtDsl
public fun MethodBodyBuilder.loadThis() {
    require(!method.isStatic) { "Can't load 'this' for a static method" }

    loadLocal(0, method.ownerType)
}

/**
 * Pushes the instructions required to invoke the constructor of [owner] onto the stack.
 *
 * @param [owner] the type to invoke the constructor of
 * @param [type] the type of the constructor
 *
 * @throws [IllegalArgumentException] if the [returnType][MethodType.returnType] of [type] is not [void][VoidType]
 */
@AsmKtDsl
public fun MethodBodyBuilder.invokeConstructor(owner: ReferenceType, type: MethodType) {
    require(type.returnType is VoidType) { "Return type (${type.returnType.asString()}) must be void" }

    invokeSpecial(owner, "<init>", type)
}

// -- BOXING -- \\
/**
 * Pushes the instructions required to box the value currently at the top of the stack using the `valueOf` functions
 * of the boxed variants of the [type] onto the stack.
 *
 * If `type` is a [void][VoidType] then a `null` value cast to `java.lang.Void` will be pushed onto the stack.
 *
 * @param [type] the type to box
 */
@AsmKtDsl
public fun MethodBodyBuilder.box(type: PrimitiveType) {
    when (type) {
        is FieldType -> {
            val boxedType = type.box()
            invokeStatic(boxedType, "valueOf", MethodType(boxedType, type))
        }
        is VoidType -> {
            // A boxed 'void' would be the 'java.lang.Void' class, but we can't create instances of that class
            // so a 'null' cast to 'java.lang.Void' is the best we'll get
            checkCast(ReferenceType.VOID)
            pushNull()
        }
    }
}

/**
 * Pushes the instructions required to unbox the value currently at the top of the stack to the given [type].
 *
 * @param [type] the type to unbox the top stack value to
 */
@AsmKtDsl
public fun MethodBodyBuilder.unbox(type: PrimitiveFieldType) {
    val boxedType = type.box()
    checkCast(boxedType)
    invokeVirtual(boxedType, "${type.simpleName}Value", MethodType(type))
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Byte.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedByte(value: Byte) {
    pushByte(value)
    box(ByteType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Short.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedShort(value: Short) {
    pushShort(value)
    box(ShortType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Integer.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedInt(value: Int) {
    pushInt(value)
    box(IntType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Long.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedLong(value: Long) {
    pushLong(value)
    box(LongType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Float.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedFloat(value: Float) {
    pushFloat(value)
    box(FloatType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Double.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedDouble(value: Double) {
    pushDouble(value)
    box(DoubleType)
}

/**
 * Pushes the [value] onto the stack and then pushes an instruction to invoke [valueOf][java.lang.Character.valueOf],
 * producing a boxed variant of `value`.
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedChar(value: Char) {
    pushChar(value)
    box(CharType)
}

/**
 * Pushes the `TRUE` constant from the [Boolean] class onto the stack, or the `FALSE` constant depending on the given
 * [value].
 *
 * @see [pushBoxedTrue]
 * @see [pushBoxedFalse]
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedBoolean(value: Boolean) {
    if (value) {
        pushBoxedTrue()
    } else {
        pushBoxedFalse()
    }
}

/**
 * Pushes the `TRUE` constant from the [Boolean] class onto the stack.
 *
 * @see [pushBoxedBoolean]
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedTrue() {
    getStaticField(ReferenceType.BOOLEAN, "TRUE", ReferenceType.BOOLEAN)
}

/**
 * Pushes the `FALSE` constant from the [Boolean] class onto the stack.
 *
 * @see [pushBoxedBoolean]
 */
@AsmKtDsl
public fun MethodBodyBuilder.pushBoxedFalse() {
    getStaticField(ReferenceType.BOOLEAN, "FALSE", ReferenceType.BOOLEAN)
}

// -- INTERNAL -- \\
@PublishedApi
internal inline fun MethodBodyBuilder.withChildBuilder(
    body: MethodBodyBuilder,
    block: MethodBodyBuilder.() -> Unit,
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    body.markStart()
    block(body)
    body.markEnd()
    addBody(body.build())
}