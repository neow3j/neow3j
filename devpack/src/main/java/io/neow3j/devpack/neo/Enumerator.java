package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;

import io.neow3j.devpack.annotations.Syscall;

/**
 * A NeoVM-specific enumerator used to iterate over a set of values.
 */
public class Enumerator<V> {

    /**
     * Creates an {@code Enumerator} over the given entries.
     *
     * @param entries The values to enumerate.
     * @param <V> The type of the entries in the enumerator.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native <V> Enumerator<V> create(V[] entries);

    /**
     * Creates an {@code Enumerator} over the given bytes.
     *
     * @param entries The bytes to enumerate.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Byte> create(byte[] entries);

    /**
     * Creates an {@code Enumerator} over the given integers.
     *
     * @param entries The integers to enumerate.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Integer> create(int[] entries);

    /**
     * Creates an {@code Enumerator} over the given characters.
     *
     * @param entries The characters to enumerate.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(char[] entries);

    /**
     * Creates an {@code Enumerator} over the given boolean values.
     *
     * @param entries The boolean values to enumerate.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Boolean> create(boolean[] entries);

    /**
     * Creates an {@code Enumerator} over the characters in the given string.
     *
     * @param characters The string to enumerate.
     * @return the {@code Enumerator}.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(String characters);

    /**
     * Concatenates the given {@code Enumerator} to this one.
     *
     * @param value The {@code Enumerator} to concatenate.
     * @return the concatenated {@code Enumerator}s.
     */
    @Syscall(SYSTEM_ENUMERATOR_CONCAT)
    public native Enumerator<V> concat(Enumerator<V> value);

    /**
     * Moves this {@code Enumerator}'s position to the next element.
     *
     * @return true if there is a next element. False, otherwise.
     */
    @Syscall(SYSTEM_ENUMERATOR_NEXT)
    public native boolean next();

    /**
     * Gets the value of the element at the current {@code Enumerator} position.
     *
     * @return the value.
     */
    @Syscall(SYSTEM_ENUMERATOR_VALUE)
    public native V getValue();
}
