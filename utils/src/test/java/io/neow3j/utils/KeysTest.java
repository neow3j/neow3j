package io.neow3j.utils;

import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import static io.neow3j.utils.Keys.toAddress;
import static io.neow3j.utils.Keys.toScriptHash;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class KeysTest {

    @Test
    public void testToAddress() {
        byte[] scriptHash = Numeric.hexStringToByteArray("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(
                toAddress(scriptHash),
                is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
        );
    }

    @Test
    public void testToScriptHash() {
        byte[] scriptHash = Numeric.hexStringToByteArray("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(
                toScriptHash("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"),
                is(scriptHash)
        );
    }

    @Test
    public void testScriptHashToAddress() {
        String script = "d336d7eb9975a29b2404fdb28185e277a4b299bc";
        String address = "Ab2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU";

        assertThat(Keys.scriptHashToAddress(script), is(address));
    }

}

