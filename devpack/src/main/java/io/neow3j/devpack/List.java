package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.model.types.StackItemType;

/**
 * An ordered list of elements with the same type.
 * <p>
 * A list is backed by an array on the neo-vm.
 *
 * @param <T> the type of elements in this list
 */
public class List<T> {

    @Instruction(opcode = OpCode.NEWARRAY0)
    public List() { }

    @Instruction(opcode = OpCode.NOP)
    public List(T[] array) { }

    @Instruction(opcode = OpCode.SIZE)
    public native int size();

    @Instruction(opcode = OpCode.PICKITEM)
    public native T get(int index);

    @Instruction(opcode = OpCode.SETITEM)
    public native void set(int index, int item);

    @Instruction(opcode = OpCode.APPEND)
    public native void add(T item);

    @Instruction(opcode = OpCode.REMOVE)
    public native void remove(int index);

    @Instruction(opcode = OpCode.CLEARITEMS)
    public native void clear();

    @Instruction(opcode = OpCode.VALUES)
    public native List<T> clone();

    @Instruction
    public native T[] toArray();

}
