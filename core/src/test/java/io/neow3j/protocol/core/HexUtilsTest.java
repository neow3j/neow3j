package io.neow3j.protocol.core;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HexUtilsTest {

    @Test
    public void testHexReverse() {
        String hex = "bc99b2a477e28581b2fd04249ba27599ebd736d3";
        String reversed = "d336d7eb9975a29b2404fdb28185e277a4b299bc";

        assertThat(HexUtils.reverse(hex), is(reversed));
    }

    @Test
    public void testHexToString() {
        String hex = "72656164";
        String original = "read";

        assertThat(HexUtils.hexToString(hex), is(original));
    }

    @Test
    public void testHexToInteger() {
        String hex = "b100";
        BigInteger original = BigInteger.valueOf(177);

        assertThat(HexUtils.hexToInteger(hex), is(original));
    }

    @Test
    public void testScriptHashToAddress() {
        String script = "d336d7eb9975a29b2404fdb28185e277a4b299bc";
        String address = "Ab2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU";

        assertThat(HexUtils.scriptHashToAddress(script), is(address));
    }
}