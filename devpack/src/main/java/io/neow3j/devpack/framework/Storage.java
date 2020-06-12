package io.neow3j.devpack.framework;

public class Storage {

    @Syscall("Neo.Storage.Get")
    public static native Object get(String key);

    @Syscall("Neo.Storage.Put")
    public static native void put(StorageContext ctx, String key, Object value);

    @Syscall("Neo.Storage.GetContext")
    public static native StorageContext getStorageContext();

    public static class StorageContext {

    }
}
