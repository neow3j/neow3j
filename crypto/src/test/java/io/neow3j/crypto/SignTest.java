package io.neow3j.crypto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.utils.Numeric;
import java.security.SignatureException;
import org.junit.Test;

public class SignTest {

    private static final byte[] TEST_MESSAGE = "A test message".getBytes();
    static final ECPrivateKey PRIVATE_KEY = new ECPrivateKey(Numeric.toBigIntNoPrefix(
            "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3"));
    static final ECPublicKey PUBLIC_KEY = new ECPublicKey(Numeric.toBigIntNoPrefix(
            "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6"));
    static final ECKeyPair KEY_PAIR = new ECKeyPair(PRIVATE_KEY, PUBLIC_KEY);

    @Test
    public void testSignMessage() {
        Sign.SignatureData signatureData = Sign.signMessage(TEST_MESSAGE, KEY_PAIR);

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
        Sign.SignatureData signatureData = Sign.signMessage(TEST_MESSAGE, KEY_PAIR);
        System.out.println(Numeric.toHexStringNoPrefix(signatureData.getConcatenated()));
        ECPublicKey key = Sign.signedMessageToKey(TEST_MESSAGE, signatureData);
        assertThat(key, equalTo(PUBLIC_KEY));
    }

    @Test
    public void testPublicKeyFromPrivateKey() {
        assertThat(Sign.publicKeyFromPrivate(PRIVATE_KEY), equalTo(PUBLIC_KEY));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidSignature() throws SignatureException {
        Sign.signedMessageToKey(
                TEST_MESSAGE, new Sign.SignatureData((byte) 27, new byte[]{1}, new byte[]{0}));
    }
}
