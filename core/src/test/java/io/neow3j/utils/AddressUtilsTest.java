package io.neow3j.utils;

import io.neow3j.protocol.Neow3jConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.neow3j.crypto.Hash.sha256AndThenRipemd160;
import static io.neow3j.utils.AddressUtils.addressToScriptHash;
import static io.neow3j.utils.AddressUtils.isValidAddress;
import static io.neow3j.utils.AddressUtils.scriptHashToAddress;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AddressUtilsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCreatesScriptHashThenToAddress() {
        String script =
                "0c2102249425a06b5a1f8e6133fc79afa2c2b8430bf9327297f176761df79e8d8929c50b4195440d78";
        // sha256AndThenRipemd160 returns little-endian
        byte[] scriptHash = reverseArray(sha256AndThenRipemd160(hexStringToByteArray(script)));
        String address = scriptHashToAddress(scriptHash);
        // Used neo-core with address version 0x35 to generate this address.
        String expectedAddress = "NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke";
        assertThat(address, is(expectedAddress));
    }

    @Test
    public void testScriptHashToAddressWithAddressVersion() {
        Neow3jConfig.setAddressVersion((byte) 0x37);
        String script =
                "21030529d1296dc2af1f77d8344138a77748599b69599af7ae6be57812a4ec3fa33968747476aa";
        // sha256AndThenRipemd160 returns little-endian
        byte[] scriptHash = reverseArray(sha256AndThenRipemd160(hexStringToByteArray(script)));
        String address = scriptHashToAddress(scriptHash);
        String expectedAddress = "PRivaTenetyWuqK7Gj7Vd747d77ssYeDhL";
        assertThat(address, is(expectedAddress));
        Neow3jConfig.setAddressVersion((byte) 0x35);
    }

    @Test
    public void testAddressToScriptHash() {
        byte[] scriptHash = addressToScriptHash("NeE8xcV4ohHi9rjyj4nPdCYTGyXnWZ79UU");
        String script =
                "2102208aea0068c429a03316e37be0e3e8e21e6cda5442df4c5914a19b3a9b6de37568747476aa";
        // sha256AndThenRipemd160 returns little-endian
        byte[] expected = reverseArray(sha256AndThenRipemd160(hexStringToByteArray(script)));
        assertArrayEquals(scriptHash, expected);
    }

    @Test
    public void testAddressToScriptHash_InvalidAddress() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Not a valid NEO address.");
        addressToScriptHash("NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88");
    }

    @Test
    public void testToScriptHashLargerThan25Chars() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Not a valid NEO address.");
        addressToScriptHash("NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88ue");
    }

    @Test
    public void testScriptHashToAddress() {
        byte[] scriptHash = hexStringToByteArray("bc99b2a477e28581b2fd04249ba27599ebd736d3");
        String address = "NfAmTpaR5kxaUDs6LDBR9DWau53NsEz88u";
        assertThat(scriptHashToAddress(scriptHash), is(address));
    }

    @Test
    public void testScriptHashToAddressWithVersion() {
        Neow3jConfig.setAddressVersion((byte) 0x37);
        byte[] scriptHash = hexStringToByteArray("c67d4f062a94e9ed6a110264e50881500d4cf1bb");
        String address = "PRivaTenetyWuqK7Gj7Vd747d77ssYeDhL";
        assertThat(scriptHashToAddress(scriptHash), is(address));
        Neow3jConfig.setAddressVersion((byte) 0x35);
    }

    @Test
    public void testIsValidAddress() {
        assertTrue(isValidAddress("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertTrue(isValidAddress("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));

        assertFalse(isValidAddress(""));
        assertFalse(isValidAddress("0"));
        assertFalse(isValidAddress("b2fvZdmnM4HwDgVbdBrbTLz1wK5TcEyhU"));
        assertFalse(isValidAddress("NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmyU"));

        // If the address string is null, we don't want to say it is an invalid address because
        // there isn't even an address to be deemed invalid. Therefore expect NullPointerException.
        exceptionRule.expect(NullPointerException.class);
        isValidAddress(null);
    }

}
