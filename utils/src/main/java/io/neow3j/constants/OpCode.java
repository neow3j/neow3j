package io.neow3j.constants;

import io.neow3j.utils.Numeric;
import java.lang.annotation.Annotation;

/**
 * This enum contains a <b>subset</b> of NEO VM opcodes.
 * <p>
 * See <a href="https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs">here</a> for
 * a complete list.
 */
public enum OpCode {

//region Constants

    // Push a signed integer of the given bit length in its two's complement and little-endian
    // order.
    @OperandSize(size = 1)
    PUSHINT8(0x00, 30),

    @OperandSize(size = 2)
    PUSHINT16(0x01, 30),

    @OperandSize(size = 4)
    PUSHINT32(0x02, 30),

    @OperandSize(size = 8)
    PUSHINT64(0x03, 30),

    @OperandSize(size = 16)
    PUSHINT128(0x04, 120),

    @OperandSize(size = 32)
    PUSHINT256(0x05, 120),

    /**
     * Convert the next four bytes to an address, and push the address onto the stack.
     */
    @OperandSize(size = 4)
    PUSHA(0x0A, 120),

    /**
     * The item "null" is pushed onto the stack.
     */
    PUSHNULL(0x0B, 30),

    /**
     * The next 1 byte contains the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 1)
    PUSHDATA1(0x0C, 180),

    /**
     * The next 2 bytes contain the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 2)
    PUSHDATA2(0x0D, 13000),

    /**
     * The next 4 bytes contain the number of bytes to be pushed onto the stack.
     */
    @OperandSize(prefixSize = 4)
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
    @OperandSize(size = 1)
    JMP(0x22, 70),

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is
     * represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMP_L(0x23, 70),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero.
     * The target instruction is represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 1)
    JMPIF(0x24, 70),

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero.
     * The target instruction is represented as a 4-bytes signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 4)
    JMPIF_L(0x25, 70),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or
     * zero. The target instruction is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 1)
    JMPIFNOT(0x26, 70),

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or
     * zero. The target instruction is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 4)
    JMPIFNOT_L(0x27, 70),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is
     * represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPEQ(0x28, 70),

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is
     * represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPEQ_L(0x29, 70),

    /**
     * Transfers control to a target instruction when two values are not equal. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current
     * instruction.
     */
    @OperandSize(size = 1)
    JMPNE(0x2A, 70),

    /**
     * Transfers control to a target instruction when two values are not equal. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current
     * instruction.
     */
    @OperandSize(size = 4)
    JMPNE_L(0x2B, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than the second
     * value. The target instruction is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 1)
    JMPGT(0x2C, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than the second
     * value. The target instruction is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    @OperandSize(size = 4)
    JMPGT_L(0x2D, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the
     * second value. The target instruction is represented as a 1-byte signed offset from the
     * beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPGE(0x2E, 70),

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the
     * second value. The target instruction is represented as a 4-bytes signed offset from the
     * beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPGE_L(0x2F, 70),

    /**
     * Transfers control to a target instruction if the first value is less than the second value.
     * The target instruction is represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 1)
    JMPLT(0x30, 70),

    /**
     * Transfers control to a target instruction if the first value is less than the second value.
     * The target instruction is represented as a 4-bytes signed offset from the beginning of the
     * current instruction.
     */
    @OperandSize(size = 4)
    JMPLT_L(0x31, 70),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the
     * second value. The target instruction is represented as a 1-byte signed offset from the
     * beginning of the current instruction.
     */
    @OperandSize(size = 1)
    JMPLE(0x32, 70),

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the
     * second value. The target instruction is represented as a 4-bytes signed offset from the
     * beginning of the current instruction.
     */
    @OperandSize(size = 4)
    JMPLE_L(0x33, 70),

    /**
     * Calls the function at the target address which is represented as a 1-byte signed offset from
     * the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    CALL(0x34, 22000),

    /**
     * Calls the function at the target address which is represented as a 4-bytes signed offset from
     * the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    CALL_L(0x35, 22000),

    /**
     * Pop the address of a function from the stack, and call the function.
     */
    CALLA(0x36, 22000),

    /**
     * Turn the vm state to FAULT immediately, and cannot be caught.
     */
    ABORT(0x37, 30),

    /**
     * Pop the top value of the stack, if it false, then exit vm execution and set vm state to
     * FAULT.
     */
    ASSERT(0x38, 30),

    /**
     * Pop the top value of the stack, and throw it.
     */
    THROW(0x3A, 30),

    /**
     * TRY CatchOffset(sbyte) FinallyOffset(sbyte). If there's no catch body, set CatchOffset 0. If
     * there's no finally body, set FinallyOffset 0.
     */
    @OperandSize(size = 2)
    TRY(0x3B, 100),

    /**
     * TRY_L CatchOffset(int) FinallyOffset(int). If there's no catch body, set CatchOffset 0. If
     * there's no finally body, set FinallyOffset 0.
     */
    @OperandSize(size = 8)
    TRY_L(0x3C, 100),

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then
     * unconditionally transfers control to the specific target instruction, represented as a 1-byte
     * signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 1)
    ENDTRY(0x3D, 100),

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then
     * unconditionally transfers control to the specific target instruction, represented as a 4-byte
     * signed offset from the beginning of the current instruction.
     */
    @OperandSize(size = 4)
    ENDTRY_L(0x3E, 100),

    /**
     * End finally, If no exception happen or be catched, vm will jump to the target instruction of
     * ENDTRY/ENDTRY_L. Otherwise vm will rethrow the exception to upper layer.
     */
    ENDFINALLY(0x3F, 100),

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
    DEPTH(0x43, 60),

    /**
     * Removes the top stack item.
     */
    DROP(0x45, 60),

    /**
     * Removes the second-to-top stack item.
     */
    NIP(0x46, 60),

    /**
     * The item n back in the main stack is removed.
     */
    XDROP(0x48, 400),

    /**
     * Clear the stack
     */
    CLEAR(0x49, 400),

    /**
     * Duplicates the top stack item.
     */
    DUP(0x4A, 60),

    /**
     * Copies the second-to-top stack item to the top.
     */
    OVER(0x4B, 60),

    /**
     * The item n back in the stack is copied to the top.
     */
    PICK(0x4D, 60),

    /**
     * The item at the top of the stack is copied and inserted before the second-to-top item.
     */
    TUCK(0x4E, 60),

    /**
     * The top two items on the stack are swapped.
     */
    SWAP(0x50, 60),

    /**
     * The top three items on the stack are rotated to the left.
     */
    ROT(0x51, 60),

    /**
     * The item n back in the stack is moved to the top.
     */
    ROLL(0x52, 400),

    /**
     * Reverse the order of the top 3 items on the stack.
     */
    REVERSE3(0x53, 60),

    /**
     * Reverse the order of the top 4 items on the stack.
     */
    REVERSE4(0x54, 60),

    /**
     * Pop the number N on the stack, and reverse the order of the top N items on the stack.
     */
    REVERSEN(0x55, 400),

//endregion

//region Slot

    /**
     * Initialize the static field list for the current execution context.
     */
    @OperandSize(size = 1)
    INITSSLOT(0x56, 400),

    /**
     * Initialize the argument slot and the local variable list for the current execution context.
     */
    @OperandSize(size = 2)
    INITSLOT(0x57, 800),

    /**
     * Loads the static field at index 0 onto the evaluation stack.
     */
    LDSFLD0(0x58, 60),

    /**
     * Loads the static field at index 1 onto the evaluation stack.
     */
    LDSFLD1(0x59, 60),

    /**
     * Loads the static field at index 2 onto the evaluation stack.
     */
    LDSFLD2(0x5A, 60),

    /**
     * Loads the static field at index 3 onto the evaluation stack.
     */
    LDSFLD3(0x5B, 60),

    /**
     * Loads the static field at index 4 onto the evaluation stack.
     */
    LDSFLD4(0x5C, 60),

    /**
     * Loads the static field at index 5 onto the evaluation stack.
     */
    LDSFLD5(0x5D, 60),

    /**
     * Loads the static field at index 6 onto the evaluation stack.
     */
    LDSFLD6(0x5E, 60),

    /**
     * Loads the static field at a specified index onto the evaluation stack. The index is
     * represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    LDSFLD(0x5F, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 0.
     */
    STSFLD0(0x60, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 1.
     */
    STSFLD1(0x61, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 2.
     */
    STSFLD2(0x62, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 3.
     */
    STSFLD3(0x63, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 4.
     */
    STSFLD4(0x64, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 5.
     */
    STSFLD5(0x65, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 6.
     */
    STSFLD6(0x66, 60),

    /**
     * Stores the value on top of the evaluation stack in the static field list at a specified
     * index. The index is represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STSFLD(0x67, 60),

    /**
     * Loads the local variable at index 0 onto the evaluation stack.
     */
    LDLOC0(0x68, 60),

    /**
     * Loads the local variable at index 1 onto the evaluation stack.
     */
    LDLOC1(0x69, 60),

    /**
     * Loads the local variable at index 2 onto the evaluation stack.
     */
    LDLOC2(0x6A, 60),

    /**
     * Loads the local variable at index 3 onto the evaluation stack.
     */
    LDLOC3(0x6B, 60),

    /**
     * Loads the local variable at index 4 onto the evaluation stack.
     */
    LDLOC4(0x6C, 60),

    /**
     * Loads the local variable at index 5 onto the evaluation stack.
     */
    LDLOC5(0x6D, 60),

    /**
     * Loads the local variable at index 6 onto the evaluation stack.
     */
    LDLOC6(0x6E, 60),

    /**
     * Loads the local variable at a specified index onto the evaluation stack. The index is
     * represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    LDLOC(0x6F, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 0.
     */
    STLOC0(0x70, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 1.
     */
    STLOC1(0x71, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 2.
     */
    STLOC2(0x72, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 3.
     */
    STLOC3(0x73, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 4.
     */
    STLOC4(0x74, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 5.
     */
    STLOC5(0x75, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 6.
     */
    STLOC6(0x76, 60),

    /**
     * Stores the value on top of the evaluation stack in the local variable list at a specified
     * index. The index is represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STLOC(0x77, 60),

    /**
     * Loads the argument at index 0 onto the evaluation stack.
     */
    LDARG0(0x78, 60),

    /**
     * Loads the argument at index 1 onto the evaluation stack.
     */
    LDARG1(0x79, 60),

    /**
     * Loads the argument at index 2 onto the evaluation stack.
     */
    LDARG2(0x7A, 60),

    /**
     * Loads the argument at index 3 onto the evaluation stack.
     */
    LDARG3(0x7B, 60),

    /**
     * Loads the argument at index 4 onto the evaluation stack.
     */
    LDARG4(0x7C, 60),

    /**
     * Loads the argument at index 5 onto the evaluation stack.
     */
    LDARG5(0x7D, 60),

    /**
     * Loads the argument at index 6 onto the evaluation stack.
     */
    LDARG6(0x7E, 60),

    /**
     * Loads the argument at a specified index onto the evaluation stack. The index is represented
     * as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    LDARG(0x7F, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 0.
     */
    STARG0(0x80, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 1.
     */
    STARG1(0x81, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 2.
     */
    STARG2(0x82, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 3.
     */
    STARG3(0x83, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 4.
     */
    STARG4(0x84, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 5.
     */
    STARG5(0x85, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 6.
     */
    STARG6(0x86, 60),

    /**
     * Stores the value on top of the evaluation stack in the argument slot at a specified index.
     * The index is represented as a 1-byte unsigned integer.
     */
    @OperandSize(size = 1)
    STARG(0x87, 60),

//endregion

//region Splice

    NEWBUFFER(0x88, 80000),

    MEMCPY(0x89, 80000),

    /**
     * Concatenates two strings.
     */
    CAT(0x8B, 80000),

    /**
     * Returns a section of a string.
     */
    SUBSTR(0x8C, 80000),

    /**
     * Keeps only characters left of the specified point in a string.
     */
    LEFT(0x8D, 80000),

    /**
     * Keeps only characters right of the specified point in a string.
     */
    RIGHT(0x8E, 80000),

//endregion

//region Bitwise logic

    /**
     * Flips all of the bits in the input.
     */
    INVERT(0x90, 100),

    /**
     * Boolean and between each bit in the inputs.
     */
    AND(0x91, 200),

    /**
     * Boolean or between each bit in the inputs.
     */
    OR(0x92, 200),

    /**
     * Boolean exclusive or between each bit in the inputs.
     */
    XOR(0x93, 200),

    /**
     * Returns 1 if the inputs are exactly equal, 0 otherwise.
     */
    EQUAL(0x97, 200),

    /**
     * Returns 1 if the inputs are not equal, 0 otherwise.
     */
    NOTEQUAL(0x98, 200),

//endregion

//region Arithmetic

    /**
     * Puts the sign of top stack item on top of the main stack. If value is negative, put -1; if
     * positive, put 1; if value is zero, put 0.
     */
    SIGN(0x99, 100),

    /**
     * The input is made positive.
     */
    ABS(0x9A, 100),

    /**
     * The sign of the input is flipped.
     */
    NEGATE(0x9B, 100),

    /**
     * 1 is added to the input.
     */
    INC(0x9C, 100),

    /**
     * 1 is subtracted from the input.
     */
    DEC(0x9D, 100),

    /**
     * a is added to b.
     */
    ADD(0x9E, 200),

    /**
     * b is subtracted from a.
     */
    SUB(0x9F, 200),

    /**
     * a is multiplied by b.
     */
    MUL(0xA0, 300),

    /**
     * a is divided by b.
     */
    DIV(0xA1, 300),

    /**
     * Returns the remainder after dividing a by b.
     */
    MOD(0xA2, 300),

    /**
     * Shifts a left b bits, preserving sign.
     */
    SHL(0xA8, 300),

    /**
     * Shifts a right b bits, preserving sign.
     */
    SHR(0xA9, 300),

    /**
     * If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
     */
    NOT(0xAA, 100),

    /**
     * If both a and b are not 0, the output is 1. Otherwise 0.
     */
    BOOLAND(0xAB, 200),

    /**
     * If a or b is not 0, the output is 1. Otherwise 0.
     */
    BOOLOR(0xAC, 200),

    /**
     * Returns 0 if the input is 0. 1 otherwise.
     */
    NZ(0xB1, 100),

    /**
     * Returns 1 if the numbers are equal, 0 otherwise.
     */
    NUMEQUAL(0xB3, 200),

    /**
     * Returns 1 if the numbers are not equal, 0 otherwise.
     */
    NUMNOTEQUAL(0xB4, 200),

    /**
     * Returns 1 if a is less than b, 0 otherwise.
     */
    LT(0xB5, 200),

    /**
     * Returns 1 if a is less than or equal to b, 0 otherwise.
     */
    LE(0xB6, 200),

    /**
     * Returns 1 if a is greater than b, 0 otherwise.
     */
    GT(0xB7, 200),

    /**
     * Returns 1 if a is greater than or equal to b, 0 otherwise.
     */
    GE(0xB8, 200),

    /**
     * Returns the smaller of a and b.
     */
    MIN(0xB9, 200),

    /**
     * Returns the larger of a and b.
     */
    MAX(0xBA, 200),

    /**
     * Returns 1 if x is within the specified range (left-inclusive), 0 otherwise.
     */
    WITHIN(0xBB, 200),

//endregion

//region Compound-type

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put
     * inside n-sized array and this array is put on top of the main stack.
     */
    PACK(0xC0, 7000),

    /**
     * An array is removed from top of the main stack. Its elements are put on top of the main stack
     * (in reverse order) and the array size is also put on main stack.
     */
    UNPACK(0xC1, 7000),

    /**
     * An empty array (with size 0) is put on top of the main stack.
     */
    NEWARRAY0(0xC2, 400),

    /**
     * A value n is taken from top of main stack. A null-filled array with size n is put on top of
     * the main stack.
     */
    NEWARRAY(0xC3, 15000),

    /**
     * A value n is taken from top of main stack. An array of type T with size n is put on top of
     * the main stack.
     */
    @OperandSize(size = 1)
    NEWARRAY_T(0xC4, 15000),

    /**
     * An empty struct (with size 0) is put on top of the main stack.
     */
    NEWSTRUCT0(0xC5, 400),

    /**
     * A value n is taken from top of main stack. A zero-filled struct with size n is put on top of
     * the main stack.
     */
    NEWSTRUCT(0xC6, 15000),

    /**
     * A Map is created and put on top of the main stack.
     */
    NEWMAP(0xC8, 200),

    /**
     * An array is removed from top of the main stack. Its size is put on top of the main stack.
     */
    SIZE(0xCA, 150),

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack.
     * Puts True on top of main stack if array[n] (or map[n]) exist, and False otherwise.
     */
    HASKEY(0xCB, 270_000),

    /**
     * A map is taken from top of the main stack. The keys of this map are put on top of the main
     * stack.
     */
    KEYS(0xCC, 500),

    /**
     * A map is taken from top of the main stack. The values of this map are put on top of the main
     * stack.
     */
    VALUES(0xCD, 7000),

    /**
     * An input index n (or key) and an array (or map) are taken from main stack. Element array[n]
     * (or map[n]) is put on top of the main stack.
     */
    PICKITEM(0xCE, 270000),

    /**
     * The item on top of main stack is removed and appended to the second item on top of the main
     * stack.
     */
    APPEND(0xCF, 15000),

    /**
     * A value v, index n (or key) and an array (or map) are taken from main stack. Attribution
     * array[n]=v (or map[n]=v) is performed.
     */
    SETITEM(0xD0, 270000),

    /**
     * An array is removed from the top of the main stack and its elements are reversed.
     */
    REVERSEITEMS(0xD1, 500),

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack.
     * Element array[n] (or map[n]) is removed.
     */
    REMOVE(0xD2, 500),

    /**
     * Remove all the items from the compound-type.
     */
    CLEARITEMS(0xD3, 400),

//endregion

//region Types

    /**
     * Returns true if the input is null. Returns false otherwise.
     */
    ISNULL(0xD8, 60),

    /**
     * Returns true if the top item is of the specified type.
     */
    @OperandSize(size = 1)
    ISTYPE(0xD9, 60),

    /**
     * Converts the top item to the specified type.
     */
    @OperandSize(size = 1)
    CONVERT(0xDB, 80000);

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
        throw new IllegalArgumentException("No Opcode found for byte value " +
                Numeric.toHexString(code) + ".");
    }

    @Override
    public String toString() {
        return Numeric.toHexStringNoPrefix((byte) this.getCode());
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
