package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.Map.Entry;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_DELETE;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_FIND;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GET;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_GETREADONLYCONTEXT;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_PUT;

/**
 * Provides a set of methods to insert, query, and delete data in the persistent storage of smart
 * contracts.
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
    public static native ByteString get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, String key);

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, ByteString key);

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
    public static native void put(StorageContext context, byte[] key, ByteString value);

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
    public static native void put(StorageContext context, ByteString key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, ByteString value);

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
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, ByteString value);

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
    public static native void delete(StorageContext context, ByteString key);

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
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used. They
     * are documented in {@link FindOptions}.
     *
     * @param context     The storage context to get the values from.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, byte[] prefix, byte findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used. They
     * are documented in {@link FindOptions}.
     *
     * @param context     The storage context to get the values from.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, ByteString prefix, byte findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used. They
     * are documented in {@link FindOptions}.
     *
     * @param context     The storage context to get the values from.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, String prefix, byte findOptions);

}
