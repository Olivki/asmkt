/*
 * Copyright 2020 Oliver Berg
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

package moe.kanon.asmkt

import org.objectweb.asm.Opcodes

enum class BytecodeVersion(val opcode: Int) {
    JAVA_6(Opcodes.V1_6),
    JAVA_7(Opcodes.V1_7),
    JAVA_8(Opcodes.V1_8),
    JAVA_9(Opcodes.V9),
    JAVA_10(Opcodes.V10),
    JAVA_11(Opcodes.V11),
    JAVA_12(Opcodes.V12),
    JAVA_13(Opcodes.V13),
    JAVA_14(Opcodes.V14),
    JAVA_15(Opcodes.V15);
}