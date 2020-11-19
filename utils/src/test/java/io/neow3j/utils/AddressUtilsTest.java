package io.neow3j.utils;

import io.neow3j.contract.ScriptHash;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.crypto.Hash;
import io.neow3j.model.NeoConfig;
import org.junit.Test;

public class AddressUtilsTest {

    @Test
    public void scriptHashToAddress() {
        NeoConfig.setAddressVersion((byte) 0x17);
        String script =
            "2102208aea0068c429a03316e37be0e3e8e21e6cda5442df4c5914a19b3a9b6de37568747476aa";
        byte[] scriptHash = Hash.sha256AndThenRipemd160(Numeric.hexStringToByteArray(script));
        String address = AddressUtils.scriptHashToAddress(scriptHash);
        // Used neo-core with address version 0x17 to generate this address.
        String expectedAddress = "Aa63RMYRWHPRcrZNzUnq5SNrPqoV866Spu";
        assertThat(address, is(expectedAddress));
    }

    @Test
    public void scriptHashToAddressWithAddressVersion() {
        String script =
            "21030529d1296dc2af1f77d8344138a77748599b69599af7ae6be57812a4ec3fa33968747476aa";
        byte[] scriptHash = Hash.sha256AndThenRipemd160(Numeric.hexStringToByteArray(script));
        String address =
            AddressUtils.scriptHashToAddress(scriptHash);
        String expectedAddress = "PRivaTenetyWuqK7Gj7Vd747d77ssYeDhL";
        assertThat(address, is(expectedAddress));
    }

    @Test
    public void addressToScriptHash() {
        byte[] scriptHash = AddressUtils.addressToScriptHash("NeE8xcV4ohHi9rjyj4nPdCYTGyXnWZ79UU");
        String script =
            "2102208aea0068c429a03316e37be0e3e8e21e6cda5442df4c5914a19b3a9b6de37568747476aa";
        byte[] expected = Hash.sha256AndThenRipemd160(Numeric.hexStringToByteArray(script));
        assertArrayEquals(scriptHash, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addressToScriptHash_InvalidAddress() {
        AddressUtils.addressToScriptHash("NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToScriptHashLargerThan25Chars() {
        AddressUtils.addressToScriptHash("NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88ue");
    }

    @Test
    public void testScriptHashToAddress() {
        byte[] scriptHash = Numeric.hexStringToByteArray("d336d7eb9975a29b2404fdb28185e277a4b299bc");
        String address = "NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88u";
        assertThat(AddressUtils.scriptHashToAddress(scriptHash), is(address));
    }

    @Test
    public void testScriptHashToAddressWithVersion() {
        NeoConfig.setAddressVersion((byte) 0x37);
        byte[] scriptHash = Numeric.hexStringToByteArray("bbf14c0d508108e56402116aede9942a064f7dc6");
        String address = "PRivaTenetyWuqK7Gj7Vd747d77ssYeDhL";
        assertThat(AddressUtils.scriptHashToAddress(scriptHash), is(address));
    }

    @Test
    public void testIsValidAddress() {
        assertTrue(AddressUtils.isValidAddress("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertTrue(AddressUtils.isValidAddress("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));

        assertFalse(AddressUtils.isValidAddress(""));
        assertFalse(AddressUtils.isValidAddress("0"));
        assertFalse(AddressUtils.isValidAddress("b2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU"));
        assertFalse(AddressUtils.isValidAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmyU"));

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