package io.neow3j.devpack;

import io.neow3j.devpack.Map.Entry;
import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_VALUE;

/**
 * A NeoVM-specific iterator used to iterate over a set of elements.
 * <p>
 * If you invoke a method that returns an iterator via an RPC call (e.g., {@code invokefunction}),
 * the returned value will always be empty. To access the iterator's entries in this way, you
 * would have to first put them into a {@link List} or a {@link Map} as shown in the following
 * example:
 * <pre>
 * {@literal
 * ...
 *  List<ByteString> list = new io.neow3j.devpack.List<>();
 *  iterator.next();
 *  list.add(iterator.getValue());
 * ...
 * }
 * </pre>
 */
public class Iterator<V> implements InteropInterface {

    /**
     * Creates an {@code Iterator} over the entries of the given array.
     *
     * @param entries The elements to iterator over.
     * @param <V> The type of the iterator's entries.
     * @return the iterator.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <V> Iterator<V> create(V[] entries);

    /**
     * Creates a {@code Iterator} over the entries of the given map.
     * @param map The map to iterate over.
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     * @return the iterator.
     */
    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K, V> Iterator<Entry<K, V>> create(Map<K, V> map);

    /**
     * Moves this {@code Iterator}'s position to the next element.
     *
     * @return true if there is a next element. False, otherwise.
     */
    @Syscall(SYSTEM_ITERATOR_NEXT)
    public native boolean next();

    /**
     * Gets the value of the element at the current {@code Iterator} position.
     *
     * @return the value.
     */
    @Syscall(SYSTEM_ITERATOR_VALUE)
    public native V getValue();

}



