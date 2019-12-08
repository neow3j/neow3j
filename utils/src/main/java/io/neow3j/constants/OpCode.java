package io.neow3j.constants;

import io.neow3j.utils.Numeric;

/**
 * <p>This class represents a <b>subset</b> of NEO VM opcodes.</p>
 * <br>
 * <p>Based on: https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs</p>
 */
public enum OpCode {

    // Constants

    /**
     * <p>An empty array of bytes is pushed onto the stack.</p>
     */
    PUSH0((byte) 0x00),
    PUSHF((byte) 0x00),
    /**
     * <p>0x01-0x4B The next opcode bytes is data to be pushed onto the stack.</p>
     */
    PUSHBYTES1((byte) 0x01),
    PUSHBYTES33((byte) 0x21),
    PUSHBYTES64((byte) 0x40),
    PUSHBYTES75((byte) 0x4B),
    /**
     * <p>The next byte contains the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA1((byte) 0x4C),
    /**
     * <p>The next two bytes contain the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA2((byte) 0x4D),
    /**
     * <p>The next four bytes contain the number of bytes to be pushed onto the stack.</p>
     */
    PUSHDATA4((byte) 0x4E),
    /**
     * <p>The number -1 is pushed onto the stack.</p>
     */
    PUSHM1((byte) 0x4F),
    /**
     * <p>The number 1 is pushed onto the stack.</p>
     */
    PUSH1((byte) 0x51),
    PUSHT((byte) 0x51),
    /**
     * <p>The number 2 is pushed onto the stack.</p>
     */
    PUSH2((byte) 0x52),
    /**
     * <p>The number 3 is pushed onto the stack.</p>
     */
    PUSH3((byte) 0x53),
    /**
     * <p>The number 4 is pushed onto the stack.</p>
     */
    PUSH4((byte) 0x54),
    /**
     * <p>The number 5 is pushed onto the stack.</p>
     */
    PUSH5((byte) 0x55),
    /**
     * <p>The number 6 is pushed onto the stack.</p>
     */
    PUSH6((byte) 0x56),
    /**
     * <p>The number 7 is pushed onto the stack.</p>
     */
    PUSH7((byte) 0x57),
    /**
     * <p>The number 8 is pushed onto the stack.</p>
     */
    PUSH8((byte) 0x58),
    /**
     * <p>The number 9 is pushed onto the stack.</p>
     */
    PUSH9((byte) 0x59),
    /**
     * <p>The number 10 is pushed onto the stack.</p>
     */
    PUSH10((byte) 0x5A),
    /**
     * <p>The number 11 is pushed onto the stack.</p>
     */
    PUSH11((byte) 0x5B),
    /**
     * <p>The number 12 is pushed onto the stack.</p>
     */
    PUSH12((byte) 0x5C),
    /**
     * <p>The number 13 is pushed onto the stack.</p>
     */
    PUSH13((byte) 0x5D),
    /**
     * <p>The number 14 is pushed onto the stack.</p>
     */
    PUSH14((byte) 0x5E),
    /**
     * <p>The number 15 is pushed onto the stack.</p>
     */
    PUSH15((byte) 0x5F),
    /**
     * <p>The number 16 is pushed onto the stack.</p>
     */
    PUSH16((byte) 0x60),


    // Flow control

    /**
     * <p>No operation. Nothing is done.</p>
     */
    NOP((byte) 0x61),
    /**
     * <p>Reads a 2-byte value n and a jump is performed to relative position n-3.</p>
     */
    JMP((byte) 0x62),
    /**
     * <p>A boolean value b is taken from main stack and reads a 2-byte value n,
     * if b is True then a jump is performed to relative position n-3.</p>
     */
    JMPIF((byte) 0x63),
    /**
     * <p>A boolean value b is taken from main stack and reads a 2-byte value n,
     * if b is False then a jump is performed to relative position n-3.</p>
     */
    JMPIFNOT((byte) 0x64),
    /**
     * <p>Current context is copied to the invocation stack.
     * Reads a 2-byte value n and a jump is performed to relative position n-3.</p>
     */
    CALL((byte) 0x65),
    /**
     * <p>Stops the execution if invocation stack is empty.</p>
     */
    RET((byte) 0x66),
    /**
     * <p>Reads a script hash and executes the corresponding contract.
     * If script hash is zero, performs dynamic invoke by taking script hash from main stack.</p>
     */
    APPCALL((byte) 0x67),
    /**
     * <p>Reads a string and executes the corresponding operation.</p>
     */
    SYSCALL((byte) 0x68),
    /**
     * <p>Reads a script hash and executes the corresponding contract.
     * If script hash is zero, performs dynamic invoke by taking script hash from main stack.
     * Disposes the top item on invocation stack.</p>
     */
    TAILCALL((byte) 0x69),


    // Crypto

    /**
     * <p>The input is hashed using SHA-1.</p>
     */
    SHA1((byte) 0xA7),
    /**
     * <p>The input is hashed using SHA-256.</p>
     */
    SHA256((byte) 0xA8),
    /**
     * <p>The input is hashed using Hash160: first with SHA-256 and then with RIPEMD-160.</p>
     */
    HASH160((byte) 0xA9),
    /**
     * <p>The input is hashed using Hash256: twice with SHA-256.</p>
     */
    HASH256((byte) 0xAA),
    /**
     * <p>The publickey and signature are taken from main stack.</p>
     * <br>
     * <p>Verifies if transaction was signed by given publickey and a boolean output is put on top of the main stack.</p>
     */
    CHECKSIG((byte) 0xAC),
    /**
     * <p>The publickey, signature and message are taken from main stack.</p>
     * <br>
     * <p>Verifies if given message was signed by given publickey and a boolean output is put on top of the main stack.</p>
     */
    VERIFY((byte) 0xAD),
    /**
     * <p>A set of n public keys (an array or value n followed by n pubkeys) is validated
     * against a set of m signatures (an array or value m followed by m signatures).</p>
     * <br>
     * <p>Verify transaction as multisig and a boolean output is put on top of the main stack.</p>
     */
    CHECKMULTISIG((byte) 0xAE),

    /**
     * <p>A value n is taken from top of main stack. The next n items on main stack are removed,
     * put inside n-sized array and this array is put on top of the main stack.</p>
     */
    PACK((byte) 0xC1);

    private byte opCode;

    OpCode(byte opCode) {
        this.opCode = opCode;
    }

    public byte getValue() {
        return opCode;
    }

    public static String toHexString(OpCode opCode) {
        return Numeric.toHexStringNoPrefix(opCode.getValue());
    }

    @Override
    public String toString() {
        return Numeric.toHexStringNoPrefix(this.getValue());
    }
}
