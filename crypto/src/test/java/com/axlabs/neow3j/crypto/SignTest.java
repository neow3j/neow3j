package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.utils.Numeric;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SignatureException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SignTest {

    private static final byte[] TEST_MESSAGE = "A test message".getBytes();

    @Test
    public void testSignMessage() {
        Sign.SignatureData signatureData = Sign.signMessage(TEST_MESSAGE, SampleKeys.KEY_PAIR_1);

        Sign.SignatureData expected = new Sign.SignatureData(
                (byte) 27,
                Numeric.hexStringToByteArray(
                        "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655"),
                Numeric.hexStringToByteArray(
                        "523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf")
        );

        assertThat(signatureData, is(expected));
    }

    @Test
    public void testSignedMessageToKey() throws SignatureException {
        Sign.SignatureData signatureData = Sign.signMessage(TEST_MESSAGE, SampleKeys.KEY_PAIR_1);
        System.out.println(Numeric.toHexStringNoPrefix(signatureData.getConcatenated()));
        BigInteger key = Sign.signedMessageToKey(TEST_MESSAGE, signatureData);
        assertThat(key, equalTo(SampleKeys.PUBLIC_KEY_1));
    }

    @Test
    public void testPublicKeyFromPrivateKey() {
        assertThat(Sign.publicKeyFromPrivate(SampleKeys.PRIVATE_KEY_1),
                equalTo(SampleKeys.PUBLIC_KEY_1));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidSignature() throws SignatureException {
        Sign.signedMessageToKey(
                TEST_MESSAGE, new Sign.SignatureData((byte) 27, new byte[]{1}, new byte[]{0}));
    }
}
