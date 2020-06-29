package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_ASREADONLY;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_DELETE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_FIND;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GET;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETREADONLYCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_PUT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_PUTEX;

import io.neow3j.devpack.framework.annotations.Syscall;

public class Storage {

    /**
     * Returns the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    public static native StorageContext getStorageContext();

    /**
     * Returns the current read only storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETREADONLYCONTEXT)
    public static native StorageContext getReadOnlyContext();

    /**
     * Returns the value corresponding to the given key using the given storage context.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key using the given storage context.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, String key);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, int value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, long value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(StorageContext context, byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, String value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, int value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, long value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(StorageContext context, String key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, String value);

    /**
     * Deletes the entry with the given key from the given storage context.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, byte[] key);

    /**
     * Deletes the entry with the given key from the given storage context.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, String key);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the given storage
//     * context.
//     */
//    @Syscall(SYSTEM_STORAGE_FIND)
//    public static native Iterator<byte[], byte[]> find(StorageContext context, byte[] prefix);

//    /**
//     * Returns an iterator over the storage entries with the given prefix from the given storage
//     * context.
//     */
//    @Syscall(SYSTEM_STORAGE_FIND)
//    public static native Iterator<String, byte[]> find(StorageContext context, String prefix);

    /**
     * Returns the value corresponding to the given key for the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(byte[] key);

    /**
     * Returns the value corresponding to the given key for the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(String key);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, int value);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, long value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, long value, byte storageFlag);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUTEX)
//    public static native void putEx(byte[] key, BigInteger value, StorageFlags flags);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, String value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, String value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, long value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, int value);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, long value);

//    /**
//     * Stores the given key-value pair using the current storage context.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(String key, BigInteger value);

//    /**
//     * Stores the given key-value pair using the current storage context and the given storage
//     * flags.
//     * <p>
//     * Needs to get the current storage context first.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUTEX)
//    public static native void putEx(String key, BigInteger value, byte storageFlag);

    /**
     * Stores the given key-value pair using the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, String value);

    /**
     * Stores the given key-value pair using the current storage context and the given storage
     * flags.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, String value, byte storageFlag);

    /**
     * Deletes the entry with the given key from the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(byte[] key);

    /**
     * Deletes the entry with the given key from the current storage context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(String key);

    /**
     * Returns an iterator over the storage entries with the given prefix from the current storage
     * context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<byte[], byte[]> find(byte[] prefix);

    /**
     * Returns an iterator over the storage entries with the given prefix from the current storage
     * context.
     * <p>
     * Needs to get the current storage context first.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<String, byte[]> find(String prefix);

    public static class StorageContext {

        @Syscall(SYSTEM_STORAGE_ASREADONLY)
        public native StorageContext asReadOnly();

    }
}
