package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_KEYS;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ITERATOR_VALUES;

import io.neow3j.devpack.framework.annotations.Syscall;

public class Iterator<K, V> {

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K, V> Iterator<K, V> create(Map<K, V> entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <V> Iterator<Integer, V> create(V[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Byte> create(byte[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Integer> create(int[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Character> create(char[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Boolean> create(boolean[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native Iterator<Integer, Character> create(String entry);

    @Syscall(SYSTEM_ITERATOR_CONCAT)
    public native Iterator<K, V> concat(Iterator<K, V> value);

    @Syscall(SYSTEM_ENUMERATOR_NEXT)
    public native boolean next();

    @Syscall(SYSTEM_ITERATOR_KEY)
    public native K getKey();

    @Syscall(SYSTEM_ENUMERATOR_VALUE)
    public native V getValue();

    @Syscall(SYSTEM_ITERATOR_KEYS)
    public native Enumerator<K> getKeys();

    @Syscall(SYSTEM_ITERATOR_VALUES)
    public native Enumerator<V> getValues();

}

