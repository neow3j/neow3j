package io.neow3j.devpack;

import io.neow3j.script.OpCode;
import io.neow3j.devpack.annotations.Instruction;

import static io.neow3j.devpack.Helper.concat;

/**
 * A key-value view on the entries of smart contract's storage with a specific prefix.
 * <p>
 * Note that the storage size limit is 64 bytes for prefix + key and 65535 bytes for the value.
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

    // region delete

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(byte[] key) {
        Storage.delete(context, concat(prefix, key));
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(ByteString key) {
        Storage.delete(context, concat(prefix, key));
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(String key) {
        Storage.delete(context, concat(prefix, key));
    }

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    public void delete(int key) {
        Storage.delete(context, concat(prefix, key));
    }

    // endregion delete
    // region get bytearray key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(byte[] key) {
        return Storage.get(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    public byte[] getByteArray(byte[] key) {
        return Storage.getByteArray(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    public String getString(byte[] key) {
        return Storage.getString(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    public Integer getInteger(byte[] key) {
        return Storage.getInteger(context, concat(prefix, key));
    }

    // endregion get bytearray key
    // region get bytestring key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(ByteString key) {
        return Storage.get(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    public byte[] getByteArray(ByteString key) {
        return Storage.getByteArray(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    public String getString(ByteString key) {
        return Storage.getString(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    public Integer getInteger(ByteString key) {
        return Storage.getInteger(context, concat(prefix, key));
    }

    // endregion get bytestring key
    // region get string key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(String key) {
        return Storage.get(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    public byte[] getByteArray(String key) {
        return Storage.getByteArray(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    public String getString(String key) {
        return Storage.getString(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    public Integer getInteger(String key) {
        return Storage.getInteger(context, concat(prefix, key));
    }

    // endregion get string key
    // region get integer key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    public ByteString get(int key) {
        return Storage.get(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    public byte[] getByteArray(int key) {
        return Storage.getByteArray(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    public String getString(int key) {
        return Storage.getString(context, concat(prefix, key));
    }

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    public Integer getInteger(int key) {
        return Storage.getInteger(context, concat(prefix, key));
    }

    // endregion get integer key
    // region put bytearray key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, byte[] value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, int value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, ByteString value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, String value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, Hash160 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(byte[] key, Hash256 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    // endregion put bytearray key
    // region put bytestring key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, byte[] value) {
        Storage.put(context, concat(prefix, key), value);

    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, int value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, ByteString value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, String value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, Hash160 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(ByteString key, Hash256 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    // endregion put bytestring key
    // region put string key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, String value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, byte[] value) {
        Storage.put(context, concat(prefix, key), value);

    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, int value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, ByteString value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, Hash160 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    public void put(String key, Hash256 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    // endregion put string key
    // region put integer key

    public void put(int key, byte[] value) {
        Storage.put(context, concat(prefix, key), value);
    }

    public void put(int key, int value) {
        Storage.put(context, concat(prefix, key), value);
    }

    public void put(int key, ByteString value) {
        Storage.put(context, concat(prefix, key), value);
    }

    public void put(int key, String value) {
        Storage.put(context, concat(prefix, key), value);
    }

    public void put(int key, Hash160 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    public void put(int key, Hash256 value) {
        Storage.put(context, concat(prefix, key), value);
    }

    // endregion put integer key

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

    /**
     * Compares this and the given storage map by value, i.e., checks if they have the same context
     * and prefix.
     *
     * @param map Other storage map to compare to.
     * @return True if the two storage maps have the same context and prefix. False otherwise.
     */
    public boolean equals(StorageMap map) {
        if (this == map) {
            return true;
        }
        return context.equals(map.context)
                // The prefix is converted to a byte string for comparison because the neo-vm
                // does not compare Buffers (which byte[] is on the neo-vm) by value.
                && new ByteString(prefix).equals(new ByteString(map.prefix));
    }

}
