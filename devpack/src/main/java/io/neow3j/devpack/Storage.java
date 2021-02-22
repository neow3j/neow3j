package io.neow3j.devpack;

import io.neow3j.devpack.Map.Entry;
import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_DELETE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_FIND;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GET;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETREADONLYCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_PUT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_PUTEX;

/**
 * Provides a set of methods to insert, query, and delete data in the persistent storage of smart
 * contracts.
 * <p>
 * Most methods in <tt>Storage</tt> are available in a version that requires a {@link
 * StorageContext} as parameter and a version that doesn't. On multiple consecutive calls to
 * <tt>Storage</tt> methods, it makes sense to retrieve the <tt>StorageContext</tt> once and reuse
 * it in each call. This saves GAS because the Syscall for getting the
 * <tt>StorageContext</tt> is only called once and not with every <tt>Storage</tt> method
 * invocation.
 */
public class Storage {

    /**
     * Returns the storage context of the contract.
     *
     * @return the storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    public static native StorageContext getStorageContext();

    /**
     * Returns the storage context of the contract in read-only mode.
     *
     * @return the read-only storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETREADONLYCONTEXT)
    public static native StorageContext getReadOnlyContext();

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, String key);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, String value);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, byte[] key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, String key);

    /**
     * Returns an iterator over the values found under the given key prefix.
     *
     * @param context The storage context to get the values from.
     * @param prefix  The key prefix.
     * @return an iterator over key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<Entry<byte[], byte[]>> find(StorageContext context,
            byte[] prefix);

    /**
     * Returns an iterator over the values found under the given key prefix.
     *
     * @param context The storage context to get the values from.
     * @param prefix  The key prefix.
     * @return an iterator over key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<Entry<String, byte[]>> find(StorageContext context,
            String prefix);

    /**
     * Returns the value corresponding to the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(byte[] key);

    /**
     * Returns the value corresponding to the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(String key);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, int value);

    /**
     * Stores the given key-value pair using the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, String value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, String value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, int value);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key   The key.
     * @param value The value to store.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, String value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key         The key.
     * @param value       The value to store.
     * @param storageFlag The storage flags to use.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, String value, byte storageFlag);

    /**
     * Deletes the value corresponding to the given key from the storage.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key The key to delete.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(byte[] key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param key The key to delete.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(String key);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param prefix The key prefix.
     * @return an iterator over key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<Entry<byte[], byte[]>> find(byte[] prefix);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     *
     * @param prefix The key prefix.
     * @return an iterator over key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<Entry<String, byte[]>> find(String prefix);

}
