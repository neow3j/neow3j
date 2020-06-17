package io.neow3j.devpack.framework;

import java.math.BigInteger;

@Syscall("System.Storage")
public class Storage {

    /**
     * Returns the current storage context.
     */
    @Syscall("System.Storage.GetContext")
    public static native StorageContext getStorageContext();

    /**
     * Returns the current read only storage context.
     */
    @Syscall("System.Storage.GetReadOnlyContext")
    public static native StorageContext getReadOnlyContext();

    /**
     * Returns the value corresponding to the given key using the given storage context.
     */
    @Syscall("System.Storage.Get")
    public static native byte[] get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key using the given storage context.
     */
    @Syscall("System.Storage.Get")
    public static native byte[] get(StorageContext context, String key);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, byte[] key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, byte[] key, int value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     */
//    @Syscall("System.Storage.Put")
//    public static native void put(StorageContext context, byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, byte[] key, String value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, String key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, String key, int value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     */
//    @Syscall("System.Storage.Put")
//    public static native void put(StorageContext context, String key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall("System.Storage.Put")
    public static native void put(StorageContext context, String key, String value);

    /**
     * Deletes the entry with the given key from the given storage context.
     */
    @Syscall("System.Storage.Delete")
    public static native void delete(StorageContext context, byte[] key);

    /**
     * Deletes the entry with the given key from the given storage context.
     */
    @Syscall("System.Storage.Delete")
    public static native void delete(StorageContext context, String key);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the given storage
//     * context.
//     */
//    @Syscall("System.Storage.Find")
//    public static native Iterator<byte[], byte[]> find(StorageContext context, byte[] prefix);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the given storage
//     * context.
//     */
//    @Syscall("System.Storage.Find")
//    public static native Iterator<String, byte[]> find(StorageContext context, String prefix);

    /**
     * Returns the value corresponding to the given key for the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Get")
    public static native byte[] get(byte[] key);

    /**
     * Returns the value corresponding to the given key for the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Get")
    public static native byte[] get(String key);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(byte[] key, int value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall("System.Storage.GetContext")
//    @Syscall("System.Storage.Put")
//    public static native void put(byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(byte[] key, byte[] value, StorageFlag flag);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(byte[] key, int value, StorageFlag flag);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall("System.Storage.GetContext")
//    @Syscall("System.Storage.PutEx")
//    public static native void putEx(byte[] key, BigInteger value, StorageFlags flags);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(byte[] key, String value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(byte[] key, String value, StorageFlag flag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(String key, byte[] value, StorageFlag flag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(String key, int value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall("System.Storage.GetContext")
//    @Syscall("System.Storage.Put")
//    public static native void put(String key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(String key, BigInteger value, StorageFlag flag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Put")
    public static native void put(String key, String value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.PutEx")
    public static native void putEx(String key, String value, StorageFlag flag);

    /**
     * Deletes the entry with the given key from the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Delete")
    public static native void delete(byte[] key);

    /**
     * Deletes the entry with the given key from the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall("System.Storage.GetContext")
    @Syscall("System.Storage.Delete")
    public static native void delete(String key);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the current storage
//     * context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall("System.Storage.GetContext")
//    @Syscall("System.Storage.Find")
//    public static native Iterator<byte[], byte[]> find(byte[] prefix);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the current storage
//     * context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall("System.Storage.GetContext")
//    @Syscall("System.Storage.Find")
//    public static native Iterator<String, byte[]> find(String prefix);

    public static class StorageContext {

    }
}
