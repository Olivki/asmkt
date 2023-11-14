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

import net.ormr.asmkt.AnnotationValue.*
import net.ormr.asmkt.type.TypeWithInternalName

// TODO: add support for adding annotations to the array

@AsmKtDsl
@JvmName("addString")
public fun AnnotationArrayBuilder<ForString>.add(value: String) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addBoolean")
public fun AnnotationArrayBuilder<ForBoolean>.add(value: Boolean) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addChar")
public fun AnnotationArrayBuilder<ForChar>.add(value: Char) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addByte")
public fun AnnotationArrayBuilder<ForByte>.add(value: Byte) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addShort")
public fun AnnotationArrayBuilder<ForShort>.add(value: Short) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addInt")
public fun AnnotationArrayBuilder<ForInt>.add(value: Int) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addLong")
public fun AnnotationArrayBuilder<ForLong>.add(value: Long) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addFloat")
public fun AnnotationArrayBuilder<ForFloat>.add(value: Float) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addDouble")
public fun AnnotationArrayBuilder<ForDouble>.add(value: Double) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addClass")
public fun AnnotationArrayBuilder<ForClass>.add(value: TypeWithInternalName) {
    add(value.asValue())
}

@AsmKtDsl
@JvmName("addEnum")
public fun <E : Enum<E>> AnnotationArrayBuilder<ForEnum<E>>.add(value: E) {
    add(value.asValue())
}