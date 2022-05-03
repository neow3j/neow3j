package io.neow3j.devpack.constants;

import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;

/**
 * These options are to be used with the {@link Storage#find(StorageContext, byte[], byte)}} methods and allow to
 * control the kind of iterator that should be returned.
 */
public class FindOptions {

    /**
     * No option is set. The results will be an {@code Iterator<Map.Entry<ByteString, ByteString>>}, where each
     * {@code Map.Entry} is a key-value pair.
     */
    public static final byte None = 0;

    /**
     * Indicates that only keys need to be returned. The results will be an {@code Iterator<ByteString>}, where each
     * {@code ByteString} is a key.
     */
    public static final byte KeysOnly = 1;

    /**
     * Indicates that the prefix of keys should be removed before return. The results will be an
     * {@code Iterator<Map.Entry<ByteString, ByteString>>}, where each {@code Map.Entry} is a key-value pair.
     */
    public static final byte RemovePrefix = 1 << 1;

    /**
     * Indicates that only values need to be returned. The results will be an {@code Iterator<ByteString>}, where
     * each {@code ByteString} is a value.
     */
    public static final byte ValuesOnly = 1 << 2;

    /**
     * Indicates that values should be deserialized before return.
     */
    public static final byte DeserializeValues = 1 << 3;

    /**
     * Indicates that only the field 0 of the deserialized values needs to be returned. This flag must be set
     * together with {@link FindOptions#DeserializeValues}.
     */
    public static final byte PickField0 = 1 << 4;

    /**
     * Indicates that only the field 1 of the deserialized values needs to be returned. This flag must be set
     * together with {@link FindOptions#DeserializeValues}.
     */
    public static final byte PickField1 = 1 << 5;

}
