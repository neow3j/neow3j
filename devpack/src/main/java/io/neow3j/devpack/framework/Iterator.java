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

// TODO: Iterator should be able to handle at least the following cases:
//    object[] enum = new object[4];
//    Iterator<int,object> it = Iterator<int,object>.Create(enum);
//    int[] enum1 = new int[4];
//    Iterator<int,int> it1 = Iterator<int,int>.Create(enum1);
//    char[] enum2 = new char[4];
//    Iterator<int,char> it2 = Iterator<int,char>.Create(enum2);
//    string enum3 = "hello";
//    Iterator<int,char> it3 = Iterator<int,char>.Create(enum3);
//    byte[] enum4 = new byte[4];
//    Iterator<int,byte> it4 = Iterator<int,byte>.Create(enum4);
//    bool[] enum5 = new bool[4];
//    Iterator<int,bool> it5 = Iterator<int,bool>.Create(enum5);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K,V> Iterator<K, V> create(Map<K, V> entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K,V> Iterator<K, V> create(V[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K> Iterator<K, Byte> create(byte[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K> Iterator<K, Integer> create(int[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K> Iterator<K, Character> create(char[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K> Iterator<K, Boolean> create(boolean[] entry);

    @Syscall(SYSTEM_ITERATOR_CREATE)
    public static native <K> Iterator<K, Character> create(String entry);

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

