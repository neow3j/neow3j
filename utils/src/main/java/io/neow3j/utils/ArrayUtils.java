package io.neow3j.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArrayUtils {

    public static byte[] reverseArray(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
        return array;
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
        // create an empty array with the combined
        // size of array a and array b
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

    public static byte[] toByteArray(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }


}
