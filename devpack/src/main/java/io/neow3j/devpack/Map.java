package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

/**
 * A NeoVM-specific map for mapping keys to values of any NeoVM-compatible types.
 */
public class Map<K, V> {

    /**
     * Constructs a new {@code Map}.
     */
    @Instruction(opcode = OpCode.NEWMAP)
    public Map() {
    }

    /**
     * Gets the value to which the given key is mapped.
     * <p>
     * If the map doesn't contain the given key the NeoVM will throw an exception that needs to be catched.
     * Otherwise, the NeoVM will FAULT.
     *
     * @param key the key.
     * @return the corresponding value.
     */
    @Instruction(opcode = OpCode.PICKITEM)
    public native V get(K key);

    /**
     * Associates the given value with the given key in this map. If the map previously contained a mapping for the
     * key, the old value is replaced by the new value.
     *
     * @param key   the key.
     * @param value the value.
     */
    @Instruction(opcode = OpCode.SETITEM)
    public native void put(K key, V value);

    /**
     * Returns an array of the values contained in this map. Changes to the array are not reflected in the map.
     *
     * @return an array of this map's values.
     */
    @Instruction(opcode = OpCode.VALUES)
    public native V[] values();

    /**
     * Returns an array of the keys contained in this map. Changes to the array are not reflected in the map.
     *
     * @return an array of this map's keys.
     */
    @Instruction(opcode = OpCode.KEYS)
    public native K[] keys();

    /**
     * Checks if this map contains the given key.
     *
     * @param key the key to check.
     * @return true if this map contains a mapping for the specified key. False, otherwise.
     */
    @Instruction(opcode = OpCode.HASKEY)
    public native boolean containsKey(K key);

    /**
     * Removes the mapping for the given key from this map.
     *
     * @param key the key whose mapping is to be removed from the map.
     */
    @Instruction(opcode = OpCode.REMOVE)
    public native void remove(K key);

    /**
     * Compares this map to the given object. The comparison happens by reference only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same map. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
