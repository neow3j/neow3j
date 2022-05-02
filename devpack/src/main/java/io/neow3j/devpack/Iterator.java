package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

import static io.neow3j.script.InteropService.SYSTEM_ITERATOR_NEXT;
import static io.neow3j.script.InteropService.SYSTEM_ITERATOR_VALUE;

/**
 * A NeoVM-specific iterator used to iterate over a set of elements.
 */
public class Iterator<V> implements InteropInterface {

    /**
     * Moves this {@code Iterator}'s position to the next element.
     *
     * @return true if there is a next element. False, otherwise.
     */
    @Instruction(interopService = SYSTEM_ITERATOR_NEXT)
    public native boolean next();

    /**
     * @return the element at the current {@code Iterator} position.
     */
    @Instruction(interopService = SYSTEM_ITERATOR_VALUE)
    public native V get();

    /**
     * Compares this iterator to the given object. The comparison happens by reference only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same iterator. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Represents a two element struct that the neo-vm uses when iterating over a map.
     *
     * @param <K> the type of the first element of the struct.
     * @param <V> the type of the second element of the struct.
     */
    public static class Struct<K, V> {
        public K key;
        public V value;
    }

}



