package io.neow3j.devpack;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.annotations.Instruction;

import static io.neow3j.devpack.Helper.concat;

/**
 * A key-value view on the entries of smart contract's storage with a specific prefix.
 */
public class StorageMap {

    private final StorageContext context;
    private final byte[] prefix;

    /**
     * Constructs a new {@code StorageMep} from entries with the given prefix in the given {@link
     * StorageContext}.
     *
     * @param context The storage to look for the entries.
     * @param prefix  The prefix.
     */
    public StorageMap(StorageContext context, byte[] prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(byte[] key) {
        Storage.delete(this.context, concat(this.prefix, key));
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(ByteString key) {
        Storage.delete(this.context, concat(this.prefix, key));
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(String key) {
        Storage.delete(this.context, concat(this.prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(byte[] key) {
        return Storage.get(this.context, concat(this.prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(ByteString key) {
        return Storage.get(this.context, concat(this.prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(String key) {
        return Storage.get(this.context, concat(this.prefix, key));
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, byte[] value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, int value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, ByteString value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, String value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, byte[] value) {
        Storage.put(this.context, concat(this.prefix, key), value);

    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, int value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, ByteString value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, String value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, String value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, byte[] value) {
        Storage.put(this.context, concat(this.prefix, key), value);

    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, int value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, ByteString value) {
        Storage.put(this.context, concat(this.prefix, key), value);
    }

    /**
     * Compares this {@code StorageMap} to the given object. The comparison happens by reference
     * only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same storage map. False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);
}
