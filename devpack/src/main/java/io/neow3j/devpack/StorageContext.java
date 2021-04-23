package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Syscall;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_STORAGE_ASREADONLY;
import static io.neow3j.devpack.Helper.toByteArray;

/**
 * A {@code StorageContext} is the gateway to a contracts storage. It can be passed to other
 * contracts as an argument, allowing them to perform read/write operations on the persistent store
 * of the current contract. It is required in all {@link Storage} methods.
 */
public class StorageContext implements InteropInterface {

    /**
     * Gets this {@code StorageContext} in read-only mode, meaning that after calling this method,
     * write access to the contract's storage is denied.
     *
     * @return this {@code StorageContext}.
     */
    @Syscall(SYSTEM_STORAGE_ASREADONLY)
    public native StorageContext asReadOnly();

    /**
     * Creates a new {@link StorageMap} from entries with the given prefix in this
     * {@code StorageContext}.
     *
     * @param prefix The prefix.
     * @return the {@link StorageMap}
     */
    public StorageMap createMap(ByteString prefix) {
        return new StorageMap(this, prefix.toByteArray());
    }

    /**
     * Creates a new {@link StorageMap} from entries with the given prefix in this
     * {@code StorageContext}.
     *
     * @param prefix The prefix.
     * @return the {@link StorageMap}
     */
    public StorageMap createMap(String prefix) {
        return new StorageMap(this, Helper.toByteArray(prefix));
    }

    /**
     * Creates a new {@link StorageMap} from entries with the given prefix in this
     * {@code StorageContext}.
     *
     * @param prefix The prefix.
     * @return the {@link StorageMap}
     */
    public StorageMap createMap(byte[] prefix) {
        return new StorageMap(this, prefix);
    }

    /**
     * Creates a new {@link StorageMap} from entries with the given prefix in this
     * {@code StorageContext}.
     *
     * @param prefix The prefix.
     * @return the {@link StorageMap}
     */
    public StorageMap createMap(byte prefix) {
        return new StorageMap(this, toByteArray(prefix));
    }

}
