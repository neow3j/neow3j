package io.neow3j.crypto;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountPrivateKey;
import static io.neow3j.test.TestProperties.defaultAccountScriptHash;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ECKeyPairTest {

    @BeforeAll
    public static void setUp() {
        addBouncyCastle();
    }

    @Test
    public void setupNewECPublicKeyAndGetEncodedAndGetECPoint() {
        BigInteger expectedX = new BigInteger(
                "b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816", 16);
        BigInteger expectedY = new BigInteger(
                "5f4f7fb1c5862465543c06dd5a2aa414f6583f92a5cc3e1d4259df79bf6839c9", 16);
        ECPoint expectedECPoint = NeoConstants.secp256r1CurveParams().getCurve()
                .createPoint(expectedX, expectedY);
        String encECPoint = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(hexStringToByteArray(encECPoint));
        assertThat(pubKey.getECPoint(), is(expectedECPoint));
        assertArrayEquals(pubKey.getEncoded(true), hexStringToByteArray(encECPoint));
        assertThat(pubKey.getEncodedCompressedHex(), is(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
    }

    @Test
    public void createECPublicKeyFromUncompressedECPoint() {
        String ecPoint =
                "04b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368165f4f7fb1c5862465543c06dd5a2aa414f6583f92a5cc3e1d4259df79bf6839c9";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(hexStringToByteArray(ecPoint));
        assertThat(pubKey.getEncodedCompressedHex(),
                is("03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
    }

    @Test
    public void invalidSize() {
        String pubKeyHex = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368"; // Only 32 bytes
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new ECKeyPair.ECPublicKey(hexStringToByteArray(pubKeyHex)));
        assertThat(thrown.getMessage(), is("Incorrect length for compressed encoding"));
    }

    @Test
    public void cleanHexPrefix() {
        String pubKeyHex = "0x03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        String expected = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(hexStringToByteArray(pubKeyHex));
        assertThat(pubKey.getEncodedCompressedHex(), is(expected));
    }

    @Test
    public void serializeECPublicKey() {
        String encECPoint = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(
                hexStringToByteArray(encECPoint));

        assertArrayEquals(pubKey.toArray(), hexStringToByteArray(encECPoint));
    }

    @Test
    public void deserializeECPublicKeyFromSecP256R1GeneratorPoint() throws
            DeserializationException {

        byte[] data = hexStringToByteArray(
                "036b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296");
        ECKeyPair.ECPublicKey pubKey = NeoSerializableInterface.from(
                data, ECKeyPair.ECPublicKey.class);

        ECPoint g = NeoConstants.secp256r1CurveParams().getG().normalize();
        assertThat(pubKey.getECPoint(), is(g));
    }

    @Test
    public void getSize() {
        byte[] data = hexStringToByteArray(
                "036b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296");
        ECKeyPair.ECPublicKey key = new ECKeyPair.ECPublicKey(data);
        assertThat(key.getSize(), is(33));
    }

    @Test
    public void exportAsWIF() {
        ECKeyPair keyPair = ECKeyPair.create(
                hexStringToByteArray("c7134d6fd8e73d819e82755c64c93788d8db0961929e025a53363c4cc02a6962"));
        assertThat(keyPair.exportAsWIF(), is("L3tgppXLgdaeqSGSFw1Go3skBiy8vQAM7YMXvTHsKQtE16PBncSU"));
    }

    @Test
    public void getAddress() {
        ECKeyPair pair = ECKeyPair.create(hexStringToByteArray(defaultAccountPrivateKey()));
        assertThat(pair.getAddress(), is(defaultAccountAddress()));
    }

    @Test
    public void getScriptHash() {
        ECKeyPair pair = ECKeyPair.create(hexStringToByteArray(defaultAccountPrivateKey()));
        assertThat(pair.getScriptHash(), is(new Hash160(defaultAccountScriptHash())));
    }

    @Test
    public void privateKeyShouldBeZeroAfterErasing() {
        ECPrivateKey privKey = new ECPrivateKey(hexStringToByteArray(
                "a7038726c5a127989d78593c423e3dad93b2d74db90a16c0a58468c9e6617a87"));
        privKey.erase();
        assertThat(privKey.getBytes(), is(new byte[NeoConstants.PRIVATE_KEY_SIZE]));
    }

    @Test
    public void compareTo() {
        String encodedKey1 = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        String encodedKey2 = "036b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296";
        String encodedKey1Uncompressed =
                "04b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368165f4f7fb1c5862465543c06dd5a2aa414f6583f92a5cc3e1d4259df79bf6839c9";
        ECKeyPair.ECPublicKey key1 = new ECKeyPair.ECPublicKey(encodedKey1);
        ECKeyPair.ECPublicKey key2 = new ECKeyPair.ECPublicKey(encodedKey2);
        ECKeyPair.ECPublicKey key1FromUncompressed = new ECKeyPair.ECPublicKey(encodedKey1Uncompressed);
        assertThat(key1.compareTo(key2), is(1)); // x coord of key1 is larger than key2
        assertThat(key1.compareTo(key1FromUncompressed), is(0));
    }

}
