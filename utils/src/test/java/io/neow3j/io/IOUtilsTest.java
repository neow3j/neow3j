package io.neow3j.io;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IOUtilsTest {

    @Test
    public void getSizeOfVarInt() {
        assertThat(IOUtils.getSizeOfVarInt(0), is(1));

        // v == 0xfd - 1, encode with one byte
        assertThat(IOUtils.getSizeOfVarInt(252), is(1));

        // v == 0xfd, encode with 3 bytes
        assertThat(IOUtils.getSizeOfVarInt(253), is(3));

        // v == 0xfd + 1, encode with 3 bytes
        assertThat(IOUtils.getSizeOfVarInt(254), is(3));

        // v == 0xffff - 1, encode with 3 bytes
        assertThat(IOUtils.getSizeOfVarInt(65_534), is(3));

        // v == 0xffff, encode with 3 bytes
        assertThat(IOUtils.getSizeOfVarInt(65_535), is(3));

        // v == 0xffff + 1, encode with 5 bytes
        assertThat(IOUtils.getSizeOfVarInt(65_536), is(5));

        // v == 0x7fffffff (max int), encode with 5 bytes
        assertThat(IOUtils.getSizeOfVarInt(2_147_483_647), is(5));
    }

}