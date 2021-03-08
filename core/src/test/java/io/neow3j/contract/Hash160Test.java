package io.neow3j.contract;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class Hash160Test {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void createFromValidHash() {
        Hash160 hash = new Hash160("0x23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));

        hash = new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test
    public void createFromHashWithOddLength() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("String argument is not hexadecimal.");
        new Hash160("0x23ba2703c53263e8d6e522dc32203339dcd8eee");
    }

    @Test
    public void createFromMalformedHash() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("String argument is not hexadecimal.");
        new Hash160("g3ba2703c53263e8d6e522dc32203339dcd8eee9");
    }

    @Test
    public void createFromTooShortHash() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Hash must be 20 bytes long but was 19 bytes.");
        new Hash160("23ba2703c53263e8d6e522dc32203339dcd8ee");
    }

    @Test
    public void createFromTooLongHash() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Hash must be 20 bytes long but was 32 bytes.");
        new Hash160("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b");
    }

    @Test
    public void toArray() {
        Hash160 hash = new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        byte[] expected = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        assertArrayEquals(expected, hash.toArray());
    }

    @Test
    public void serialize() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9").serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        Hash160 hash = NeoSerializableInterface.from(data, Hash160.class);
        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test
    public void equals() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        Hash160 hash1 = Hash160.fromScript(m1);
        Hash160 hash2 = Hash160.fromScript(m2);
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash1);
        assertEquals(hash1, hash1);
    }

    @Test
    public void fromValidAddress() {
        Hash160 hash = Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        byte[] expectedHash = Numeric.hexStringToByteArray(
                "09a55874c2da4b86e5d49ff530a1b153eb12c7d6");
        assertThat(hash.toArray(), is(expectedHash));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidAddress() {
        Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8keas");
    }

    @Test
    public void fromPublicKeyByteArray() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        byte[] script = Numeric.hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.PUSHNULL.toString()
                + OpCode.SYSCALL.toString()
                + InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1.getHash()
        );

        Hash160 hash = Hash160.fromPublicKey(Numeric.hexStringToByteArray(key));
        assertThat(hash.toArray(), is(Hash.sha256AndThenRipemd160(script)));
    }

    @Test
    public void fromPublicKeyByteArrays() {
        final String key1 = "02249425a06b5a1f8e6133fc79afa2c2b8430bf9327297f176761df79e8d8929c5";
        final String key2 = "031ccaaa46df7c494f442698c8c17c09311e3615c2dc042cbd3afeaba60fa40740";
        String expectedScriptHash = "aaf6f842f8450c4226bfaee5da2fab983cfa07e6";
        List<byte[]> keys = Arrays.asList(
                Numeric.hexStringToByteArray(key1), Numeric.hexStringToByteArray(key2));
        Hash160 hash = Hash160.fromPublicKeys(keys, 2);
        assertThat(hash.toString(), is(expectedScriptHash));
    }

    @Test
    public void fromContractScript() {
        String verificationScript = "110c21026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d110b41138defaf";
        Hash160 hash = Hash160.fromScript(verificationScript);

        String bigEndian = hash.toString();
        assertThat(bigEndian, is("afaed076854454449770763a628f379721ea9808"));
        String littleEndian = Numeric.toHexStringNoPrefix(hash.toArray());
        assertThat(littleEndian, is("0898ea2197378f623a7670974454448576d0aeaf"));
    }

    @Test
    public void toAddress() {
        final String key = "031ccaaa46df7c494f442698c8c17c09311e3615c2dc042cbd3afeaba60fa40740";
        // Address generated from the above key, with address version 0x35.
        final String expectedAddress = "NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy";
        Hash160 sh = Hash160.fromPublicKey(Numeric.hexStringToByteArray(key));
        assertThat(sh.toAddress(), is(expectedAddress));
    }

    @Test
    public void compareTo() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as integer)
        byte[] m3 = Numeric.hexStringToByteArray("a7b3a191");
        Hash160 hash1 = Hash160.fromScript(m1);
        Hash160 hash2 = Hash160.fromScript(m2);
        Hash160 hash3 = Hash160.fromScript(m3);

        assertThat(hash1.compareTo(hash1), is(0));
        assertThat(hash1.compareTo(hash2), is(-1));
        assertThat(hash1.compareTo(hash3), is(-1));
        assertThat(hash2.compareTo(hash1), is(1));
        assertThat(hash2.compareTo(hash3), is(1));
        assertThat(hash3.compareTo(hash1), is(1));
        assertThat(hash3.compareTo(hash2), is(-1));
    }

    @Test
    public void getSize() {
        Hash160 hash = new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(hash.getSize(), is(20));
    }

}
