package io.neow3j.crypto;

import io.neow3j.constants.NeoConstants;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Numeric;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import static io.neow3j.crypto.SecurityProviderChecker.addBouncyCastle;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

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
        byte[] data = {3, 107, 23, (byte) 209, (byte) 242, (byte) 225, 44, 66, 71, (byte) 248,
                (byte) 188, (byte) 230, (byte) 229, 99, (byte) 164, 64, (byte) 242, 119, 3, 125,
                (byte) 129, 45, (byte) 235, 51, (byte) 160, (byte) 244, (byte) 161, 57, 69, (byte) 216,
                (byte) 152, (byte) 194, (byte) 150};

        ECKeyPair.ECPublicKey pubKey = NeoSerializableInterface.from(
                data, ECKeyPair.ECPublicKey.class);

        ECPoint g = NeoConstants.CURVE_PARAMS.getG().normalize();
        java.security.spec.ECPoint g_ = new java.security.spec.ECPoint(
                g.getAffineXCoord().toBigInteger(),
                g.getAffineYCoord().toBigInteger());
        assertThat(pubKey.getECPoint(), is(g_));
    }

}