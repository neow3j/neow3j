package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;

/**
 * A NeoVM-specific map for mapping keys to values of any NeoVM-compatible types.
 */
public class Map<K, V> {

    /**
     * Constructs a new <tt>Map</tt>.
     */
    @Instruction(opcode = OpCode.NEWMAP)
    public Map() {
    }

    /**
     * Gets the value to which the given key is mapped, or {@code null} if this map contains no
     * mapping for the key.
     *
     * @param key The key.
     * @return the corresponding value.
     */
    @Instruction(opcode = OpCode.PICKITEM)
    public native V get(K key);

    /**
     * Associates the given value with the given key in this map. If the map previously contained a
     * mapping for the key, the old value is replaced by the new value.
     *
     * @param key   The key.
     * @param value The value.
     */
    @Instruction(opcode = OpCode.SETITEM)
    public native void put(K key, V value);

    /**
     * Returns an array of the values contained in this map. Changes to the array are not reflected
     * in the map.
     *
     * @return an array of this map's values.
     */
    @Instruction(opcode = OpCode.VALUES)
    public native V[] values();

    /**
     * Returns an array of the keys contained in this map. Changes to the array are not reflected in
     * the map.
     *
     * @return an array of this map's keys.
     */
    @Instruction(opcode = OpCode.KEYS)
    public native K[] keys();

    /**
     * Checks if this map contains the given key.
     *
     * @param key The key to check.
     * @return <tt>true</tt>, if this map contains a mapping for the specified key.
     * <tt>False</tt>, otherwise.
     */
    @Instruction(opcode = OpCode.HASKEY)
    public native boolean containsKey(K key);

    /**
     * Removes the mapping for the given key from this map.
     *
     * @param key The key whose mapping is to be removed from the map.
     */
    @Instruction(opcode = OpCode.REMOVE)
    public native void remove(K key);

    public static class Entry<K, V> {
        public K key;
        public V value;
    }

}
