package io.neow3j.script;

import java.lang.annotation.Annotation;

import static io.neow3j.utils.Numeric.toHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;

/**
 * This enum contains a <b>subset</b> of NeoVM opcodes.
 * <p>
 * See <a href="https://github.com/neo-project/neo-vm/blob/master/src/Neo.VM/OpCode.cs">here</a> for a complete list.
 */
public enum OpCode {

//region Constants

    // Push a signed integer of the given bit length in its two's complement and little-endian order.
    @OperandSize(size = 1)
    PUSHINT8(0x00, 1),

    @OperandSize(size = 2)
    PUSHINT16(0x01, 1),

    @OperandSize(size = 4)
    PUSHINT32(0x02, 1),

    @OperandSize(size = 8)
    PUSHINT64(0x03, 1),

    @OperandSize(size = 16)
    PUSHINT128(0x04, 1 << 2),

    @OperandSize(size = 32)
    PUSHINT256(0x05, 1 << 2),

    /**
     * Pushes the boolean value {@code true} onto the stack.
     */
    PUSHT(0x08, 1),

    /**
     * Pushes the boolean value {@code false} onto the stack.
     */
    PUSHF(0x09, 1),

    /**
     * Convert the next four bytes to an address, and push the address onto the stack.
     */
    @OperandSize(size = 4)
    PUSHA(0x0A, 1 << 2),

    /**
     * The item "null" is pushed onto the stack.
     */
    PUSHNULL(0x0B, 1),

    /**
     * The next 1 byte contains the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 1)
    PUSHDATA1(0x0C, 1 << 3),

    /**
     * The next 2 bytes contain the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 2)
    PUSHDATA2(0x0D, 1 << 9),

    /**
     * The next 4 bytes contain the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 4)
    PUSHDATA4(0x0E, 1 << 12),

    /**
     * The number -1 is pushed onto the stack.
     */
    PUSHM1(0x0F, 1),

    /**
     * The number 0 is pushed onto the stack.
     */
    PUSH0(0x10, 1),

    /**
     * The number 1 is pushed onto the stack.
     */
    PUSH1(0x11, 1),

    /**
     * The number 2 is pushed onto the stack.
     */
    PUSH2(0x12, 1),

    /**
     * The number 3 is pushed onto the stack.
     */
    PUSH3(0x13, 1),

    /**
     * The number 4 is pushed onto the stack.
     */
    PUSH4(0x14, 1),

    /**
     * The number 5 is pushed onto the stack.
     */
    PUSH5(0x15, 1),

    /**
     * The number 6 is pushed onto the stack.
     */
    PUSH6(0x16, 1),

    /**
     * The number 7 is pushed onto the stack.
     */
    PUSH7(0x17, 1),

    /**
     * The number 8 is pushed onto the stack.
     */
    PUSH8(0x18, 1),

    /**
     * The number 9 is pushed onto the stack.
     */
    PUSH9(0x19, 1),

    /**
     * The number 10 is pushed onto the stack.
     */
    PUSH10(0x1A, 1),

    /**
     * The number 11 is pushed onto the stack.
     */
    PUSH11(0x1B, 1),

    /**
     * The number 12 is pushed onto the stack.
     */
    PUSH12(0x1C, 1),

    /**
     * The number 13 is pushed onto the stack.
     */
    PUSH13(0x1D, 1),

    /**
     * The number 14 is pushed onto the stack.
     */
    PUSH14(0x1E, 1),

    /**
     * The number 15 is pushed onto the stack.
     */
    PUSH15(0x1F, 1),

    /**
     * The number 16 is pushed onto the stack.
     */
    PUSH16(0x20, 1),

//endregion

//region Flow control

    /**
     * The NOP operation does nothing. It is intended to fill in space if opcodes are patched.
     */
    NOP(0x21, 1),

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is represented as a 1-byte
     * signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMP(0x22, 1 << 1),

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is represented as a 4-bytes
     * signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMP_L(0x23, 1 << 1),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPIF(0x24, 1 << 1),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPIF_L(0x25, 1 << 1),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or zero. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPIFNOT(0x26, 1 << 1),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or zero. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPIFNOT_L(0x27, 1 << 1),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is represented as a
     * 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPEQ(0x28, 1 << 1),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is represented as a
     * 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPEQ_L(0x29, 1 << 1),

    /**
     * Transfers control to a target instruction when two values are not equal. The target instruction is represented
     * as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPNE(0x2A, 1 << 1),

    /**
     * Transfers control to a target instruction when two values are not equal. The target instruction is represented
     * as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPNE_L(0x2B, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is greater than the second value. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPGT(0x2C, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is greater than the second value. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPGT_L(0x2D, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the second value. The
     * target instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPGE(0x2E, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the second value. The
     * target instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPGE_L(0x2F, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is less than the second value. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPLT(0x30, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is less than the second value. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPLT_L(0x31, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the second value. The
     * target instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPLE(0x32, 1 << 1),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the second value. The
     * target instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPLE_L(0x33, 1 << 1),

    /**
     * Calls the function at the target address which is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 1)
    CALL(0x34, 1 << 9),

    /**
     * Calls the function at the target address which is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 4)
    CALL_L(0x35, 1 << 9),

    /**
     * Pop the address of a function from the stack, and call the function.
     */
    CALLA(0x36, 1 << 9),

    /**
     * Calls the function which is described by the token.
     */
    @OperandSize(size = 2)
    CALLT(0x37, 1 << 15),

    /**
     * Turn the vm state to FAULT immediately, and cannot be caught.
     */
    ABORT(0x38, 0),

    /**
     * Pop the top value of the stack, if it's false, then exit vm execution and set VM state to FAULT.
     */
    ASSERT(0x39, 1),

    /**
     * Pop the top value of the stack, and throw it.
     */
    THROW(0x3A, 1 << 9),

    /**
     * TRY CatchOffset(sbyte) FinallyOffset(sbyte). If there's no catch body, set CatchOffset 0. If there's no
     * finally body, set FinallyOffset 0.
     */
    @OperandSize(size = 2)
    TRY(0x3B, 1 << 2),

    /**
     * TRY_L CatchOffset(int) FinallyOffset(int). If there's no catch body, set CatchOffset 0. If there's no finally
     * body, set FinallyOffset 0.
     */
    @OperandSize(size = 8)
    TRY_L(0x3C, 1 << 2),

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then unconditionally transfers
     * control to the specific target instruction, represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 1)
    ENDTRY(0x3D, 1 << 2),

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then unconditionally transfers
     * control to the specific target instruction, represented as a 4-byte signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 4)
    ENDTRY_L(0x3E, 1 << 2),

    /**
     * End finally, If no exception happen or be catched, vm will jump to the target instruction of ENDTRY/ENDTRY_L.
     * Otherwise, the VM will rethrow the exception to upper layer.
     */
    ENDFINALLY(0x3F, 1 << 2),

    /**
     * Returns from the current method.
     */
    RET(0x40, 0),

    /**
     * Calls to an interop service.
     */
    @OperandSize(size = 4)
    SYSCALL(0x41, 0),

//endregion

//region Stack

    /**
     * Puts the number of stack items onto the stack.
     */
    DEPTH(0x43, 1 << 1),

    /**
     * Removes the top stack item.
     */
    DROP(0x45, 1 << 1),

    /**
     * Removes the second-to-top stack item.
     */
    NIP(0x46, 1 << 1),

    /**
     * The item n back in the main stack is removed.
     */
    XDROP(0x48, 1 << 4),

    /**
     * Clears the stack.
     */
    CLEAR(0x49, 1 << 4),

    /**
     * Duplicates the top stack item.
     */
    DUP(0x4A, 1 << 1),

    /**
     * Copies the second-to-top stack item to the top.
     */
    OVER(0x4B, 1 << 1),

    /**
     * The item n back in the stack is copied to the top.
     */
    PICK(0x4D, 1 << 1),

    /**
     * The item at the top of the stack is copied and inserted before the second-to-top item.
     */
    TUCK(0x4E, 1 << 1),

    /**
     * The top two items on the stack are swapped.
     */
    SWAP(0x50, 1 << 1),

    /**
     * The top three items on the stack are rotated to the left.
     */
    ROT(0x51, 1 << 1),

    /**
     * The item n back in the stack is moved to the top.
     */
    ROLL(0x52, 1 << 4),

    /**
     * Reverse the order of the top 3 items on the stack.
     */
    REVERSE3(0x53, 1 << 1),

    /**
     * Reverse the order of the top 4 items on the stack.
     */
    REVERSE4(0x54, 1 << 1),

    /**
     * Pop the number N on the stack, and reverse the order of the top N items on the stack.
     */
    REVERSEN(0x55, 1 << 4),

//endregion

//region Slot

    /**
     * Initialize the static field list for the current execution context.
     */
    @OperandSize(size = 1)
    INITSSLOT(0x56, 1 << 4),

    /**
     * Initialize the argument slot and the local variable list for the current execution context.
     */
    @OperandSize(size = 2)
    INITSLOT(0x57, 1 << 6),

    /**
     * Loads the static field at index 0 onto the evaluation stack.
     */
    LDSFLD0(0x58, 1 << 1),

    /**
     * Loads the static field at index 1 onto the evaluation stack.
     */
    LDSFLD1(0x59, 1 << 1),

    /**
     * Loads the static field at index 2 onto the evaluation stack.
     */
    LDSFLD2(0x5A, 1 << 1),

    /**
     * Loads the static field at index 3 onto the evaluation stack.
     */
    LDSFLD3(0x5B, 1 << 1),

    /**
     * Loads the static field at index 4 onto the evaluation stack.
     */
    LDSFLD4(0x5C, 1 << 1),

    /**
     * Loads the static field at index 5 onto the evaluation stack.
     */
    LDSFLD5(0x5D, 1 << 1),

    /**
     * Loads the static field at index 6 onto the evaluation stack.
     */
    LDSFLD6(0x5E, 1 << 1),

    /**
     * Loads the static field at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    @OperandSize(size = 1)
    LDSFLD(0x5F, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 0.
     */
    STSFLD0(0x60, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 1.
     */
    STSFLD1(0x61, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 2.
     */
    STSFLD2(0x62, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 3.
     */
    STSFLD3(0x63, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 4.
     */
    STSFLD4(0x64, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 5.
     */
    STSFLD5(0x65, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 6.
     */
    STSFLD6(0x66, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the static field list at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STSFLD(0x67, 1 << 1),

    /**
     * Loads the local variable at index 0 onto the evaluation stack.
     */
    LDLOC0(0x68, 1 << 1),

    /**
     * Loads the local variable at index 1 onto the evaluation stack.
     */
    LDLOC1(0x69, 1 << 1),

    /**
     * Loads the local variable at index 2 onto the evaluation stack.
     */
    LDLOC2(0x6A, 1 << 1),

    /**
     * Loads the local variable at index 3 onto the evaluation stack.
     */
    LDLOC3(0x6B, 1 << 1),

    /**
     * Loads the local variable at index 4 onto the evaluation stack.
     */
    LDLOC4(0x6C, 1 << 1),

    /**
     * Loads the local variable at index 5 onto the evaluation stack.
     */
    LDLOC5(0x6D, 1 << 1),

    /**
     * Loads the local variable at index 6 onto the evaluation stack.
     */
    LDLOC6(0x6E, 1 << 1),

    /**
     * Loads the local variable at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    @OperandSize(size = 1)
    LDLOC(0x6F, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 0.
     */
    STLOC0(0x70, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 1.
     */
    STLOC1(0x71, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 2.
     */
    STLOC2(0x72, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 3.
     */
    STLOC3(0x73, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 4.
     */
    STLOC4(0x74, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 5.
     */
    STLOC5(0x75, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 6.
     */
    STLOC6(0x76, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STLOC(0x77, 1 << 1),

    /**
     * Loads the argument at index 0 onto the evaluation stack.
     */
    LDARG0(0x78, 1 << 1),

    /**
     * Loads the argument at index 1 onto the evaluation stack.
     */
    LDARG1(0x79, 1 << 1),

    /**
     * Loads the argument at index 2 onto the evaluation stack.
     */
    LDARG2(0x7A, 1 << 1),

    /**
     * Loads the argument at index 3 onto the evaluation stack.
     */
    LDARG3(0x7B, 1 << 1),

    /**
     * Loads the argument at index 4 onto the evaluation stack.
     */
    LDARG4(0x7C, 1 << 1),

    /**
     * Loads the argument at index 5 onto the evaluation stack.
     */
    LDARG5(0x7D, 1 << 1),

    /**
     * Loads the argument at index 6 onto the evaluation stack.
     */
    LDARG6(0x7E, 1 << 1),

    /**
     * Loads the argument at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    @OperandSize(size = 1)
    LDARG(0x7F, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 0.
     */
    STARG0(0x80, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 1.
     */
    STARG1(0x81, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 2.
     */
    STARG2(0x82, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 3.
     */
    STARG3(0x83, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 4.
     */
    STARG4(0x84, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 5.
     */
    STARG5(0x85, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 6.
     */
    STARG6(0x86, 1 << 1),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STARG(0x87, 1 << 1),

//endregion

//region Splice

    /**
     * Creates a new Buffer and pushes it onto the stack.
     */
    NEWBUFFER(0x88, 1 << 8),

    /**
     * Copies a range of bytes from one Buffer to another.
     */
    MEMCPY(0x89, 1 << 11),

    /**
     * Concatenates two strings.
     */
    CAT(0x8B, 1 << 11),

    /**
     * Returns a section of a string.
     */
    SUBSTR(0x8C, 1 << 11),

    /**
     * Keeps only characters left of the specified point in a string.
     */
    LEFT(0x8D, 1 << 11),

    /**
     * Keeps only characters right of the specified point in a string.
     */
    RIGHT(0x8E, 1 << 11),

//endregion

//region Bitwise logic

    /**
     * Flips all bits in the input.
     */
    INVERT(0x90, 1 << 2),

    /**
     * Boolean and between each bit in the inputs.
     */
    AND(0x91, 1 << 3),

    /**
     * Boolean or between each bit in the inputs.
     */
    OR(0x92, 1 << 3),

    /**
     * Boolean exclusive or between each bit in the inputs.
     */
    XOR(0x93, 1 << 3),

    /**
     * Returns 1 if the inputs are exactly equal, 0 otherwise.
     */
    EQUAL(0x97, 1 << 5),

    /**
     * Returns 1 if the inputs are not equal, 0 otherwise.
     */
    NOTEQUAL(0x98, 1 << 5),

//endregion

//region Arithmetic

    /**
     * Puts the sign of top stack item on top of the main stack.
     * <p>If the value is negative, put -1; if positive, put 1; if zero, put 0.
     */
    SIGN(0x99, 1 << 2),

    /**
     * The input is made positive.
     */
    ABS(0x9A, 1 << 2),

    /**
     * The sign of the input is flipped.
     */
    NEGATE(0x9B, 1 << 2),

    /**
     * 1 is added to the input.
     */
    INC(0x9C, 1 << 2),

    /**
     * 1 is subtracted from the input.
     */
    DEC(0x9D, 1 << 2),

    /**
     * a is added to b.
     */
    ADD(0x9E, 1 << 3),

    /**
     * b is subtracted from a.
     */
    SUB(0x9F, 1 << 3),

    /**
     * a is multiplied by b.
     */
    MUL(0xA0, 1 << 3),

    /**
     * a is divided by b.
     */
    DIV(0xA1, 1 << 3),

    /**
     * Returns the remainder after dividing a by b.
     */
    MOD(0xA2, 1 << 3),

    /**
     * Takes two elements of the stack and raises the first one to the power of the second one.
     */
    POW(0xA3, 1 << 6),

    /**
     * Returns the square root of a specified number.
     */
    SQRT(0xA4, 1 << 11),

    /**
     * Performs modulus division on a number multiplied by another number.
     */
    MODMUL(0xA5, 1 << 5),

    /**
     * Performs modulus division on a number raised to the power of another number. If the exponent is -1, it will
     * have the calculation of the modular inverse.
     */
    MODPOW(0xA6, 1 << 11),

    /**
     * Shifts a left b bits, preserving sign.
     */
    SHL(0xA8, 1 << 3),

    /**
     * Shifts a right b bits, preserving sign.
     */
    SHR(0xA9, 1 << 3),

    /**
     * If the input is 0 or 1, it is flipped. Otherwise, the output will be 0.
     */
    NOT(0xAA, 1 << 2),

    /**
     * If both a and b are not 0, the output is 1. Otherwise, 0.
     */
    BOOLAND(0xAB, 1 << 3),

    /**
     * If a or b is not 0, the output is 1. Otherwise, 0.
     */
    BOOLOR(0xAC, 1 << 3),

    /**
     * Returns 0 if the input is 0. 1 otherwise.
     */
    NZ(0xB1, 1 << 2),

    /**
     * Returns 1 if the numbers are equal, 0 otherwise.
     */
    NUMEQUAL(0xB3, 1 << 3),

    /**
     * Returns 1 if the numbers are not equal, 0 otherwise.
     */
    NUMNOTEQUAL(0xB4, 1 << 3),

    /**
     * Returns 1 if a is less than b, 0 otherwise.
     */
    LT(0xB5, 1 << 3),

    /**
     * Returns 1 if a is less than or equal to b, 0 otherwise.
     */
    LE(0xB6, 1 << 3),

    /**
     * Returns 1 if a is greater than b, 0 otherwise.
     */
    GT(0xB7, 1 << 3),

    /**
     * Returns 1 if a is greater than or equal to b, 0 otherwise.
     */
    GE(0xB8, 1 << 3),

    /**
     * Returns the smallest of a and b.
     */
    MIN(0xB9, 1 << 3),

    /**
     * Returns the largest of a and b.
     */
    MAX(0xBA, 1 << 3),

    /**
     * Returns 1 if x is within the specified range (left-inclusive), 0 otherwise.
     */
    WITHIN(0xBB, 1 << 3),

//endregion

//region Compound-type

    /**
     * A value n is taken from top of main stack. The next n*2 items on main stack are removed, put inside n-sized
     * map and this map is put on top of the main stack.
     */
    PACKMAP(0xBE, 1 << 11),

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put inside n-sized
     * struct and this struct is put on top of the main stack.
     */
    PACKSTRUCT(0xBF, 1 << 11),

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put inside n-sized
     * array and this array is put on top of the main stack.
     */
    PACK(0xC0, 1 << 11),

    /**
     * An array or map is removed from top of the main stack. Its elements are put on top of the main stack (in
     * reverse order). In case of a map, the key-value pairs are flattened, i.e., the value of the first pair is
     * pushed, then the key of the first pair, then the value of the second item and so on.
     */
    UNPACK(0xC1, 1 << 11),

    /**
     * An empty array (with size 0) is put on top of the main stack.
     */
    NEWARRAY0(0xC2, 1 << 4),

    /**
     * A value n is taken from top of main stack. A null-filled array with size n is put on top of
     * the main stack.
     */
    NEWARRAY(0xC3, 1 << 9),

    /**
     * A value n is taken from top of main stack. An array of type T with size n is put on top of the main stack.
     */
    @OperandSize(size = 1)
    NEWARRAY_T(0xC4, 1 << 9),

    /**
     * An empty struct (with size 0) is put on top of the main stack.
     */
    NEWSTRUCT0(0xC5, 1 << 4),

    /**
     * A value n is taken from top of main stack. A zero-filled struct with size n is put on top of the main stack.
     */
    NEWSTRUCT(0xC6, 1 << 9),

    /**
     * A Map is created and put on top of the main stack.
     */
    NEWMAP(0xC8, 1 << 3),

    /**
     * An array is removed from top of the main stack. Its size is put on top of the main stack.
     */
    SIZE(0xCA, 1 << 2),

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack.
     * <p>
     * Puts True on top of main stack if array[n] (or map[n]) exist, and False otherwise.
     */
    HASKEY(0xCB, 1 << 6),

    /**
     * A map is taken from top of the main stack. The keys of this map are put on top of the main stack.
     */
    KEYS(0xCC, 1 << 4),

    /**
     * A map is taken from top of the main stack. The values of this map are put on top of the main stack.
     */
    VALUES(0xCD, 1 << 13),

    /**
     * An input index n (or key) and an array (or map) are taken from main stack. Element array[n] (or map[n]) is put
     * on top of the main stack.
     */
    PICKITEM(0xCE, 1 << 6),

    /**
     * The item on top of main stack is removed and appended to the second item on top of the main stack.
     */
    APPEND(0xCF, 1 << 13),

    /**
     * A value v, index n (or key) and an array (or map) are taken from main stack. Attribution array[n]=v (or
     * map[n]=v) is performed.
     */
    SETITEM(0xD0, 1 << 13),

    /**
     * An array is removed from the top of the main stack and its elements are reversed.
     */
    REVERSEITEMS(0xD1, 1 << 13),

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack. Element array[n]
     * (or map[n]) is removed.
     */
    REMOVE(0xD2, 1 << 4),

    /**
     * Remove all the items from the compound-type.
     */
    CLEARITEMS(0xD3, 1 << 4),

//endregion

//region Types

    /**
     * Returns true if the input is null. Returns false otherwise.
     */
    ISNULL(0xD8, 1 << 1),

    /**
     * Returns true if the top item is of the specified type.
     */
    @OperandSize(size = 1)
    ISTYPE(0xD9, 1 << 1),

    /**
     * Converts the top item to the specified type.
     */
    @OperandSize(size = 1)
    CONVERT(0xDB, 1 << 13);

//endregion

    private int opcode;
    private Long price;
    private static OpCode[] opcodes = new OpCode[220];

    static {
        for (OpCode code : values()) {
            opcodes[code.opcode] = code;
        }
    }

    OpCode(int opcode, int price) {
        this.opcode = opcode;
        this.price = (long) price;
    }

    public int getCode() {
        return opcode;
    }

    public long getPrice() {
        return this.price;
    }

    public static OpCode get(byte opcode) {
        return get(Byte.toUnsignedInt(opcode));
    }

    public static OpCode get(int opcode) {
        if (opcode < 0 || opcode > 219) {
            return null;
        }
        return opcodes[opcode];
    }

    public static OpCode valueOf(byte code) {
        for (OpCode c : OpCode.values()) {
            if (c.opcode == code) {
                return c;
            }
        }
        throw new IllegalArgumentException(format("No Opcode found for byte value %s.", toHexString(code)));
    }

    @Override
    public String toString() {
        return toHexStringNoPrefix((byte) this.getCode());
    }

    /**
     * Gets the {@link OperandSize} annotation for the given opcode.
     *
     * @param code The opcode to get the annotation for.
     * @return the annotation, or null if the given opcode is not annotated.
     */
    public static OperandSize getOperandSize(OpCode code) {
        try {
            Annotation[] annotations = OpCode.class.getField(code.name()).getAnnotations();
            if (annotations.length == 0) {
                return null;
            }
            if (annotations[0].annotationType() != OperandSize.class) {
                throw new IllegalStateException("Unsupported annotation on OpCode.");
            }
            return (OperandSize) annotations[0];
        } catch (NoSuchFieldException ignore) {
            // Does not happen.
            return null;
        }
    }

}
