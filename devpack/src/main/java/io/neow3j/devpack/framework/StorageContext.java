package io.neow3j.devpack.framework;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_ASREADONLY;

import io.neow3j.devpack.framework.annotations.Syscall;

/**
 * A <tt>StorageContext</tt> is the gateway to a contracts storage. It can be passed to other
 * contracts as an argument, allowing them to perform read/write operations on the persistent
 * store of the current contract. It is required in all {@link Storage} methods.
 */
public class StorageContext {

    /**
     * Gets this <tt>StorageContext</tt> in read-only mode, meaning that after calling this
     * method, write access to the contract's storage is denied.
     *
     * @return this <tt>StorageContext</tt>.
     */
    @Syscall(SYSTEM_STORAGE_ASREADONLY)
    public native StorageContext asReadOnly();

    public StorageMap createMap(String prefix) {
        return new StorageMap(this, SmartContract.toByteArray(prefix));
    }

    public StorageMap createMap(byte[] prefix) {
        return new StorageMap(this, prefix, 1);
    }

    public StorageMap createMap(byte prefix) {
        return new StorageMap(this, SmartContract.toByteArray(prefix));
    }

}
