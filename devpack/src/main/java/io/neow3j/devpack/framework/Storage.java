package io.neow3j.devpack.framework;

@Syscall("System.Storage")
public class Storage {

    @Syscall("System.Storage.Get")
    public static native Object get(String key);

    @Syscall("System.Storage.Put")
    public static native void put(StorageContext ctx, String key, Object value);

    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(String key, Object value);

    @Syscall("System.Storage.GetContext")
    public static native StorageContext getStorageContext();

    public static class StorageContext {

    }
}
