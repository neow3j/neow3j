package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEYS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_VALUES;

import io.neow3j.devpack.ApiInterface;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.annotations.Syscall;

/**
 * A NeoVM-specific iterator used to iterate over a set of key-value elements.
 */
public class Iterator<K, V> implements ApiInterface {

    /**
     * Creates an {@code Iterator} over the entries of the given {@code Enumerator}. The keys of the
     * iterator are consecutive integers from 0 up to the size of the {@code Enumerator}, i.e., the
     * indices of the entries. Thus, the {@code Iterator}'s first type parameter should be treated
     * as an Integer.
     * <p>
     * I.e., {@code Iterator<Integer, String> it = new Iterator(new Enumerator(new String[]{1, 2,
     * 3})}
     *
     * @param entries The elements to iterator over.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public Iterator(V[] entries) {
    }

    /**
     * Creates an {@code Iterator} over the entries of the given {@link Map}. The keys and values of
     * the {@code Map} become the keys and values of the {@code Iterator}.
     *
     * @param entries The map to iterator over.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public Iterator(Map<K, V> entries) {
    }

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

