package io.neow3j.crypto;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Numeric;
import java.math.BigInteger;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.BeforeClass;
import org.junit.Test;

public class ECKeyPairTest {

    @BeforeClass
    public static void setUp() {
        addBouncyCastle();
    }

    @Test
    public void setupNewECPublicKeyAndGetEncodedAndGetECPoint() {
        BigInteger expectedX = new BigInteger(
                "b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816", 16);
        BigInteger expectedY = new BigInteger(
                "5f4f7fb1c5862465543c06dd5a2aa414f6583f92a5cc3e1d4259df79bf6839c9", 16);
        java.security.spec.ECPoint expectedECPoint =
                new java.security.spec.ECPoint(expectedX, expectedY);

        String encECPoint = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(
                Numeric.hexStringToByteArray(encECPoint));

        assertThat(pubKey.getECPoint(), is(expectedECPoint));
        assertArrayEquals(pubKey.getEncoded(true), Numeric.hexStringToByteArray(encECPoint));
    }

    @Test
    public void serializeECPublicKey() {
        String encECPoint = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ECKeyPair.ECPublicKey pubKey = new ECKeyPair.ECPublicKey(
                Numeric.hexStringToByteArray(encECPoint));

        assertArrayEquals(pubKey.toArray(), Numeric.hexStringToByteArray(encECPoint));
    }

    @Test
    public void deserializeECPublicKeyFromSecP256R1GeneratorPoint() throws
            DeserializationException {

        byte[] data = Numeric.hexStringToByteArray(
                "036b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296");
        ECKeyPair.ECPublicKey pubKey = NeoSerializableInterface.from(
                data, ECKeyPair.ECPublicKey.class);

        ECPoint g = NeoConstants.curveParams().getG().normalize();
        java.security.spec.ECPoint g_ = new java.security.spec.ECPoint(
                g.getAffineXCoord().toBigInteger(),
                g.getAffineYCoord().toBigInteger());
        assertThat(pubKey.getECPoint(), is(g_));
    }

    @Test
    public void getSize() {
        byte[] data = Numeric.hexStringToByteArray(
                "036b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296");
        ECKeyPair.ECPublicKey key = new ECKeyPair.ECPublicKey(data);
        assertThat(key.getSize(), is(33));
    }

    @Test
    public void getAddress() {
        final String privKey = "a7038726c5a127989d78593c423e3dad93b2d74db90a16c0a58468c9e6617a87";
        final String expectedAddress = "NVqPvouR2QjzBBakmDRxi3gGHYbtnhRvfn";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(privKey));
        assertThat(pair.getAddress(), is(expectedAddress));
    }

    @Test
    public void privateKeyShouldBeZeroAfterErasing() {
        ECPrivateKey privKey = new ECPrivateKey(Numeric.hexStringToByteArray(
                        "a7038726c5a127989d78593c423e3dad93b2d74db90a16c0a58468c9e6617a87"));
        privKey.erase();
        assertThat(privKey.getBytes(), is(new byte[NeoConstants.PRIVATE_KEY_SIZE]));
    }

}