package io.neow3j.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;

public class IOUtils {

    /**
     * Gets the byte size of the given number when Neo-serialized.
     *
     * @param i The number.
     * @return the byte size.
     */
    public static int getVarSize(long i) {
        if (i < 0xFD) {
            return 1; // byte
        } else if (i <= 0xFFFF) {
            return 1 + 2; // 0xFD + uint16
        } else if (i <= 0xFFFFFFFFL) {
            return 1 + 4; // 0xFE + uint32
        } else {
            return 1 + 8; // 0xFF + uint64
        }
    }

    /**
     * Gets the byte size of the given list when Neo-serialized.
     *
     * @param serializables The list of serializables.
     * @return the byte size.
     */
    public static int getVarSize(List<? extends NeoSerializable> serializables) {
        int sizeByteSize = getVarSize(serializables.size());
        int objectsByteSize = serializables.stream()
                .map(NeoSerializableInterface::getSize)
                .reduce(0, Integer::sum);
        return sizeByteSize + objectsByteSize;
    }

    /**
     * Gets the byte size of the given byte array when Neo-serialized.
     *
     * @param bytes The bytes to serialize.
     * @return the byte size.
     */
    public static int getVarSize(byte[] bytes) {
        return getVarSize(bytes.length) + bytes.length;
    }

    /**
     * Gets the byte size of the given string when Neo-serialized.
     *
     * @param value The string to serialize.
     * @return the byte size.
     */
    public static int getVarSize(String value) {
        return getVarSize(value.getBytes(UTF_8));
    }

}
