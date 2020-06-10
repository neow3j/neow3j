package io.neow3j.devpack.framework;

public class Storage {

    @InteropService("Neo.Storage.Get")
    public static native Object get(String key);

    @InteropService("Neo.Storage.Put")
    public static native void put(String key, Object value);

}
