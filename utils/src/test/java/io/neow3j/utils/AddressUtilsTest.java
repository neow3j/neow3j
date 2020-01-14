package io.neow3j.utils;

import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class AddressUtilsTest {


    @Test
    public void scriptHashToAddress() {
        byte[] scriptHash = Numeric
                .hexStringToByteArray("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(
                AddressUtils.scriptHashToAddress(scriptHash),
                is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
        );
    }

    @Test
    public void addressToScriptHash() {
        byte[] scriptHash = Numeric
                .hexStringToByteArray("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(
                AddressUtils.addressToScriptHash("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"),
                is(scriptHash)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToScriptHashLargerThan25Chars() {
        AddressUtils.addressToScriptHash("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8yyyy");
    }

    @Test
    public void testScriptHashToAddress() {
        byte[] script = Numeric.hexStringToByteArray("d336d7eb9975a29b2404fdb28185e277a4b299bc");
        String address = "Ab2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU";

        assertThat(AddressUtils.scriptHashToAddress(script), is(address));
    }

    @Test
    public void testIsValidAddress() {
        assertTrue(AddressUtils.isValidAddress("Ab2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU"));
        assertTrue(AddressUtils.isValidAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"));

        assertFalse(AddressUtils.isValidAddress(""));
        assertFalse(AddressUtils.isValidAddress("0"));
        assertFalse(AddressUtils.isValidAddress("b2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU"));
        assertFalse(AddressUtils.isValidAddress("AAb2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU"));

        // If the address string is null, we don't want to say it is an invalid address because
        // there isn't even an address to be deemed invalid. Therefore expect NullPointerException.
        try {
            AddressUtils.isValidAddress(null);
        } catch (NullPointerException e) {
            return;
        }
        fail();
    }
}