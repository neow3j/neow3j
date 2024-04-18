package io.neow3j.devpack.constants;

/**
 * NeoVM opcodes.
 * <p>
 * See the OpCode class in the NeoVM
 * <a href="https://github.com/neo-project/neo/blob/master/src/Neo.VM/OpCode.cs">C# implementation</a>
 * for the actual byte values of the opcodes.
 */
// This enum can also be found in the neow3j core module (io.neow3j.script.OpCode). Make sure to update it here and in
// the core, when things change.
public enum OpCode {

    // region Constants

    // Push a signed integer of the given bit length in its two's complement and little-endian order.
    PUSHINT8,
    PUSHINT16,
    PUSHINT32,
    PUSHINT64,
    PUSHINT128,
    PUSHINT256,

    /**
     * Pushes the boolean value {@code true} onto the stack.
     */
    PUSHT,

    /**
     * Pushes the boolean value {@code false} onto the stack.
     */
    PUSHF,

    /**
     * Convert the next four bytes to an address, and push the address onto the stack.
     */
    PUSHA,

    /**
     * The item "null" is pushed onto the stack.
     */
    PUSHNULL,

    /**
     * The next 1 byte contains the number of bytes to be pushed onto the stack.
     */
    PUSHDATA1,

    /**
     * The next 2 bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA2,

    /**
     * The next 4 bytes contain the number of bytes to be pushed onto the stack.
     */
    PUSHDATA4,

    /**
     * The number -1 is pushed onto the stack.
     */
    PUSHM1,

    /**
     * The number 0 is pushed onto the stack.
     */
    PUSH0,

    /**
     * The number 1 is pushed onto the stack.
     */
    PUSH1,

    /**
     * The number 2 is pushed onto the stack.
     */
    PUSH2,

    /**
     * The number 3 is pushed onto the stack.
     */
    PUSH3,

    /**
     * The number 4 is pushed onto the stack.
     */
    PUSH4,

    /**
     * The number 5 is pushed onto the stack.
     */
    PUSH5,

    /**
     * The number 6 is pushed onto the stack.
     */
    PUSH6,

    /**
     * The number 7 is pushed onto the stack.
     */
    PUSH7,

    /**
     * The number 8 is pushed onto the stack.
     */
    PUSH8,

    /**
     * The number 9 is pushed onto the stack.
     */
    PUSH9,

    /**
     * The number 10 is pushed onto the stack.
     */
    PUSH10,

    /**
     * The number 11 is pushed onto the stack.
     */
    PUSH11,

    /**
     * The number 12 is pushed onto the stack.
     */
    PUSH12,

    /**
     * The number 13 is pushed onto the stack.
     */
    PUSH13,

    /**
     * The number 14 is pushed onto the stack.
     */
    PUSH14,

    /**
     * The number 15 is pushed onto the stack.
     */
    PUSH15,

    /**
     * The number 16 is pushed onto the stack.
     */
    PUSH16,

    // endregion
    // region Flow control

    /**
     * The NOP operation does nothing. It is intended to fill in space if opcodes are patched.
     */
    NOP,

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is represented as a 1-byte
     * signed offset from the beginning of the current instruction.
     */
    JMP,

    /**
     * Unconditionally transfers control to a target instruction. The target instruction is represented as a 4-bytes
     * signed offset from the beginning of the current instruction.
     */
    JMP_L,

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPIF,

    /**
     * Transfers control to a target instruction if the value is "true", not "null", or non-zero. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPIF_L,

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or zero. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPIFNOT,

    /**
     * Transfers control to a target instruction if the value is "false", a "null" reference, or zero. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPIFNOT_L,

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is represented as a
     * 1-byte signed offset from the beginning of the current instruction.
     */
    JMPEQ,

    /**
     * Transfers control to a target instruction if two values are equal. The target instruction is represented as a
     * 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPEQ_L,

    /**
     * Transfers control to a target instruction when two values are not equal. The target instruction is represented
     * as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPNE,

    /**
     * Transfers control to a target instruction when two values are not equal. The target instruction is represented
     * as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPNE_L,

    /**
     * Transfers control to a target instruction if the first value is greater than the second value. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPGT,

    /**
     * Transfers control to a target instruction if the first value is greater than the second value. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPGT_L,

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the second value. The
     * target instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPGE,

    /**
     * Transfers control to a target instruction if the first value is greater than or equal to the second value. The
     * target instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPGE_L,

    /**
     * Transfers control to a target instruction if the first value is less than the second value. The target
     * instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPLT,

    /**
     * Transfers control to a target instruction if the first value is less than the second value. The target
     * instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPLT_L,

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the second value. The
     * target instruction is represented as a 1-byte signed offset from the beginning of the current instruction.
     */
    JMPLE,

    /**
     * Transfers control to a target instruction if the first value is less than or equal to the second value. The
     * target instruction is represented as a 4-bytes signed offset from the beginning of the current instruction.
     */
    JMPLE_L,

    /**
     * Calls the function at the target address which is represented as a 1-byte signed offset from the beginning of
     * the current instruction.
     */
    CALL,

    /**
     * Calls the function at the target address which is represented as a 4-bytes signed offset from the beginning of
     * the current instruction.
     */
    CALL_L,

    /**
     * Pop the address of a function from the stack, and call the function.
     */
    CALLA,

    /**
     * Calls the function which is described by the token.
     */
    CALLT,

    /**
     * Turns the vm state to FAULT immediately, and cannot be caught.
     */
    ABORT,

    /**
     * Pops the top value of the stack. If it's false, exits the vm execution and sets the vm state to FAULT.
     */
    ASSERT,

    /**
     * Pops the top value of the stack, and throw it.
     */
    THROW,

    /**
     * TRY CatchOffset(sbyte) FinallyOffset(sbyte). If there's no catch body, set CatchOffset 0. If there's no
     * finally body, set FinallyOffset 0.
     */
    TRY,

    /**
     * TRY_L CatchOffset(int) FinallyOffset(int). If there's no catch body, set CatchOffset 0. If there's no finally
     * body, set FinallyOffset 0.
     */
    TRY_L,

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then unconditionally transfers
     * control to the specific target instruction, represented as a 1-byte signed offset from the beginning of the
     * current instruction.
     */
    ENDTRY,

    /**
     * Ensures that the appropriate surrounding finally blocks are executed. And then unconditionally transfers
     * control to the specific target instruction, represented as a 4-byte signed offset from the beginning of the
     * current instruction.
     */
    ENDTRY_L,

    /**
     * End finally, If no exception happen or be catched, vm will jump to the target instruction of ENDTRY/ENDTRY_L.
     * Otherwise, the VM will rethrow the exception to upper layer.
     */
    ENDFINALLY,

    /**
     * Returns from the current method.
     */
    RET,

    /**
     * Calls to an interop service.
     */
    SYSCALL,

    // endregion
    // region Stack

    /**
     * Puts the number of stack items onto the stack.
     */
    DEPTH,

    /**
     * Removes the top stack item.
     */
    DROP,

    /**
     * Removes the second-to-top stack item.
     */
    NIP,

    /**
     * The item n back in the main stack is removed.
     */
    XDROP,

    /**
     * Clears the stack.
     */
    CLEAR,

    /**
     * Duplicates the top stack item.
     */
    DUP,

    /**
     * Copies the second-to-top stack item to the top.
     */
    OVER,

    /**
     * The item n back in the stack is copied to the top.
     */
    PICK,

    /**
     * The item at the top of the stack is copied and inserted before the second-to-top item.
     */
    TUCK,

    /**
     * The top two items on the stack are swapped.
     */
    SWAP,

    /**
     * The top three items on the stack are rotated to the left.
     */
    ROT,

    /**
     * The item n back in the stack is moved to the top.
     */
    ROLL,

    /**
     * Reverse the order of the top 3 items on the stack.
     */
    REVERSE3,

    /**
     * Reverse the order of the top 4 items on the stack.
     */
    REVERSE4,

    /**
     * Pop the number N on the stack, and reverse the order of the top N items on the stack.
     */
    REVERSEN,

    // endregion
    // region Slot

    /**
     * Initialize the static field list for the current execution context.
     */
    INITSSLOT,

    /**
     * Initialize the argument slot and the local variable list for the current execution context.
     */
    INITSLOT,

    /**
     * Loads the static field at index 0 onto the evaluation stack.
     */
    LDSFLD0,

    /**
     * Loads the static field at index 1 onto the evaluation stack.
     */
    LDSFLD1,

    /**
     * Loads the static field at index 2 onto the evaluation stack.
     */
    LDSFLD2,

    /**
     * Loads the static field at index 3 onto the evaluation stack.
     */
    LDSFLD3,

    /**
     * Loads the static field at index 4 onto the evaluation stack.
     */
    LDSFLD4,

    /**
     * Loads the static field at index 5 onto the evaluation stack.
     */
    LDSFLD5,

    /**
     * Loads the static field at index 6 onto the evaluation stack.
     */
    LDSFLD6,

    /**
     * Loads the static field at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    LDSFLD,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 0.
     */
    STSFLD0,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 1.
     */
    STSFLD1,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 2.
     */
    STSFLD2,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 3.
     */
    STSFLD3,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 4.
     */
    STSFLD4,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 5.
     */
    STSFLD5,

    /**
     * Stores the value on top of the evaluation stack in the static field list at index 6.
     */
    STSFLD6,

    /**
     * Stores the value on top of the evaluation stack in the static field list at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    STSFLD,

    /**
     * Loads the local variable at index 0 onto the evaluation stack.
     */
    LDLOC0,

    /**
     * Loads the local variable at index 1 onto the evaluation stack.
     */
    LDLOC1,

    /**
     * Loads the local variable at index 2 onto the evaluation stack.
     */
    LDLOC2,

    /**
     * Loads the local variable at index 3 onto the evaluation stack.
     */
    LDLOC3,

    /**
     * Loads the local variable at index 4 onto the evaluation stack.
     */
    LDLOC4,

    /**
     * Loads the local variable at index 5 onto the evaluation stack.
     */
    LDLOC5,

    /**
     * Loads the local variable at index 6 onto the evaluation stack.
     */
    LDLOC6,

    /**
     * Loads the local variable at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    LDLOC,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 0.
     */
    STLOC0,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 1.
     */
    STLOC1,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 2.
     */
    STLOC2,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 3.
     */
    STLOC3,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 4.
     */
    STLOC4,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 5.
     */
    STLOC5,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at index 6.
     */
    STLOC6,

    /**
     * Stores the value on top of the evaluation stack in the local variable list at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    STLOC,

    /**
     * Loads the argument at index 0 onto the evaluation stack.
     */
    LDARG0,

    /**
     * Loads the argument at index 1 onto the evaluation stack.
     */
    LDARG1,

    /**
     * Loads the argument at index 2 onto the evaluation stack.
     */
    LDARG2,

    /**
     * Loads the argument at index 3 onto the evaluation stack.
     */
    LDARG3,

    /**
     * Loads the argument at index 4 onto the evaluation stack.
     */
    LDARG4,

    /**
     * Loads the argument at index 5 onto the evaluation stack.
     */
    LDARG5,

    /**
     * Loads the argument at index 6 onto the evaluation stack.
     */
    LDARG6,

    /**
     * Loads the argument at a specified index onto the evaluation stack. The index is represented as a 1-byte
     * unsigned integer.
     */
    LDARG,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 0.
     */
    STARG0,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 1.
     */
    STARG1,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 2.
     */
    STARG2,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 3.
     */
    STARG3,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 4.
     */
    STARG4,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 5.
     */
    STARG5,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at index 6.
     */
    STARG6,

    /**
     * Stores the value on top of the evaluation stack in the argument slot at a specified index. The index is
     * represented as a 1-byte unsigned integer.
     */
    STARG,

    // endregion
    // region Splice

    /**
     * Creates a new Buffer and pushes it onto the stack.
     */
    NEWBUFFER,

    /**
     * Copies a range of bytes from one Buffer to another.
     */
    MEMCPY,

    /**
     * Concatenates two strings.
     */
    CAT,

    /**
     * Returns a section of a string.
     */
    SUBSTR,

    /**
     * Keeps only characters left of the specified point in a string.
     */
    LEFT,

    /**
     * Keeps only characters right of the specified point in a string.
     */
    RIGHT,

    // endregion
    // region Bitwise logic

    /**
     * Flips all bits in the input.
     */
    INVERT,

    /**
     * Boolean and between each bit in the inputs.
     */
    AND,

    /**
     * Boolean or between each bit in the inputs.
     */
    OR,

    /**
     * Boolean exclusive or between each bit in the inputs.
     */
    XOR,

    /**
     * Returns 1 if the inputs are exactly equal, 0 otherwise.
     */
    EQUAL,

    /**
     * Returns 1 if the inputs are not equal, 0 otherwise.
     */
    NOTEQUAL,

    // endregion
    // region Arithmetic

    /**
     * Puts the sign of top stack item on top of the main stack.
     * <p>If the value is negative, put -1; if positive, put 1; if zero, put 0.
     */
    SIGN,

    /**
     * The input is made positive.
     */
    ABS,

    /**
     * The sign of the input is flipped.
     */
    NEGATE,

    /**
     * 1 is added to the input.
     */
    INC,

    /**
     * 1 is subtracted from the input.
     */
    DEC,

    /**
     * a is added to b.
     */
    ADD,

    /**
     * b is subtracted from a.
     */
    SUB,

    /**
     * a is multiplied by b.
     */
    MUL,

    /**
     * a is divided by b.
     */
    DIV,

    /**
     * Returns the remainder after dividing a by b.
     */
    MOD,

    /**
     * Takes two elements of the stack and raises the first one to the power of the second one.
     */
    POW,

    /**
     * Returns the square root of a specified number.
     */
    SQRT,

    /**
     * Performs modulus division on a number multiplied by another number.
     */
    MODMUL,

    /**
     * Performs modulus division on a number raised to the power of another number. If the exponent is -1, it will
     * have the calculation of the modular inverse.
     */
    MODPOW,

    /**
     * Shifts a left b bits, preserving sign.
     */
    SHL,

    /**
     * Shifts a right b bits, preserving sign.
     */
    SHR,

    /**
     * If the input is 0 or 1, it is flipped. Otherwise, the output will be 0.
     */
    NOT,

    /**
     * If both a and b are not 0, the output is 1. Otherwise, 0.
     */
    BOOLAND,

    /**
     * If a or b is not 0, the output is 1. Otherwise, 0.
     */
    BOOLOR,

    /**
     * Returns 0 if the input is 0. 1 otherwise.
     */
    NZ,

    /**
     * Returns 1 if the numbers are equal, 0 otherwise.
     */
    NUMEQUAL,

    /**
     * Returns 1 if the numbers are not equal, 0 otherwise.
     */
    NUMNOTEQUAL,

    /**
     * Returns 1 if a is less than b, 0 otherwise.
     */
    LT,

    /**
     * Returns 1 if a is less than or equal to b, 0 otherwise.
     */
    LE,

    /**
     * Returns 1 if a is greater than b, 0 otherwise.
     */
    GT,

    /**
     * Returns 1 if a is greater than or equal to b, 0 otherwise.
     */
    GE,

    /**
     * Returns the smallest of a and b.
     */
    MIN,

    /**
     * Returns the largest of a and b.
     */
    MAX,

    /**
     * Returns 1 if x is within the specified range (left-inclusive), 0 otherwise.
     */
    WITHIN,

    // endregion
    // region Compound-type

    /**
     * A value n is taken from top of main stack. The next n*2 items on main stack are removed, put inside n-sized
     * map and this map is put on top of the main stack.
     */
    PACKMAP,

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put inside n-sized
     * struct and this struct is put on top of the main stack.
     */
    PACKSTRUCT,

    /**
     * A value n is taken from top of main stack. The next n items on main stack are removed, put inside n-sized
     * array and this array is put on top of the main stack.
     */
    PACK,

    /**
     * An array or map is removed from top of the main stack. Its elements are put on top of the main stack (in
     * reverse order). In case of a map, the key-value pairs are flattened, i.e., the value of the first pair is
     * pushed, then the key of the first pair, then the value of the second item and so on.
     */
    UNPACK,

    /**
     * An empty array (with size 0) is put on top of the main stack.
     */
    NEWARRAY0,

    /**
     * A value n is taken from top of main stack. A null-filled array with size n is put on top of
     * the main stack.
     */
    NEWARRAY,

    /**
     * A value n is taken from top of main stack. An array of type T with size n is put on top of the main stack.
     */
    NEWARRAY_T,

    /**
     * An empty struct (with size 0) is put on top of the main stack.
     */
    NEWSTRUCT0,

    /**
     * A value n is taken from top of main stack. A zero-filled struct with size n is put on top of the main stack.
     */
    NEWSTRUCT,

    /**
     * A Map is created and put on top of the main stack.
     */
    NEWMAP,

    /**
     * An array is removed from top of the main stack. Its size is put on top of the main stack.
     */
    SIZE,

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack.
     * <p>
     * Puts True on top of main stack if array[n] (or map[n]) exist, and False otherwise.
     */
    HASKEY,

    /**
     * A map is taken from top of the main stack. The keys of this map are put on top of the main stack.
     */
    KEYS,

    /**
     * A map is taken from top of the main stack. The values of this map are put on top of the main stack.
     */
    VALUES,

    /**
     * An input index n (or key) and an array (or map) are taken from main stack. Element array[n] (or map[n]) is put
     * on top of the main stack.
     */
    PICKITEM,

    /**
     * The item on top of main stack is removed and appended to the second item on top of the main stack.
     */
    APPEND,

    /**
     * A value v, index n (or key) and an array (or map) are taken from main stack. Attribution array[n]=v (or
     * map[n]=v) is performed.
     */
    SETITEM,

    /**
     * An array is removed from the top of the main stack and its elements are reversed.
     */
    REVERSEITEMS,

    /**
     * An input index n (or key) and an array (or map) are removed from the top of the main stack. Element array[n]
     * (or map[n]) is removed.
     */
    REMOVE,

    /**
     * Remove all the items from the compound-type.
     */
    CLEARITEMS,

    // endregion
    // region Types

    /**
     * Returns true if the input is null. Returns false otherwise.
     */
    ISNULL,

    /**
     * Returns true if the top item is of the specified type.
     */
    ISTYPE,

    /**
     * Converts the top item to the specified type.
     */
    CONVERT,

    /**
     * Pops the top stack item. Then, turns the vm state to FAULT immediately, and cannot be caught. The top stack
     * item is used as reason.
     */
    ABORTMSG,

    /**
     * Pops the top two stack items. If the second-to-top stack value is false, exits the vm execution and sets the
     * vm state to FAULT. In this case, the top stack value is used as reason for the exit. Otherwise, it is ignored.
     */
    ASSERTMSG;

    // endregion
}
