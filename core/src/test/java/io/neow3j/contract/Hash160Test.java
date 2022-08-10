package io.neow3j.contract;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.script.InteropService;
import io.neow3j.script.OpCode;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.neow3j.crypto.Hash.sha256AndThenRipemd160;
import static io.neow3j.test.TestProperties.committeeAccountScriptHash;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Hash160Test {

    @Test
    public void createFromValidHash() {
        Hash160 hash = new Hash160("0x23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));

        hash = new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test
    public void createFromHashWithOddLength() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Hash160("0x23ba2703c53263e8d6e522dc32203339dcd8eee"));
        assertThat(thrown.getMessage(), is("String argument is not hexadecimal."));
    }

    @Test
    public void createFromMalformedHash() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Hash160("g3ba2703c53263e8d6e522dc32203339dcd8eee9"));
        assertThat(thrown.getMessage(), is("String argument is not hexadecimal."));
    }

    @Test
    public void createFromTooShortHash() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Hash160("23ba2703c53263e8d6e522dc32203339dcd8ee"));
        assertThat(thrown.getMessage(), is("Hash must be 20 bytes long but was 19 bytes."));
    }

    @Test
    public void createFromTooLongHash() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Hash160("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b"));
        assertThat(thrown.getMessage(), is("Hash must be 20 bytes long but was 32 bytes."));
    }

    @Test
    public void toArray() {
        Hash160 hash = new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9");
        byte[] expected = reverseArray(hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));

        assertArrayEquals(expected, hash.toLittleEndianArray());
    }

    @Test
    public void serialize() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Hash160("23ba2703c53263e8d6e522dc32203339dcd8eee9").serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = reverseArray(hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));

        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = reverseArray(hexStringToByteArray(
                "23ba2703c53263e8d6e522dc32203339dcd8eee9"));
        Hash160 hash = NeoSerializableInterface.from(data, Hash160.class);

        assertThat(hash.toString(), is("23ba2703c53263e8d6e522dc32203339dcd8eee9"));
    }

    @Test
    public void equals() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = hexStringToByteArray("d802a401");
        Hash160 hash1 = Hash160.fromScript(m1);
        Hash160 hash2 = Hash160.fromScript(m2);

        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash1);
        assertEquals(hash1, hash1);
    }

    @Test
    public void fromValidAddress() {
        Hash160 hash = Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        byte[] expectedHash = hexStringToByteArray("09a55874c2da4b86e5d49ff530a1b153eb12c7d6");

        assertThat(hash.toLittleEndianArray(), is(expectedHash));
    }

    @Test
    public void fromInvalidAddress() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> Hash160.fromAddress("NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8keas"));
        assertThat(thrown.getMessage(), is("Not a valid NEO address."));
    }

    @Test
    public void fromPublicKeyByteArray() {
        final String key = "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
        byte[] script = hexStringToByteArray(""
                + OpCode.PUSHDATA1.toString() + "21"  // PUSHDATA 33 bytes
                + key // public key
                + OpCode.SYSCALL.toString()
                + InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash()
        );
        Hash160 hash = Hash160.fromPublicKey(hexStringToByteArray(key));

        assertThat(hash.toLittleEndianArray(), is(sha256AndThenRipemd160(script)));
    }

    @Test
    public void fromPublicKeyByteArrays() {
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(defaultAccountPublicKey());
        Hash160 hash = Hash160.fromPublicKeys(asList(pubKey), 1);

        assertThat(hash.toString(), is(committeeAccountScriptHash()));
    }

    @Test
    public void fromContractScript() {
        String verificationScript =
                "110c21026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d110b41138defaf";
        Hash160 hash = Hash160.fromScript(verificationScript);

        String bigEndian = hash.toString();
        assertThat(bigEndian, is("afaed076854454449770763a628f379721ea9808"));
        String littleEndian = toHexStringNoPrefix(hash.toLittleEndianArray());
        assertThat(littleEndian, is("0898ea2197378f623a7670974454448576d0aeaf"));
    }

    @Test
    public void toAddress() {
        final String key = "031ccaaa46df7c494f442698c8c17c09311e3615c2dc042cbd3afeaba60fa40740";
        // Address generated from the above key, with address version 0x35.
        Hash160 sh = Hash160.fromPublicKey(hexStringToByteArray(defaultAccountPublicKey()));

        assertThat(sh.toAddress(), is(defaultAccountAddress()));
    }

    @Test
    public void compareTo() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as integer)
        byte[] m1 = hexStringToByteArray("01a402d8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as integer)
        byte[] m2 = hexStringToByteArray("d802a401");
        // first message has script hash 226912894221247444770625744046962264064050576762 (as integer)
        byte[] m3 = hexStringToByteArray("a7b3a191");
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
