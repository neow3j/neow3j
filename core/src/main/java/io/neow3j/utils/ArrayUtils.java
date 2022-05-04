package io.neow3j.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArrayUtils {

    public static byte[] reverseArray(byte[] array) {
        byte[] copy = new byte[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        for (int i = 0; i < copy.length / 2; i++) {
            byte temp = copy[i];
            copy[i] = copy[copy.length - i - 1];
            copy[copy.length - i - 1] = temp;
        }
        return copy;
    }

    public static byte[] concatenate(byte[] a, byte b) {
        return concatenate(a, new byte[]{b});
    }

    public static byte[] concatenate(byte a, byte[] b) {
        return concatenate(new byte[]{a}, b);
    }

    public static byte[] concatenate(byte[]... arrays) {
        byte[] result = new byte[0];
        for (byte[] array : arrays) {
            if (array != null) {
                result = concatenate(result, array);
            }
        }
        return result;
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        // create an empty array with the combined size of array a and array b
        byte[] n = new byte[a.length + b.length];
        // copy the array a into n
        System.arraycopy(a, 0, n, 0, a.length);
        // copy the array b into n
        System.arraycopy(b, 0, n, a.length, b.length);
        return n;
    }

    public static byte[] getFirstNBytes(byte[] array, int nBytes) {
        if (array != null && array.length > 0) {
            return Arrays.copyOfRange(array, 0, nBytes);
        }
        return new byte[]{};
    }

    public static byte[] getLastNBytes(byte[] array, int nBytes) {
        if (array != null && array.length > 0) {
            return Arrays.copyOfRange(array, array.length - nBytes, array.length);
        }
        return new byte[]{};
    }

    public static byte[] xor(byte[] array1, byte[] array2) throws IllegalArgumentException {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("Arrays do not have the same length to perform the XOR operation.");
        }
        byte[] result = new byte[array1.length];
        int i = 0;
        for (byte b : array1) {
            result[i] = (byte) (b ^ array2[i++]);
        }
        return result;
    }

    public static byte[] trimLeadingBytes(byte[] bytes, byte b) {
        int offset = 0;
        for (; offset < bytes.length - 1; offset++) {
            if (bytes[offset] != b) {
                break;
            }
        }
        return Arrays.copyOfRange(bytes, offset, bytes.length);
    }

    public static byte[] trimLeadingZeroes(byte[] bytes) {
        return trimLeadingBytes(bytes, (byte) 0);
    }

    public static byte[] trimTrailingBytes(byte[] bytes, byte b) {
        int offset = bytes.length - 1;
        for (; offset > 0; offset--) {
            if (bytes[offset] != b) {
                break;
            }
        }
        return Arrays.copyOfRange(bytes, 0, offset + 1);
    }

    public static byte[] toPrimitive(final Byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new byte[0];
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].byteValue();
        }
        return result;
    }

    /**
     * Gets the given int as a byte array of length 4 in big-endian format.
     *
     * @param v the integer.
     * @return byte array of length 4.
     */
    public static byte[] toByteArray(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }

    /**
     * Gets the given long as a byte array of length 8 in big-endian format.
     *
     * @param v the long.
     * @return byte array of length 8.
     */
    public static byte[] toByteArray(long v) {
        return ByteBuffer.allocate(8).putLong(v).array();
    }

}
