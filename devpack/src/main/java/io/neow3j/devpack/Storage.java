package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.constants.OpCode;
import io.neow3j.devpack.constants.StackItemType;

import static io.neow3j.devpack.constants.InteropService.SYSTEM_STORAGE_LOCAL_DELETE;
import static io.neow3j.devpack.constants.InteropService.SYSTEM_STORAGE_LOCAL_FIND;
import static io.neow3j.devpack.constants.InteropService.SYSTEM_STORAGE_LOCAL_GET;
import static io.neow3j.devpack.constants.InteropService.SYSTEM_STORAGE_LOCAL_PUT;

/**
 * Provides a set of methods to insert, query, and delete data in the persistent storage of smart contracts.
 * <p>
 * Note that the size limit for storage keys and values is 64 bytes and 65535 bytes, respectively.
 */
public class Storage {

    // region get bytearray key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ByteString get(byte[] key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash160}.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash160.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash160 getHash160(byte[] key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash256}.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash256.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash256 getHash256(byte[] key);

    /**
     * Returns the value corresponding to the given key as a {@code ECPoint}.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a ECPoint.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ECPoint getECPoint(byte[] key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to a byte array.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public static native byte[] getByteArray(byte[] key);

    /**
     * Returns the value corresponding to the given key as a string.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a string.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native String getString(byte[] key);

    /**
     * Returns the value corresponding to the given key as a boolean.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a boolean.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public static native Boolean getBoolean(byte[] key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getInt(byte[] key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getIntOrZero(byte[] key);

    // endregion get bytearray key
    // region get bytestring type key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ByteString get(ByteStringType key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash160.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash160 getHash160(ByteStringType key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash256.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash256 getHash256(ByteStringType key);

    /**
     * Returns the value corresponding to the given key as a {@code ECPoint}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a ECPoint.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ECPoint getECPoint(ByteStringType key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to a byte array.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public static native byte[] getByteArray(ByteStringType key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a string.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native String getString(ByteStringType key);

    /**
     * Returns the value corresponding to the given key as a boolean.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a boolean.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public static native Boolean getBoolean(ByteStringType key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getInt(ByteStringType key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getIntOrZero(ByteStringType key);

    // endregion get bytestring key
    // region get string key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key.
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ByteString get(String key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash160.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash160 getHash160(String key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash256.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash256 getHash256(String key);

    /**
     * Returns the value corresponding to the given key as a {@code ECPoint}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a ECPoint.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ECPoint getECPoint(String key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to a byte array.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public static native byte[] getByteArray(String key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a string.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native String getString(String key);

    /**
     * Returns the value corresponding to the given key as a boolean.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a boolean.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public static native Boolean getBoolean(String key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getInt(String key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getIntOrZero(String key);

    // endregion get string key
    // region get integer key

    /**
     * Returns the value corresponding to the given key.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ByteString get(int key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash160.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash160 getHash160(int key);

    /**
     * Returns the value corresponding to the given key as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a Hash256.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native Hash256 getHash256(int key);

    /**
     * Returns the value corresponding to the given key as a {@code ECPoint}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a ECPoint.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native ECPoint getECPoint(int key);

    /**
     * Returns the value corresponding to the given key and converts it to a byte array.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to a byte array.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to a byte array.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public static native byte[] getByteArray(int key);

    /**
     * Returns the value corresponding to the given key as a string.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a string.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    public static native String getString(int key);

    /**
     * Returns the value corresponding to the given key as a boolean.
     * <p>
     * This does not incur any extra GAS costs.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key as a boolean.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public static native Boolean getBoolean(int key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getInt(int key);

    /**
     * Returns the value corresponding to the given key and converts it to an integer. The bytes are read in
     * little-endian format. E.g., the byte string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key the key to get the value for.
     * @return the value corresponding to the given key converted to an integer.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public static native Integer getIntOrZero(int key);

    // endregion
    // region put bytearray key

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(byte[] key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(byte[] key, boolean value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(byte[] key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(byte[] key, ByteStringType value);

    // endregion put bytearray key
    // region put bytestring type key

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(ByteStringType key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(ByteStringType key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(ByteStringType key, boolean value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(ByteStringType key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(ByteStringType key, ByteStringType value);

    // endregion
    // region put string key

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(String key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(String key, boolean value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(String key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(String key, ByteStringType value);

    // endregion put string key
    // region put integer key

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(int key, byte[] value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(int key, int value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(int key, boolean value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(int key, String value);

    /**
     * Stores the given key-value pair.
     *
     * @param key   the key.
     * @param value the value to store.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_PUT)
    public static native void put(int key, ByteStringType value);

    // endregion put integer key
    // region delete

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param key the key to delete.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_DELETE)
    public static native void delete(byte[] key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param key the key to delete.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_DELETE)
    public static native void delete(int key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param key the key to delete.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_DELETE)
    public static native void delete(String key);

    /**
     * Deletes the value corresponding to the given key from the storage.
     *
     * @param key the key to delete.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_DELETE)
    public static native void delete(ByteStringType key);

    // endregion delete
    // region find

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>} will be returned,
     *         where each {@code Struct} is a key-value pair found under the given prefix. The prefix is part of the
     *         key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>} where each
     *         {@code ByteString} is a key found under the given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an {@code Iterator<Struct<ByteString,
     *          ByteString>>}, where each {@code Struct} is a key-value pair found under the given prefix but the
     *          prefix is removed from the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an {@code Iterator<ByteString>}, where each
     *          {@code ByteString} is a value found under the given prefix.
     *     </li>
     * </ul>
     *
     * @param prefix      the key prefix.
     * @param findOptions the find options to control the kind of iterator to return.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_FIND)
    public static native Iterator find(byte[] prefix, int findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>} will be returned,
     *         where each {@code Struct} is a key-value pair found under the given prefix. The prefix is part of the
     *         key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>} where each
     *         {@code ByteString} is a key found under the given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an {@code Iterator<Struct<ByteString,
     *          ByteString>>}, where each {@code Struct} is a key-value pair found under the given prefix but the
     *          prefix is removed from the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an {@code Iterator<ByteString>}, where each
     *          {@code ByteString} is a value found under the given prefix.
     *     </li>
     * </ul>
     *
     * @param prefix      the key prefix.
     * @param findOptions the find options to control the kind of iterator to return.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_FIND)
    public static native Iterator find(int prefix, int findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>} will be returned,
     *         where each {@code Struct} is a key-value pair found under the given prefix. The prefix is part of the
     *         key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>} where each
     *         {@code ByteString} is a key found under the given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an {@code Iterator<Struct<ByteString,
     *          ByteString>>}, where each {@code Struct} is a key-value pair found under the given prefix but the
     *          prefix is removed from the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an {@code Iterator<ByteString>}, where each
     *          {@code ByteString} is a value found under the given prefix.
     *     </li>
     * </ul>
     *
     * @param prefix      the key prefix.
     * @param findOptions the find options to control the kind of iterator to return.
     * @return an iterator over key, values or key-value pairs found under the given prefix.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_FIND)
    public static native Iterator find(String prefix, int findOptions);

    /**
     * Returns an iterator over the values found under the given key prefix.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>} will be returned,
     *         where each {@code Struct} is a key-value pair found under the given prefix. The prefix is part of the
     *         key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>} where each
     *         {@code ByteString} is a key found under the given prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an {@code Iterator<Struct<ByteString,
     *          ByteString>>}, where each {@code Struct} is a key-value pair found under the given prefix but the
     *          prefix is removed from the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an {@code Iterator<ByteString>}, where each
     *          {@code ByteString} is a value found under the given prefix.
     *     </li>
     * </ul>
     *
     * @param prefix      the key prefix.
     * @param findOptions the find options to control the kind of iterator to return.
     * @return an iterator over key, values, or key-value pairs found under the given prefix.
     * @since 3.24.1
     */
    @Instruction(interopService = SYSTEM_STORAGE_LOCAL_FIND)
    public static native Iterator find(ByteStringType prefix, int findOptions);

    // endregion find

}
