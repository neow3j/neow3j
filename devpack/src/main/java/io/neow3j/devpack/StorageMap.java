package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.devpack.constants.InteropService;
import io.neow3j.devpack.constants.OpCode;
import io.neow3j.devpack.constants.StackItemType;

/**
 * A key-value view on the entries of smart contract's storage with a specific prefix.
 * <p>
 * Note that the storage size limit is 64 bytes for prefix + key and 65535 bytes for the value.
 */
public class StorageMap {

    private StorageContext context;
    private Object prefix;

    // region constructors

    /**
     * Constructs a new {@code StorageMap} from entries with the given prefix in the given {@link StorageContext}.
     *
     * @param context the storage to look for the entries.
     * @param prefix  the prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PACK)
    public StorageMap(StorageContext context, ByteStringType prefix) {
    }

    /**
     * Constructs a new {@code StorageMap} from entries with the given prefix in the given {@link StorageContext}.
     *
     * @param context the storage to look for the entries.
     * @param prefix  the prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PACK)
    public StorageMap(StorageContext context, byte[] prefix) {
    }

    /**
     * Constructs a new {@code StorageMap} from entries with the given prefix in the given {@link StorageContext}.
     *
     * @param context the storage to look for the entries.
     * @param prefix  the prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PACK)
    public StorageMap(StorageContext context, String prefix) {
    }

    /**
     * Constructs a new {@code StorageMap} from entries with the given prefix in the given {@link StorageContext}.
     *
     * @param context the storage to look for the entries.
     * @param prefix  the prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PACK)
    public StorageMap(StorageContext context, int prefix) {
    }

    /**
     * Constructs a new {@code StorageMap} from entries with the given prefix in the given {@link StorageContext}.
     *
     * @param context the storage to look for the entries.
     * @param prefix  the prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PACK)
    public StorageMap(StorageContext context, byte prefix) {
    }

    // endregion
    // region get bytearray key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ByteString get(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash160 getHash160(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash256 getHash256(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ECPoint getECPoint(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context and converts it to
     * a byte array.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public native byte[] getByteArray(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a string.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native String getString(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a boolean.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key as a boolean.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public native Boolean getBoolean(byte[] key);

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
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getInt(byte[] key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getIntOrZero(byte[] key);

    // endregion
    // region get bytestring type key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ByteString get(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash160}.
     * <p>
     * <b>Does not check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} to verify.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash160 getHash160(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash256 getHash256(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ECPoint getECPoint(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public native byte[] getByteArray(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native String getString(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a boolean.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a boolean.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public native Boolean getBoolean(ByteStringType key);

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
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getInt(ByteStringType key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getIntOrZero(ByteStringType key);

    // endregion get bytestring key
    // region get string key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ByteString get(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash160 getHash160(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash256 getHash256(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ECPoint getECPoint(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public native byte[] getByteArray(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native String getString(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a boolean.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a boolean.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public native Boolean getBoolean(String key);

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
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getInt(String key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getIntOrZero(String key);

    // endregion
    // region get integer key

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ByteString get(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash160}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash160}.</b> Use {@link Hash160#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash160 getHash160(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code Hash256}.</b> Use {@link Hash256#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native Hash256 getHash256(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context as a {@code Hash256}.
     * <p>
     * <b>Does NOT check if the value is a valid {@code ECPoint}.</b> Use {@link ECPoint#isValid(Object)} in order to
     * verify the correct format.
     *
     * @param key the key of the value to retrieve.
     * @return the value corresponding to the given key.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native ECPoint getECPoint(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to a byte array.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to a byte array.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.BUFFER)
    public native byte[] getByteArray(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a string.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a string.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    public native String getString(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * as a boolean.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key as a boolean.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.NOT)
    @Instruction(opcode = OpCode.NOT)
    public native Boolean getBoolean(int key);

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
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getInt(int key);

    /**
     * Gets the value with a key equal to {@code prefix + key} from the underlying storage context
     * and converts it to an integer. The bytes are read in little-endian format. E.g., the byte
     * string {@code 0102} (in hexadecimal representation) is converted to 513.
     * <p>
     * Returns 0, if no value is found for the provided key.
     * <p>
     * This incurs the GAS cost of converting the {@code ByteString} value to an integer.
     *
     * @param key The key of the value to retrieve.
     * @return the value corresponding to the given key converted to an integer.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_GET)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.ISNULL)
    @Instruction(opcode = OpCode.JMPIFNOT, operand = 0x06)
    @Instruction(opcode = OpCode.DROP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.JMP, operand = 0x04)
    @Instruction(opcode = OpCode.CONVERT, operand = StackItemType.INTEGER)
    public native Integer getIntOrZero(int key);

    // endregion
    // region put bytearray key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(byte[] key, byte[] value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(byte[] key, int value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code prefix + key}) into the
     * underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(byte[] key, boolean value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(byte[] key, ByteStringType value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(byte[] key, String value);

    // endregion
    // region put bytestring type key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(ByteStringType key, byte[] value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(ByteStringType key, int value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code prefix + key}) into the
     * underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(ByteStringType key, boolean value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(ByteStringType key, ByteStringType value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(ByteStringType key, String value);

    // endregion
    // region put string key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(String key, String value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(String key, byte[] value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(String key, int value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code prefix + key}) into the
     * underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(String key, boolean value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(String key, ByteStringType value);

    // endregion
    // region put integer key

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(int key, byte[] value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(int key, int value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code prefix + key}) into the
     * underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(int key, boolean value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(int key, ByteStringType value);

    /**
     * Stores the given key-value pair prefixed with this {@code StorageMap}'s prefix ({@code
     * prefix + key}) into the underlying storage context.
     *
     * @param key   The key of the entry.
     * @param value The value of the entry.
     */
    @Instruction(opcode = OpCode.PUSH2)
    @Instruction(opcode = OpCode.PICK)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.ROT)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_PUT)
    public native void put(int key, String value);

    // endregion
    // region delete

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_DELETE)
    public native void delete(byte[] key);

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_DELETE)
    public native void delete(int key);

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_DELETE)
    public native void delete(String key);

    /**
     * Deletes the entry with a key equal to {@code prefix + key} from the underlying storage
     * context.
     *
     * @param key The key to delete.
     */
    @Instruction(opcode = OpCode.OVER)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.CAT)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_DELETE)
    public native void delete(ByteStringType key);

    // endregion delete
    // region find

    /**
     * Returns an iterator over the values in this {@code StorageMap}.
     * <p>
     * The types that the {@code Iterator} contains are dependent on the find options used.
     * <ul>
     *     <li>
     *         With {@link FindOptions#None} an {@code Iterator<Struct<ByteString, ByteString>>}
     *         will be returned, where each {@code Struct} is a key-value pair found under this
     *         map's prefix. The prefix is part of the key.
     *     </li>
     *     <li>
     *         With {@link FindOptions#KeysOnly} the results will be an {@code Iterator<ByteString>}
     *         where each {@code ByteString} is a key found under this map's prefix. The prefix is
     *         part of the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#RemovePrefix} the results will be an
     *          {@code Iterator<Struct<ByteString, ByteString>>}, where each {@code Struct}
     *          is a key-value pair found under this map's prefix but the prefix is removed from
     *          the key.
     *     </li>
     *     <li>
     *          With {@link FindOptions#ValuesOnly} the results will be an
     *          {@code Iterator<ByteString>}, where each {@code ByteString} is a value found
     *          under this map's prefix.
     *     </li>
     * </ul>
     *
     * @param findOptions Controls the kind of iterator to return. Use the values of {@link FindOptions}.
     * @return an iterator over key, values or key-value pairs found under this map's prefix.
     */
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.DUP)
    @Instruction(opcode = OpCode.PUSH1)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(opcode = OpCode.SWAP)
    @Instruction(opcode = OpCode.PUSH0)
    @Instruction(opcode = OpCode.PICKITEM)
    @Instruction(interopService = InteropService.SYSTEM_STORAGE_FIND)
    public native Iterator find(int findOptions);

    // endregion find

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
        // In case the prefixes are Buffer stack items we need to convert them to ByteStrings to
        // be able to compare them.
        Object checkedPrefix = prefix;
        Object checkdPrefixOther = map.prefix;
        if (checkedPrefix instanceof byte[]) {
            checkedPrefix = new ByteString((byte[]) checkedPrefix);
        }
        if (checkdPrefixOther instanceof byte[]) {
            checkdPrefixOther = new ByteString((byte[]) checkdPrefixOther);
        }
        return context.equals(map.context) &&
                // Behaviour depends on the actual stack item type of the prefixes
                checkedPrefix == checkdPrefixOther;
    }

}
