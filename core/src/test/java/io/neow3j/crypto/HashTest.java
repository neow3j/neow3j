package io.neow3j.crypto;

import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HashTest {

    @Test
    public void testSha3() {
        byte[] input = Numeric.hexStringToByteArray("68656c6c6f20776f726c64");
        byte[] expected = Numeric.hexStringToByteArray(
                        "47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad");
        byte[] result = Hash.sha3(input);
        assertThat(result, is(expected));
    }

    @Test
    public void testSha3HashHex() {
        assertThat(Hash.sha3(""),
                is("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"));

        assertThat(Hash.sha3("68656c6c6f20776f726c64"),
                is("0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    public void testSha3String() {
        assertThat(Hash.sha3String(""),
                is("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"));

        assertThat(Hash.sha3String("EVWithdraw(address,uint256,bytes32)"),
                is("0x953d0c27f84a9649b0e121099ffa9aeb7ed83e65eaed41d3627f895790c72d41"));
    }

    @Test
    public void testByte() {
        assertThat(Numeric.asByte(0x0, 0x0), is((byte) 0x0));
        assertThat(Numeric.asByte(0x1, 0x0), is((byte) 0x10));
        assertThat(Numeric.asByte(0xf, 0xf), is((byte) 0xff));
        assertThat(Numeric.asByte(0xc, 0x5), is((byte) 0xc5));
    }

    @Test
    public void testRipeMD160() {
        final String expected = "c5d570cb5b85319dbafc0385b998fd98eb62295e";
        final byte[] bytesToHash = "Hello World.".getBytes();
        final byte[] result = Hash.ripemd160(bytesToHash);
        assertThat(Numeric.toHexStringNoPrefix(result), is(expected));
    }

    @Test
    public void testRipeMD160String() {
        final String expected = "0xc5d570cb5b85319dbafc0385b998fd98eb62295e";
        final String hexStringToHash = Numeric.toHexString("Hello World.".getBytes());
        final String result = Hash.ripemd160(hexStringToHash);
        assertThat(result, is(expected));
    }

}
