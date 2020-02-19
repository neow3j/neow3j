package io.neow3j.constants;

import io.neow3j.utils.Numeric;

/**
 * This class represents a <b>subset</b> of NEO VM opcodes.
 * <br>
 * Based on: https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs
 */
public enum OpCode {

    //region Constants

    PUSHINT8(0x00, 30),
    PUSHINT16(0x01, 30),
    PUSHINT32(0x02, 30),
    PUSHINT64(0x03, 30),
    PUSHINT128(0x04, 120),
    PUSHINT256(0x05, 120),

    /**
     * Convert the next four bytes to an address, and push the address onto the stack.
     */
    PUSHA(0x0A, 120),

    /**
     * The item "null" is pushed onto the stack.
     */
    PUSHNULL(0x0B, 30),

    /**
     * The next 1 byte contains the number of bytes to be pushed onto the stack.
     */
    PUSHDATA1(0x0C, 180),

    /**
     * The next 2 bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA2(0x0D, 13000),

    /**
     * The next 4 bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA4(0x0E, 110000),

    /**
     * The number -1 is pushed onto the stack.
     */
    PUSHM1(0x0F, 30),

    /**
     * The number 0 is pushed onto the stack.
     */
    PUSH0(0x10, 30),

    /**
     * The number 1 is pushed onto the stack.
     */
    PUSH1(0x11, 30),

    /**
     * The number 2 is pushed onto the stack.
     */
    PUSH2(0x12, 30),

    /**
     * The number 3 is pushed onto the stack.
     */
    PUSH3(0x13, 30),

    /**
     * The number 4 is pushed onto the stack.
     */
    PUSH4(0x14, 30),

    /**
     * The number 5 is pushed onto the stack.
     */
    PUSH5(0x15, 30),

    /**
     * The number 6 is pushed onto the stack.
     */
    PUSH6(0x16, 30),

    /**
     * The number 7 is pushed onto the stack.
     */
    PUSH7(0x17, 30),

    /**
     * The number 8 is pushed onto the stack.
     */
    PUSH8(0x18, 30),

    /**
     * The number 9 is pushed onto the stack.
     */
    PUSH9(0x19, 30),

    /**
     * The number 10 is pushed onto the stack.
     */
    PUSH10(0x1A, 30),

    /**
     * The number 11 is pushed onto the stack.
     */
    PUSH11(0x1B, 30),

    /**
     * The number 12 is pushed onto the stack.
     */
    PUSH12(0x1C, 30),

    /**
     * The number 13 is pushed onto the stack.
     */
    PUSH13(0x1D, 30),

    /**
     * The number 14 is pushed onto the stack.
     */
    PUSH14(0x1E, 30),

    /**
     * The number 15 is pushed onto the stack.
     */
    PUSH15(0x1F, 30),

    /**
     * The number 16 is pushed onto the stack.
     */
    PUSH16(0x20, 30),

    //endregion

    //region Flow control

    /**
     * The NOP operation does nothing. It is intended to fill in space if opcodes are patched.
     */
    NOP(0x21, 30),

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is
     * represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMP(0x22, 70),

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is
     * represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMP_L(0x23, 70),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero.
     * The target instruction is represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    JMPIF(0x24, 70),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero.
     * The target instruction is represented as a 4-bytes signed offset from the beginning of the
     * current instruction.
     */
    JMPIF_L(0x25, 70),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or
     * zero. The target instruction is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    JMPIFNOT(0x26, 70),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or
     * zero. The target instruction is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    JMPIFNOT_L(0x27, 70),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is
     * represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPEQ(0x28, 70),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is
     * represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPEQ_L(0x29, 70),

    /**
     * Transfers control to a target instruction when two values are not equal. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current
     * instruction.
     */
    JMPNE(0x2A, 70),

    /**
     * Transfers control to a target instruction when two values are not equal. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current
     * instruction.
     */
    JMPNE_L(0x2B, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than the second
     * value. The target instruction is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    JMPGT(0x2C, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than the second
     * value. The target instruction is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    JMPGT_L(0x2D, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the
     * second value. The target instruction is represented as a 1-byte signed offset from the
     * beginning of the current instruction.
     */
    JMPGE(0x2E, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the
     * second value. The target instruction is represented as a 4-bytes signed offset from the
     * beginning of the current instruction.
     */
    JMPGE_L(0x2F, 70),

    /**
     * Transfers control to a target instruction if the first value is less than the second value.
     * The target instruction is represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    JMPLT(0x30, 70),

    /**
     * Transfers control to a target instruction if the first value is less than the second value.
     * The target instruction is represented as a 4-bytes signed offset from the beginning of the
     * current instruction.
     */
    JMPLT_L(0x31, 70),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the
     * second value. The target instruction is represented as a 1-byte signed offset from the
     * beginning of the current instruction.
     */
    JMPLE(0x32, 70),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the
     * second value. The target instruction is represented as a 4-bytes signed offset from the
     * beginning of the current instruction.
     */
    JMPLE_L(0x33, 70),

    /**
     * Calls the function at the target address which is represented as a 1-byte signed offset from
     * the beginning of the current instruction.
     */
    CALL(0x34, 22000),

    /**
     * Calls the function at the target address which is represented as a 4-bytes signed offset from
     * the beginning of the current instruction.
     */
    CALL_L(0x35,22000),

    /**
     * Pop the address of a function from the stack, and call the function.
     */
    CALLA(0x36, 22000),

    THROW(0x37, 30),
    THROWIF(0x38, 30),
    THROWIFNOT(0x39, 30),

    /**
     * Returns from the current method.
     */
    RET(0x40, 0),

    /**
     * Calls to an interop service.
     */
    SYSCALL(0x41, 0),

    //endregion

    //region Compound-type

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put
     * inside n-sized array and this array is put on top of the main stack.
     */
    PACK(0xC0, 7000);

    //endregion


    private byte opCode;
    private Long price;

    OpCode(int opCode, int price) {
        this.opCode = (byte)opCode;
        this.price = (long) price;
    }

    public byte getValue() {
        return opCode;
    }

    public long getPrice() {
        return this.price;
    }

    public static String toHexString(OpCode opCode) {
        return Numeric.toHexStringNoPrefix(opCode.getValue());
    }

    public static OpCode valueOf(byte code) {
        for (OpCode c : OpCode.values()) {
            if (c.opCode == code) {
                return c;
            }
        }
        throw new IllegalArgumentException("No Opcode found for byte value " +
                Numeric.toHexString(code) + ".");
    }

    @Override
    public String toString() {
        return Numeric.toHexStringNoPrefix(this.getValue());
    }
}
