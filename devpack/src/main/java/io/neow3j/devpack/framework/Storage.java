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
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    public static native StorageContext getStorageContext();

    /**
     * Returns the storage context of the contract in read-only mode.
     */
    @Syscall(SYSTEM_STORAGE_GETREADONLYCONTEXT)
    public static native StorageContext getReadOnlyContext();

    /**
     * Returns the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key.
     */
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(StorageContext context, String key);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, int value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, long value);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     */
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(StorageContext context, byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, String value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, byte[] value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, int value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, long value);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     */
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(StorageContext context, String key, BigInteger value);

    /**
     * Stores the given key-value pair.
     */
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, String value);

    /**
     * Deletes the entry with the given key.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, byte[] key);

    /**
     * Deletes the entry with the given key.
     */
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, String key);

    /**
     * Returns an iterator over the entries found under the given key prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<byte[], byte[]> find(StorageContext context, byte[] prefix);

    /**
     * Returns an iterator over the entries found under the given key prefix.
     */
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<String, byte[]> find(StorageContext context, String prefix);

    /**
     * Returns the value corresponding to the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(byte[] key);

    /**
     * Returns the value corresponding to the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_GET)
    public static native byte[] get(String key);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, int value);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, long value);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     * <p>
//     * Implicitly makes a Syscall to retrieve the current contract's storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(byte[] key, BigInteger value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, long value, byte storageFlag);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     * <p>
//     * Implicitly makes a Syscall to retrieve the current contract's storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUTEX)
//    public static native void putEx(byte[] key, BigInteger value, StorageFlags flags);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(byte[] key, String value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(byte[] key, String value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, byte[] value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, int value, byte storageFlag);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, long value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, int value);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, long value);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     * <p>
//     * Implicitly makes a Syscall to retrieve the current contract's storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUT)
//    public static native void put(String key, BigInteger value);

    // TODO: Uncomment as soon as BigIntegers are supported by the compiler.
//    /**
//     * Stores the given key-value pair.
//     * <p>
//     * Implicitly makes a Syscall to retrieve the current contract's storage context.
//     */
//    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
//    @Syscall(SYSTEM_STORAGE_PUTEX)
//    public static native void putEx(String key, BigInteger value, byte storageFlag);

    /**
     * Stores the given key-value pair.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUT)
    public static native void put(String key, String value);

    /**
     * Stores the given key-value pair using the the given {@link StorageFlag}s.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_PUTEX)
    public static native void putEx(String key, String value, byte storageFlag);

    /**
     * Deletes the entry with the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(byte[] key);

    /**
     * Deletes the entry with the given key.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_DELETE)
    public static native void delete(String key);

    /**
     * Returns an iterator over the entries found under the given key prefix.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<byte[], byte[]> find(byte[] prefix);

    /**
     * Returns an iterator over the entries found under the given key prefix.
     * <p>
     * Implicitly makes a Syscall to retrieve the current contract's storage context.
     */
    @Syscall(SYSTEM_STORAGE_GETCONTEXT)
    @Syscall(SYSTEM_STORAGE_FIND)
    public static native Iterator<String, byte[]> find(String prefix);

    /**
     * A <tt>StorageContext</tt> is the gateway to a contracts storage. It can be passed to other
     * contracts as an argument, allowing them to perform read/write operations on the persistent
     * store of the current contract. It is required in all {@link Storage} methods.
     */
    public static class StorageContext {

        /**
         * Gets this <tt>StorageContext</tt> in read-only mode, meaning that after calling this
         * method, write access to the contract's storage is denied.
         *
         * @return this <tt>StorageContext</tt>.
         */
        @Syscall(SYSTEM_STORAGE_ASREADONLY)
        public native StorageContext asReadOnly();

    }
}
