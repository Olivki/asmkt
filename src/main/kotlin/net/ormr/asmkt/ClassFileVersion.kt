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

import org.objectweb.asm.Opcodes

public enum class ClassFileVersion(public val opcode: Int) {
    RELEASE_1(Opcodes.V1_1),
    RELEASE_2(Opcodes.V1_2),
    RELEASE_3(Opcodes.V1_3),
    RELEASE_4(Opcodes.V1_4),
    RELEASE_5(Opcodes.V1_5),
    RELEASE_6(Opcodes.V1_6),
    RELEASE_7(Opcodes.V1_7),
    RELEASE_8(Opcodes.V1_8),
    RELEASE_9(Opcodes.V9),
    RELEASE_10(Opcodes.V10),
    RELEASE_11(Opcodes.V11),
    RELEASE_12(Opcodes.V12),
    RELEASE_13(Opcodes.V13),
    RELEASE_14(Opcodes.V14),
    RELEASE_15(Opcodes.V15),
    RELEASE_16(Opcodes.V16),
    RELEASE_17(Opcodes.V17),
    RELEASE_18(Opcodes.V18),
    RELEASE_19(Opcodes.V19),
    RELEASE_20(Opcodes.V20),
    RELEASE_21(Opcodes.V21),
    RELEASE_22(Opcodes.V22),
}