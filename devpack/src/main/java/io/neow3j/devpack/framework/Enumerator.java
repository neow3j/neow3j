package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;

import io.neow3j.devpack.framework.annotations.Syscall;

/**
 * A NeoVM-specific enumerator used to iterate over a set of values.
 */
public class Enumerator<V> {

    /**
     * Creates an <tt>Enumerator</tt> over the given entries.
     *
     * @param entries The values to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native <V> Enumerator<V> create(V[] entries);

    /**
     * Creates an <tt>Enumerator</tt> over the given bytes.
     *
     * @param entries The bytes to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Byte> create(byte[] entries);

    /**
     * Creates an <tt>Enumerator</tt> over the given integers.
     *
     * @param entries The integers to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Integer> create(int[] entries);

    /**
     * Creates an <tt>Enumerator</tt> over the given characters.
     *
     * @param entries The characters to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(char[] entries);

    /**
     * Creates an <tt>Enumerator</tt> over the given boolean values.
     *
     * @param entries The boolean values to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Boolean> create(boolean[] entries);

    /**
     * Creates an <tt>Enumerator</tt> over the characters in the given string.
     *
     * @param characters The string to enumerate.
     * @return the <tt>Enumerator</tt>.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(String characters);

    /**
     * Concatenates the given <tt>Enumerator</tt> to this one.
     *
     * @param value The <tt>Enumerator</tt> to concatenate.
     * @return the concatenated <tt>Enumerator</tt>s.
     */
    @Syscall(SYSTEM_ENUMERATOR_CONCAT)
    public native Enumerator<V> concat(Enumerator<V> value);

    /**
     * Moves this <tt>Enumerator</tt>'s position to the next element.
     *
     * @return true if there is a next element. False, otherwise.
     */
    @Syscall(SYSTEM_ENUMERATOR_NEXT)
    public native boolean next();

    /**
     * Gets the value of the element at the current <tt>Enumerator</tt> position.
     *
     * @return the value.
     */
    @Syscall(SYSTEM_ENUMERATOR_VALUE)
    public native V getValue();
}
