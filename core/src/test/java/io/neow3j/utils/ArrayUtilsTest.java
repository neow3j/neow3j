package io.neow3j.utils;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.getFirstNBytes;
import static io.neow3j.utils.ArrayUtils.getLastNBytes;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.ArrayUtils.toByteArray;
import static io.neow3j.utils.ArrayUtils.toPrimitive;
import static io.neow3j.utils.ArrayUtils.trimLeadingBytes;
import static io.neow3j.utils.ArrayUtils.trimLeadingZeroes;
import static io.neow3j.utils.ArrayUtils.xor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayUtilsTest {

    @Test
    public void testToPrimitive() {
        byte b1 = 0x01;
        byte b2 = 0x02;
        byte b3 = 0x03;
        Byte[] testArray = new Byte[]{b1, b2, b3};
        byte[] arrayOfBytesPrimitive = toPrimitive(testArray);

        assertThat(arrayOfBytesPrimitive[0], is(b1));
        assertThat(arrayOfBytesPrimitive[1], is(b2));
        assertThat(arrayOfBytesPrimitive[2], is(b3));
    }

    @Test
    public void testToPrimitive_Empty() {
        Byte[] testArray = new Byte[]{};
        byte[] arrayOfBytesPrimitive = toPrimitive(testArray);

        assertThat(arrayOfBytesPrimitive.length, is(0));
    }

    @Test
    public void testToPrimitive_Null() {
        Byte[] testArray = null;
        byte[] arrayOfBytesPrimitive = toPrimitive(testArray);

        assertThat(arrayOfBytesPrimitive, nullValue());
    }

    @Test
    public void testTrimLeadingZeroes() {
        assertThat(trimLeadingZeroes(new byte[]{}), CoreMatchers.is(new byte[]{}));
        assertThat(trimLeadingZeroes(new byte[]{0}), CoreMatchers.is(new byte[]{0}));
        assertThat(trimLeadingZeroes(new byte[]{1}), CoreMatchers.is(new byte[]{1}));
        assertThat(trimLeadingZeroes(new byte[]{0, 1}), CoreMatchers.is(new byte[]{1}));
        assertThat(trimLeadingZeroes(new byte[]{0, 0, 1}), CoreMatchers.is(new byte[]{1}));
        assertThat(trimLeadingZeroes(new byte[]{0, 0, 1, 0}), CoreMatchers.is(new byte[]{1, 0}));
    }

    @Test
    public void testXor() {
        byte a1 = 0x01;
        byte a2 = 0x10;
        byte b1 = 0x02;
        byte b2 = 0x11;
        byte[] xorResult = xor(new byte[]{a1, a2}, new byte[]{b1, b2});

        assertThat(xorResult, is(new byte[]{0x03, 0x01}));
    }

    @Test
    public void testXor_Different_Sizes() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> xor(new byte[]{0x01, 0x02}, new byte[]{0x01}));
        assertThat(thrown.getMessage(), is("Arrays do not have the same length to perform the XOR operation."));
    }

    @Test
    public void testGetFirstNBytes() {
        assertThat(getFirstNBytes(new byte[]{0x01, 0x02, 0x03}, 2), is(new byte[]{0x01, 0x02}));
        assertThat(getFirstNBytes(new byte[]{0x01, 0x02, 0x03}, 3), is(new byte[]{0x01, 0x02,
                0x03}));
        assertThat(getFirstNBytes(new byte[]{0x01, 0x02, 0x03}, 0), is(new byte[]{}));
        assertThat(getFirstNBytes(new byte[]{}, 2), is(new byte[]{}));
        assertThat(getFirstNBytes(null, 2), is(new byte[]{}));
    }

    @Test
    public void testGetLastNBytes() {
        assertThat(getLastNBytes(new byte[]{0x01, 0x02, 0x03}, 2), is(new byte[]{0x02, 0x03}));
        assertThat(getLastNBytes(new byte[]{0x01, 0x02, 0x03}, 3), is(new byte[]{0x01, 0x02,
                0x03}));
        assertThat(getLastNBytes(new byte[]{0x01, 0x02, 0x03}, 0), is(new byte[]{}));
        assertThat(getLastNBytes(new byte[]{}, 2), is(new byte[]{}));
        assertThat(getLastNBytes(null, 2), is(new byte[]{}));
    }

    @Test
    public void testConcatenate() {
        assertThat(concatenate(new byte[]{0x01, 0x02, 0x03}, new byte[]{0x04}),
                is(new byte[]{0x01, 0x02, 0x03, 0x04}));
        assertThat(concatenate(new byte[]{0x01, 0x02, 0x03}, (byte) 0x04), is(new byte[]{0x01,
                0x02, 0x03, 0x04}));
        assertThat(concatenate((byte) 0x04, new byte[]{0x01, 0x02, 0x03}), is(new byte[]{0x04,
                0x01, 0x02, 0x03}));
        assertThat(concatenate(new byte[]{0x01, 0x02, 0x03}, new byte[]{0x01, 0x02, 0x03},
                new byte[]{0x04}), is(new byte[]{0x01, 0x02, 0x03, 0x01, 0x02, 0x03, 0x04}));
        assertThat(concatenate(new byte[]{}, new byte[]{0x01, 0x02, 0x03}, new byte[]{},
                new byte[]{0x01}), is(new byte[]{0x01, 0x02, 0x03, 0x01}));
    }

    @Test
    public void testReverseArray() {
        assertThat(reverseArray(new byte[]{0x01, 0x02, 0x03}), is(new byte[]{0x03, 0x02, 0x01}));
        assertThat(reverseArray(new byte[]{0x01, 0x02}), is(new byte[]{0x02, 0x01}));
        assertThat(reverseArray(new byte[]{0x01}), is(new byte[]{0x01}));
        assertThat(reverseArray(new byte[]{}), is(new byte[]{}));
    }

    @Test
    public void testReverseArray_Null() {
        assertThrows(NullPointerException.class, () -> reverseArray(null));
    }

    @Test
    public void testTrimLeadingBytes() {
        assertThat(trimLeadingBytes(new byte[]{0x01, 0x02, 0x03}, (byte) 0x01),
                is(new byte[]{0x02, 0x03}));
        assertThat(trimLeadingBytes(new byte[]{0x01, 0x02, 0x03}, (byte) 0x02),
                is(new byte[]{0x01, 0x02, 0x03}));
        assertThat(trimLeadingBytes(new byte[]{0x05, 0x02, 0x03}, (byte) 0x02),
                is(new byte[]{0x05, 0x02, 0x03}));
        assertThat(trimLeadingBytes(new byte[]{0x05, 0x02, 0x03}, (byte) 0x05),
                is(new byte[]{0x02, 0x03}));
        assertThat(trimLeadingBytes(new byte[]{0x05, 0x02, 0x03}, (byte) 0x05),
                is(new byte[]{0x02, 0x03}));
        assertThat(trimLeadingBytes(new byte[]{0x05, 0x02, 0x05, 0x03}, (byte) 0x05),
                is(new byte[]{0x02, 0x05, 0x03}));
    }

    @Test
    public void testToByteArray() {
        assertThat(toByteArray(0), is(new byte[]{0x00, 0x00, 0x00, 0x00}));
        assertThat(toByteArray(16), is(new byte[]{0x00, 0x00, 0x00, 0x10}));
        assertThat(toByteArray(255), is(new byte[]{0x00, 0x00, 0x00, (byte) 0xFF}));
        assertThat(toByteArray(2147483647), is(new byte[]{0x7F, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF}));
    }

    @Test
    public void longToByteArray() {
        assertThat(toByteArray(0L),
                is(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
        assertThat(toByteArray(16L),
                is(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10}));
        assertThat(toByteArray(255L),
                is(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF}));
        assertThat(toByteArray(2147483647L),
                is(new byte[]{0x00, 0x00, 0x00, 0x00, 0x7F, (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF}));
    }

    @Test
    public void trimTrailingBytes() {
        assertThat(ArrayUtils.trimTrailingBytes(new byte[]{0x01, 0x02, 0x03}, (byte) 0x03),
                is(new byte[]{0x01, 0x02}));
        assertThat(ArrayUtils.trimTrailingBytes(new byte[]{0x05, 0x02, 0x02}, (byte) 0x02),
                is(new byte[]{0x05}));
        assertThat(ArrayUtils.trimTrailingBytes(new byte[]{0x05, 0x02, 0x03}, (byte) 0x02),
                is(new byte[]{0x05, 0x02, 0x03}));
    }

}
