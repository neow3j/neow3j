package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

public class ECPoint {

    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BYTE_STRING_CODE)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = 0x21) // 33 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public ECPoint(byte[] value) {

    }

    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.SIZE)
    @Instruction(opcode = OpCode.PUSHINT8, operand = 0x21) // 33 bytes expected array size
    @Instruction(opcode = OpCode.NUMEQUAL)
    @Instruction(opcode = OpCode.ASSERT)
    public ECPoint(String value) {

    }

    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public native byte[] toByteArray(ECPoint value);

    @Instruction
    public native String toString(ECPoint value);

}

