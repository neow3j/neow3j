package io.neow3j.io;

import java.util.List;

public class IOUtils {

    /**
     * Gets the byte size of the given number when serialized.
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
     * Gets the byte size of the given list when serialized.
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

}
