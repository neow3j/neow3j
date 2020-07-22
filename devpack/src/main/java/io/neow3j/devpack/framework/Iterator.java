package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEYS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_VALUES;

import io.neow3j.devpack.framework.annotations.Syscall;

/**
 * A NeoVM-specific iterator used to iterate over a set of key-value elements.
 */
public class Iterator<K, V> {

    /**
     * Creates an {@code Iterator} over the entries of the given {@link Map}. The keys and values
     * of the {@code Map} become the keys and values of the {@code Iterator}.
     *
     * @param entries The map to iterator over.
     * @param <K> The type of the keys in the iterator.
     * @param <V> The type of the values in the iterator.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K, V> Iterator<K, V> create(Map<K, V> entries);

    /**
     * Creates an {@code Iterator} over the given entries. The keys of the iterator are the
     * entries' indices.
     *
     * @param entries The elements to iterator over.
     * @param <V> The type of the values in the iterator.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <V> Iterator<Integer, V> create(V[] entries);

    /**
     * Creates an {@code Iterator} over the given bytes. The keys of the iterator are the entries'
     * indices.
     *
     * @param entries The bytes to iterator over.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Byte> create(byte[] entries);

    /**
     * Creates an {@code Iterator} over the given integers. The keys of the iterator are the
     * entries' indices.
     *
     * @param entries The integers to iterator over.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Integer> create(int[] entries);

    /**
     * Creates an {@code Iterator} over the given characters. The keys of the iterator are the
     * entries' indices.
     *
     * @param entries The characters to iterator over.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Character> create(char[] entries);

    /**
     * Creates an {@code Iterator} over the given boolean values. The keys of the iterator are the
     * entries' indices.
     *
     * @param entries The boolean values to iterator over.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Boolean> create(boolean[] entries);

    /**
     * Creates an {@code Iterator} over the characters in the given string. The keys of the
     * iterator are the indices of the string's characters.
     *
     * @param characters The string to iterator over.
     * @return the {@code Iterator}.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Character> create(String characters);

    /**
     * Concatenates the given {@code Iterator} to this one.
     *
     * @param value The {@code Iterator} to concatenate.
     * @return the concatenated {@code Iterator}s.
     */
    @Syscall(SYSTEM_ITERATOR_CONCAT)
    public native Iterator<K, V> concat(Iterator<K, V> value);

    /**
     * Moves this {@code Iterator}'s position to the next element.
     *
     * @return true if there is a next element. False, otherwise.
     */
    @Syscall(SYSTEM_ENUMERATOR_NEXT)
    public native boolean next();

    /**
     * Gets the key of the element at the current {@code Iterator} position.
     *
     * @return the key.
     */
    @Syscall(SYSTEM_ITERATOR_KEY)
    public native K getKey();

    /**
     * Gets the value of the element at the current {@code Iterator} position.
     *
     * @return the value.
     */
    @Syscall(SYSTEM_ENUMERATOR_VALUE)
    public native V getValue();

    /**
     * Gets all keys of this {@code Iterator}.
     *
     * @return the keys.
     */
    @Syscall(SYSTEM_ITERATOR_KEYS)
    public native Enumerator<K> getKeys();

    /**
     * Gets all values of this {@code Iterator}.
     *
     * @return the values.
     */
    @Syscall(SYSTEM_ITERATOR_VALUES)
    public native Enumerator<V> getValues();

}

