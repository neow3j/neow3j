package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * An ordered list of elements with the same type.
 * <p>
 * A list is backed by an array on the neo-vm.
 *
 * @param <T> the type of elements in this list
 */
public class List<T> {

    /**
     * Constructs an empty list.
     */
    @Instruction(opcode = OpCode.NEWARRAY0)
    public List() {
    }

    /**
     * Constructs a list from the given array.
     * <p>
     * This does not incur any GAS costs because this list will use the given array to operate on.
     *
     * @param array the array.
     */
    @Instruction()
    public List(T[] array) {
    }

    /**
     * @return the size of this list.
     */
    @Instruction(opcode = OpCode.SIZE)
    public native int size();

    /**
     * Gets the item at the given index.
     * <p>
     * If the index is out of the range of this list the NeoVM will throw an exception that needs to be catched.
     * Otherwise, the NeoVM will FAULT.
     *
     * @param index the index.
     * @return the element;
     */
    @Instruction(opcode = OpCode.PICKITEM)
    public native T get(int index);

    /**
     * Sets the item at the given index.
     * <p>
     * If the index is out of the range of this list the NeoVM will throw an exception that needs to be catched.
     * Otherwise, the NeoVM will FAULT.
     *
     * @param index the index.
     * @param item  the item.
     */
    @Instruction(opcode = OpCode.SETITEM)
    public native void set(int index, int item);

    /**
     * Appends the given item at the end of this list.
     *
     * @param item the item to append.
     */
    @Instruction(opcode = OpCode.APPEND)
    public native void add(T item);

    /**
     * Removes the item at the given index, thereby reducing the size of this list by one.
     * <p>
     * If the index is out of the range of this list the NeoVM will FAULT immediately without the possibility to
     * handle the error.
     *
     * @param index the index.
     */
    @Instruction(opcode = OpCode.REMOVE)
    public native void remove(int index);

    /**
     * Reverses this list in place. Example: ["hello",2,3,4,5]: [5,4,3,2,"hello"]
     */
    @Instruction(opcode = OpCode.REVERSEITEMS)
    public native void reverse();

    /**
     * Removes all elements from this list.
     */
    @Instruction(opcode = OpCode.CLEARITEMS)
    public native void clear();

    /**
     * Creates a new list containing the same elements as this one.
     *
     * @return the new list.
     */
    @Override
    @Instruction(opcode = OpCode.VALUES)
    public native List<T> clone();

    /**
     * Returns this list as an array.
     * <p>
     * This does not incur any GAS costs because the underlying data structure of this list is an array of the same
     * type.
     *
     * @return this list as an array.
     */
    @Instruction
    public native T[] toArray();

    /**
     * Compares this list to the given object. The comparison happens by reference only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same list. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
