package io.neow3j.io;

import java.util.List;

public class IOUtils {

    /**
     * Gets the number of bytes the given integer takes in its serialized form. This is only for
     * integers that define the size of a list or some variable-sized data in their serialized
     * form.
     *
     * @param i The integer.
     * @return the byte size of the integer in its serialized form.
     */
    public static int getSizeOfVarInt(int i) {
        if (i < 0xFD) {
            return 1; // Size can be written in one byte.
        } else if (i <= 0xFFFF) {
            return 1 + 2; // Size is written as byte plus uint16.
        } else {
            return 1 + 4; // Size is written as byte plus uint32.
        }
        // We are not testing for (s <= 0xFFFFFFFF) because the size of a list cannot be that big.
    }

    /**
     * Gets the number of bytes that the given list will take in its serialized form. The
     * calculation includes the variable sized integer used to define the size of the list.
     *
     * @param serializables The list of serializables.
     * @return the number of bytes taken by the list in its serialized form.
     */
    public static int getSizeOfVarList(List<? extends NeoSerializable> serializables) {
        int sizeByteSize = getSizeOfVarInt(serializables.size());
        int objectsByteSize = serializables.stream()
            .map(NeoSerializableInterface::getSize)
            .reduce(0, Integer::sum);
        return sizeByteSize + objectsByteSize;
    }

}
