package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CONCAT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_CREATE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_NEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_ENUMERATOR_VALUE;

import io.neow3j.devpack.framework.annotations.Syscall;

public class Enumerator<V> {

    // TODO: Enumerator should be able to handle at least the following cases:
//        object[] enum = new object[4];
//        Enumerator<object> e = Enumerator<object>.Create(enum);
//        int[] enum1 = new int[4];
//        Enumerator<int> e1 = Enumerator<int>.Create(enum1);
//        char[] enum2 = new char[4];
//        Enumerator<char> e2 = Enumerator<char>.Create(enum2);
//        string enum3 = "hello";
//        Enumerator<char> e3 = Enumerator<char>.Create(enum3);
//        byte[] enum4 = new byte[4];
//        Enumerator<byte> e4 = Enumerator<byte>.Create(enum4);
//        bool[] enum5 = new bool[4];
//        Enumerator<bool> e5 = Enumerator<bool>.Create(enum5);

        @Syscall(SYSTEM_ENUMERATOR_CREATE)
        public static native <V> Enumerator<V> Create(Iterable<V> entry);

        @Syscall(SYSTEM_ENUMERATOR_CONCAT)
        public native Enumerator<V> Concat(Enumerator<V> value);

        @Syscall(SYSTEM_ENUMERATOR_NEXT)
        public native boolean Next();

        @Syscall(SYSTEM_ENUMERATOR_VALUE)
        public native V getValue();
}
