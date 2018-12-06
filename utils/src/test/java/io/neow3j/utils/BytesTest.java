package io.neow3j.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BytesTest {

    @Test
    public void testTrimLeadingZeroes() {
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{}), is(new byte[]{}));
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{0}), is(new byte[]{0}));
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{1}), is(new byte[]{1}));
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{0, 1}), is(new byte[]{1}));
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{0, 0, 1}), is(new byte[]{1}));
        assertThat(ArrayUtils.trimLeadingZeroes(new byte[]{0, 0, 1, 0}), is(new byte[]{1, 0}));
    }
}
