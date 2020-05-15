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
import org.junit.Test;

public class ScriptHashTest {

    @Test
    public void createFromValidHash() {
        ScriptHash sh = new ScriptHash("0x23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(sh.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));

        sh = new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(sh.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromTooShortHash() {
        new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromTooLongHash() {
        new ScriptHash("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9ba");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromMidSizedHash() {
        new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee9938a3e");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFromMalformedHash() {
        new ScriptHash("g3ba2703c53263e8d6e522dc32203339dcd8eee9");
    }

    @Test
    public void toArray() {
        ScriptHash sh = new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        byte[] expected = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        assertArrayEquals(expected, sh.toArray());
    }

    @Test
    public void serialize() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee9").serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = ArrayUtils.reverseArray(Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        ScriptHash sh = NeoSerializableInterface.from(data, ScriptHash.class);
        assertThat(sh.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test
    public void equals() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = Numeric.hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = Numeric.hexStringToByteArray("d802a401");
        ScriptHash sh1 = ScriptHash.fromScript(m1);
        ScriptHash sh2 = ScriptHash.fromScript(m2);
        assertNotEquals(sh1, sh2);
        assertNotEquals(sh2, sh1);
        assertEquals(sh1, sh1);
    }

    @Test
    public void fromValidAddress() {
        byte[] expectedHash = Numeric.hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9");
        ScriptHash hash = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        assertThat(hash.toArray(), is(expectedHash));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidAddress() {
        ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8yyyy");
    }

    @Test
    public void fromPublicKeyByteArray() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        byte[] script = Numeric.hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.PUSHNULL.toString()
                + OpCode.SYSCALL.toString()
                + InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_VERIFY.getHash()
        );

        ScriptHash sh = ScriptHash.fromPublicKey(Numeric.hexStringToByteArray(key));
        assertThat(sh.toArray(), is(Hash.sha256AndThenRipemd160(script)));
    }

    @Test
    public void fromPublicKeyByteArrays() {
        final String key1 = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        final String key2 = "03eda286d19f7ee0b472afd1163d803d620a961e1581a8f2704b52c0285f6e022d";
        String expectedScriptHash = "aa20dc2168d66d5edb5de8de34da4cf3a16864c8";
        String address = "Aa3T5PGGMiSyJUzUXyJSCUUFYH1aWcrT53";
        List<byte[]> keys = Arrays.asList(
                Numeric.hexStringToByteArray(key1), Numeric.hexStringToByteArray(key2));
        ScriptHash sh = ScriptHash.fromPublicKeys(keys, 2);
        assertThat(sh.toString(), is(expectedScriptHash));
    }

    @Test
    public void fromContractScript() {
        String verificationScript = "522102028a99826edc0c97d18e22b6932373d908d323aa7f92656a77ec26" +
                "e8861699ef21031d8e1630ce640966967bc6d95223d21f44304133003140c3b52004dc981349c921" +
                "02232ce8d2e2063dce0451131851d47421bfc4fc1da4db116fca5302c0756462fa53ae";
        ScriptHash sh = ScriptHash.fromScript(verificationScript);

        String bigEndian = sh.toString();
        assertThat(bigEndian, is("fc3ea6882d1f6fdc360a2c650edf742dcb4ae078"));
        String littleEndian = Numeric.toHexStringNoPrefix(sh.toArray());
        assertThat(littleEndian, is("78e04acb2d74df0e652c0a36dc6f1f2d88a63efc"));
    }

    @Test
    public void toAddress() {
        final String key = "02c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238";
        // Address generated from the above key, with address version 0x17.
        final String expectedAddress = "AVGpjFiocR1BdYhbYWqB6Ls6kcmzx4FWhm";
        ScriptHash sh = ScriptHash.fromPublicKey(Numeric.hexStringToByteArray(key));
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
        ScriptHash sh1 = ScriptHash.fromScript(m1);
        ScriptHash sh2 = ScriptHash.fromScript(m2);
        ScriptHash sh3 = ScriptHash.fromScript(m3);

        assertThat(sh1.compareTo(sh1), is(0));
        assertThat(sh1.compareTo(sh2), is(-1));
        assertThat(sh1.compareTo(sh3), is(-1));
        assertThat(sh2.compareTo(sh1), is(1));
        assertThat(sh2.compareTo(sh3), is(1));
        assertThat(sh3.compareTo(sh1), is(1));
        assertThat(sh3.compareTo(sh2), is(-1));
    }

    @Test
    public void getSize() {
        ScriptHash sh = new ScriptHash("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(sh.getSize(), is(20));
    }

}