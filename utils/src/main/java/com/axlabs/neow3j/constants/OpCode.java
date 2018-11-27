package com.axlabs.neow3j.constants;

import com.axlabs.neow3j.utils.Numeric;

/**
 * This class represents a <b>subset</b> of NEO VM opcodes.
 * <br/>
 * Based on: https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs
 */
public enum OpCode {

    // Constants

    /**
     * An empty array of bytes is pushed onto the stack.
     */
    PUSH0((byte) 0x00),
    PUSHF((byte) 0x00),
    /**
     * 0x01-0x4B The next opcode bytes is data to be pushed onto the stack
     */
    PUSHBYTES1((byte) 0x01),
    PUSHBYTES33((byte) 0x21),
    PUSHBYTES64((byte) 0x40),
    PUSHBYTES75((byte) 0x4B),
    /**
     * The next byte contains the number of bytes to be pushed onto the stack.
     */
    PUSHDATA1((byte) 0x4C),
    /**
     * The next two bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA2((byte) 0x4D),
    /**
     * The next four bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA4((byte) 0x4E),
    /**
     * The number -1 is pushed onto the stack.
     */
    PUSHM1((byte) 0x4F),
    /**
     * The number 1 is pushed onto the stack.
     */
    PUSH1((byte) 0x51),
    PUSHT((byte) 0x51),
    /**
     * The number 2 is pushed onto the stack.
     */
    PUSH2((byte) 0x52),
    /**
     * The number 3 is pushed onto the stack.
     */
    PUSH3((byte) 0x53),
    /**
     * The number 4 is pushed onto the stack.
     */
    PUSH4((byte) 0x54),
    /**
     * The number 5 is pushed onto the stack.
     */
    PUSH5((byte) 0x55),
    /**
     * The number 6 is pushed onto the stack.
     */
    PUSH6((byte) 0x56),
    /**
     * The number 7 is pushed onto the stack.
     */
    PUSH7((byte) 0x57),
    /**
     * The number 8 is pushed onto the stack.
     */
    PUSH8((byte) 0x58),
    /**
     * The number 9 is pushed onto the stack.
     */
    PUSH9((byte) 0x59),
    /**
     * The number 10 is pushed onto the stack.
     */
    PUSH10((byte) 0x5A),
    /**
     * The number 11 is pushed onto the stack.
     */
    PUSH11((byte) 0x5B),
    /**
     * The number 12 is pushed onto the stack.
     */
    PUSH12((byte) 0x5C),
    /**
     * The number 13 is pushed onto the stack.
     */
    PUSH13((byte) 0x5D),
    /**
     * The number 14 is pushed onto the stack.
     */
    PUSH14((byte) 0x5E),
    /**
     * The number 15 is pushed onto the stack.
     */
    PUSH15((byte) 0x5F),
    /**
     * The number 16 is pushed onto the stack.
     */
    PUSH16((byte) 0x60),

    // Crypto

    /**
     * The input is hashed using SHA-1.
     */
    SHA1((byte) 0xA7),
    /**
     * The input is hashed using SHA-256.
     */
    SHA256((byte) 0xA8),
    /**
     * The input is hashed using Hash160: first with SHA-256 and then with RIPEMD-160.
     */
    HASH160((byte) 0xA9),
    /**
     * The input is hashed using Hash256: twice with SHA-256.
     */
    HASH256((byte) 0xAA),
    /**
     * The publickey and signature are taken from main stack.
     * <br/>
     * Verifies if transaction was signed by given publickey and a boolean output is put on top of the main stack.
     */
    CHECKSIG((byte) 0xAC),
    /**
     * The publickey, signature and message are taken from main stack.
     * <br/>
     * Verifies if given message was signed by given publickey and a boolean output is put on top of the main stack.
     */
    VERIFY((byte) 0xAD),
    /**
     * A set of n public keys (an array or value n followed by n pubkeys) is validated
     * against a set of m signatures (an array or value m followed by m signatures).
     * <br/>
     * Verify transaction as multisig and a boolean output is put on top of the main stack.
     */
    CHECKMULTISIG((byte) 0xAE);

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
