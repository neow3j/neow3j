package io.neow3j.constants;

import io.neow3j.utils.Numeric;

/**
 * <p>This class represents a <b>subset</b> of NEO VM opcodes.</p>
 * <br>
 * <p>Based on: https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs</p>
 */
public enum OpCode {

    //region Constants

    /**
     * An empty array of bytes is pushed onto the stack.
     */
    PUSH0((byte) 0x00, 30),

    /**
     * Alias for {@link OpCode#PUSH0}
     */
    PUSHF((byte) 0x00, 30),

    /**
     * <p>0x01-0x4B The next opcode bytes is data to be pushed onto the stack.</p>
     */
    PUSHBYTES1((byte) 0x01, 120),
    PUSHBYTES4((byte) 0x04, 120),
    PUSHBYTES20((byte) 0x14, 120),
    PUSHBYTES33((byte) 0x21, 120),
    PUSHBYTES64((byte) 0x40, 120),
    PUSHBYTES75((byte) 0x4B, 120),
    /**
     * <p>The next byte contains the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA1((byte) 0x4C, 180),
    /**
     * <p>The next two bytes contain the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA2((byte) 0x4D, 13000),
    /**
     * <p>The next four bytes contain the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA4((byte) 0x4E, 110000),
    /**
     * <p>The number -1 is pushed onto the stack.</p>
     */
    PUSHM1((byte) 0x4F, 30),
    /**
     * <p>The number 1 is pushed onto the stack.</p>
     */
    PUSH1((byte) 0x51, 30),
    PUSHT((byte) 0x51, 30),
    /**
     * <p>The number 2 is pushed onto the stack.</p>
     */
    PUSH2((byte) 0x52, 30),
    /**
     * <p>The number 3 is pushed onto the stack.</p>
     */
    PUSH3((byte) 0x53, 30),
    /**
     * <p>The number 4 is pushed onto the stack.</p>
     */
    PUSH4((byte) 0x54, 30),
    /**
     * <p>The number 5 is pushed onto the stack.</p>
     */
    PUSH5((byte) 0x55, 30),
    /**
     * <p>The number 6 is pushed onto the stack.</p>
     */
    PUSH6((byte) 0x56, 30),
    /**
     * <p>The number 7 is pushed onto the stack.</p>
     */
    PUSH7((byte) 0x57, 30),
    /**
     * <p>The number 8 is pushed onto the stack.</p>
     */
    PUSH8((byte) 0x58, 30),
    /**
     * <p>The number 9 is pushed onto the stack.</p>
     */
    PUSH9((byte) 0x59, 30),
    /**
     * <p>The number 10 is pushed onto the stack.</p>
     */
    PUSH10((byte) 0x5A, 30),
    /**
     * <p>The number 11 is pushed onto the stack.</p>
     */
    PUSH11((byte) 0x5B, 30),
    /**
     * <p>The number 12 is pushed onto the stack.</p>
     */
    PUSH12((byte) 0x5C, 30),
    /**
     * <p>The number 13 is pushed onto the stack.</p>
     */
    PUSH13((byte) 0x5D, 30),
    /**
     * <p>The number 14 is pushed onto the stack.</p>
     */
    PUSH14((byte) 0x5E, 30),
    /**
     * <p>The number 15 is pushed onto the stack.</p>
     */
    PUSH15((byte) 0x5F, 30),
    /**
     * <p>The number 16 is pushed onto the stack.</p>
     */
    PUSH16((byte) 0x60, 30),
    /**
     * <p>A value n is taken from top of main stack. The next n items on main stack are removed,
     * put inside n-sized array and this array is put on top of the main stack.</p>
     */
    PACK((byte) 0xC1, 7000),

    //endregion

    //region Flow control

    /**
     * <p>No operation. Nothing is done.</p>
     */
    NOP((byte) 0x61, 30),
    /**
     * <p>Reads a 2-byte value n and a jump is performed to relative position n-3.</p>
     */
    JMP((byte) 0x62, 70),
    /**
     * <p>A boolean value b is taken from main stack and reads a 2-byte value n,
     * if b is True then a jump is performed to relative position n-3.</p>
     */
    JMPIF((byte) 0x63, 70),
    /**
     * <p>A boolean value b is taken from main stack and reads a 2-byte value n,
     * if b is False then a jump is performed to relative position n-3.</p>
     */
    JMPIFNOT((byte) 0x64, 70),
    /**
     * <p>Current context is copied to the invocation stack.
     * Reads a 2-byte value n and a jump is performed to relative position n-3.</p>
     */
    CALL((byte) 0x65, 22000),
    /**
     * <p>Stops the execution if invocation stack is empty.</p>
     */
    RET((byte) 0x66, 40),
    /**
     * <p>Reads a string and executes the corresponding operation.</p>
     */
    SYSCALL((byte) 0x68, 0);

    //endregion

    private byte opCode;
    private Long price;

    OpCode(byte opCode, int price) {
        this.opCode = opCode;
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
