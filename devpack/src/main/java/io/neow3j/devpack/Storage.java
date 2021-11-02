package io.neow3j.devpack;

import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.script.OpCode;
import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.types.StackItemType;

import static io.neow3j.script.InteropService.SYSTEM_STORAGE_DELETE;
import static io.neow3j.script.InteropService.SYSTEM_STORAGE_FIND;
import static io.neow3j.script.InteropService.SYSTEM_STORAGE_GET;
import static io.neow3j.script.InteropService.SYSTEM_STORAGE_GETCONTEXT;
import static io.neow3j.script.InteropService.SYSTEM_STORAGE_GETREADONLYCONTEXT;
import static io.neow3j.script.InteropService.SYSTEM_STORAGE_PUT;

/**
 * Provides a set of methods to insert, query, and delete data in the persistent storage of smart
 * contracts.
 * <p>
 * Note that the size limit for storage keys and values is 64 bytes and 65535 bytes, respectively.
 */
public class Storage {

    // region get context

    /**
     * Returns the storage context of the contract.
     *
     * @return the storage context.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GETCONTEXT)
    public static native StorageContext getStorageContext();

    /**
     * Returns the storage context of the contract in read-only mode.
     *
     * @return the read-only storage context.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GETREADONLYCONTEXT)
    public static native StorageContext getReadOnlyContext();

    // endregion get context
    // region get bytearray key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public static native byte[] getByteArray(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native String getString(StorageContext context, byte[] key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes
     * are read in little-endian format. E.g., the byte string {@code 0102} (in hexadecimal
     * representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native Integer getInteger(StorageContext context, byte[] key);

    // endregion get bytearray key
    // region get string key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, String key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public static native byte[] getByteArray(StorageContext context, String key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native String getString(StorageContext context, String key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes
     * are read in little-endian format. E.g., the byte string {@code 0102} (in hexadecimal
     * representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native Integer getInteger(StorageContext context, String key);

    // endregion get string key
    // region get bytestring key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, ByteString key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public static native byte[] getByteArray(StorageContext context, ByteString key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native String getString(StorageContext context, ByteString key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes
     * are read in little-endian format. E.g., the byte string {@code 0102} (in hexadecimal
     * representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native Integer getInteger(StorageContext context, ByteString key);

    // endregion get bytestring key
    // region get integer key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native ByteString get(StorageContext context, int key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER_CODE)
    public static native byte[] getByteArray(StorageContext context, int key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    public static native String getString(StorageContext context, int key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes
     * are read in little-endian format. E.g., the byte string {@code 0102} (in hexadecimal
     * representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param context The storage context to search in.
     * @param key     The key to search for.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(interopService = SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER_CODE)
    public static native Integer getInteger(StorageContext context, int key);

    // endregion get integer key
    // region put bytearray key

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, ByteString value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, Hash160 value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, byte[] key, Hash256 value);

    // endregion put bytearray key
    // region put bytestring key

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, ByteString value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, Hash160 value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, ByteString key, Hash256 value);

    // endregion put bytestring key
    // region put string key

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, ByteString value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, Hash160 value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, String key, Hash256 value);

    // endregion put string key
    // region put integer key

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, ByteString value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, Hash160 value);

    /**
     * Stores the given key-value pair.
     *
     * @param context The storage context to store the value in.
     * @param key     The key.
     * @param value   The value to store.
     */
    @Instruction(interopService = SYSTEM_STORAGE_PUT)
    public static native void put(StorageContext context, int key, Hash256 value);

    // endregion put integer key
    // region delete

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Instruction(interopService = SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, byte[] key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Instruction(interopService = SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, ByteString key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Instruction(interopService = SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, String key);


    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param context The storage context to delete from.
     * @param key     The key to delete.
     */
    @Instruction(interopService = SYSTEM_STORAGE_DELETE)
    public static native void delete(StorageContext context, int key);

    // endregion delete
    // region find

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>}
     *         will be returned, where each {@code Struct} is a key-value pair found under the
     *         given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>}
     *         where each {@code ByteString} is a key found under the given prefix. The prefix is
     *         part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an
     *          {@code Iterator<Struct<ByteString, ByteString>>}, where each {@code Struct}
     *          is a key-value pair found under the given prefix but the prefix is removed from
     *          the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an
     *          {@code Iterator<ByteString>}, where each {@code ByteString} is a value found
     *          under the given prefix.
     *     </li>
     * </ul>
     *
     * @param context     The storage context to use.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Instruction(interopService = SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, byte[] prefix, byte findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>}
     *         will be returned, where each {@code Struct} is a key-value pair found under the
     *         given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>}
     *         where each {@code ByteString} is a key found under the given prefix. The prefix is
     *         part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an
     *          {@code Iterator<Struct<ByteString, ByteString>>}, where each {@code Struct}
     *          is a key-value pair found under the given prefix but the prefix is removed from
     *          the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an
     *          {@code Iterator<ByteString>}, where each {@code ByteString} is a value found
     *          under the given prefix.
     *     </li>
     * </ul>
     *
     * @param context     The storage context to use.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values, or key-value pairs found under the given prefix.
     */
    @Instruction(interopService = SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, ByteString prefix, byte findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>}
     *         will be returned, where each {@code Struct} is a key-value pair found under the
     *         given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>}
     *         where each {@code ByteString} is a key found under the given prefix. The prefix is
     *         part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an
     *          {@code Iterator<Struct<ByteString, ByteString>>}, where each {@code Struct}
     *          is a key-value pair found under the given prefix but the prefix is removed from
     *          the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an
     *          {@code Iterator<ByteString>}, where each {@code ByteString} is a value found
     *          under the given prefix.
     *     </li>
     * </ul>
     *
     * @param context     The storage context to use.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Instruction(interopService = SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, String prefix, byte findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>}
     *         will be returned, where each {@code Struct} is a key-value pair found under the
     *         given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>}
     *         where each {@code ByteString} is a key found under the given prefix. The prefix is
     *         part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an
     *          {@code Iterator<Struct<ByteString, ByteString>>}, where each {@code Struct}
     *          is a key-value pair found under the given prefix but the prefix is removed from
     *          the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an
     *          {@code Iterator<ByteString>}, where each {@code ByteString} is a value found
     *          under the given prefix.
     *     </li>
     * </ul>
     *
     * @param context     The storage context to use.
     * @param prefix      The key prefix.
     * @param findOptions Controls the kind of iterator to return. Use the values of
     *                    {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     */
    @Instruction(interopService = SYSTEM_STORAGE_FIND)
    public static native Iterator find(StorageContext context, int prefix, byte findOptions);

    // endregion find

}
