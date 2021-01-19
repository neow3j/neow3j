package io.neow3j.devpack.neo;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;

import io.neow3j.devpack.ApiInterface;
import io.neow3j.devpack.annotations.Syscall;

/**
 * A neo-vm enumerator used to enumerate a list of values.
 */
public class Enumerator<V> implements ApiInterface {

    /**
     * Creates an {@code Enumerator} over the given entries.
     *
     * @param entries The values to enumerate.
     */
    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public Enumerator(V[] entries) { }

    private Enumerator() { }

    /**
     * Concatenates the given {@code Enumerator} to this one.
     *
     * @param value The {@code Enumerator} to concatenate.
     * @return the concatenated {@code Enumerator}s.
     */
    @Syscall(SYSTEM_ENUMERATOR_CONCAT)
    public native Enumerator<V> concat(Enumerator<V> value);

    /**
     * Moves this {@code Enumerator}'s position to the next element if it exists and returns
     * true. Returns false if no next element exists.
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
