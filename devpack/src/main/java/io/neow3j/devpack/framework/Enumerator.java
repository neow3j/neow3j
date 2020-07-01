package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;

import io.neow3j.devpack.framework.annotations.Syscall;

public class Enumerator<V> {

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native <V> Enumerator<V> create(V[] entries);

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Byte> create(byte[] entries);

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Integer> create(int[] entries);

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(char[] entries);

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Boolean> create(boolean[] entries);

    @Syscall(SYSTEM_ENUMERATOR_CREATE)
    public static native Enumerator<Character> create(String entries);

    @Syscall(SYSTEM_ENUMERATOR_CONCAT)
    public native Enumerator<V> concat(Enumerator<V> value);

    @Syscall(SYSTEM_ENUMERATOR_NEXT)
    public native boolean next();

    @Syscall(SYSTEM_ENUMERATOR_VALUE)
    public native V getValue();
}
