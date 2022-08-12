package io.neow3j.crypto;

import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.Test;

import java.security.SignatureException;

import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.crypto.Sign.recoverSigningScriptHash;
import static io.neow3j.crypto.Sign.signHexMessage;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.crypto.Sign.signedMessageToKey;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.toHexString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignTest {

    private static final String TEST_MESSAGE = "A test message";
    private static final byte[] TEST_MESSAGE_BYTES = TEST_MESSAGE.getBytes();
    static final ECPrivateKey PRIVATE_KEY = new ECPrivateKey(Numeric.toBigIntNoPrefix(
            "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3"));
    static final ECPublicKey PUBLIC_KEY = new ECPublicKey(Numeric.toBigIntNoPrefix(
            "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6"));
    static final ECKeyPair KEY_PAIR = new ECKeyPair(PRIVATE_KEY, PUBLIC_KEY);

    @Test
    public void testSignMessage() {
        Sign.SignatureData signatureData = signMessage(TEST_MESSAGE_BYTES, KEY_PAIR);

        Sign.SignatureData expected = new Sign.SignatureData(
                (byte) 27,
                hexStringToByteArray(
                        "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655"),
                hexStringToByteArray(
                        "523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf")
        );

        assertThat(signatureData, is(expected));

        signatureData = signMessage(sha256(TEST_MESSAGE_BYTES), KEY_PAIR, false);
        assertThat(signatureData, is(expected));

        signatureData = signMessage(TEST_MESSAGE, KEY_PAIR);
        assertThat(signatureData, is(expected));

        signatureData = signHexMessage(toHexString(TEST_MESSAGE_BYTES), KEY_PAIR);
        assertThat(signatureData, is(expected));
    }

    @Test
    public void testRecoverSigningScriptHash() throws SignatureException {
        Sign.SignatureData signatureData = new Sign.SignatureData(
                (byte) 27,
                hexStringToByteArray(
                        "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655"),
                hexStringToByteArray(
                        "523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf")
        );
        Hash160 signer = recoverSigningScriptHash(TEST_MESSAGE_BYTES, signatureData);

        assertThat(signer, is(KEY_PAIR.getScriptHash()));
    }

    @Test
    public void testFromByteArray() {
        byte[] r = hexStringToByteArray(
                "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655");
        byte[] s = hexStringToByteArray(
                "523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf");

        byte[] signature = hexStringToByteArray(
                "147e5f3c929dd830d961626551dbea6b70e4b2837ed2fe9089eed2072ab3a655523ae0fa8711eee4769f1913b180b9b3410bbb2cf770f529c85f6886f22cbaaf");
        Sign.SignatureData signatureData = Sign.SignatureData.fromByteArray(signature);
        assertThat(signatureData.getV(), is((byte) 0x00));
        assertThat(signatureData.getR(), is(r));
        assertThat(signatureData.getS(), is(s));

        signatureData = Sign.SignatureData.fromByteArray((byte) 0x27, signature);
        assertThat(signatureData.getV(), is((byte) 0x27));
        assertThat(signatureData.getR(), is(r));
        assertThat(signatureData.getS(), is(s));
    }

    @Test
    public void testSignedMessageToKey() throws SignatureException {
        Sign.SignatureData signatureData = signMessage(TEST_MESSAGE_BYTES, KEY_PAIR);
        ECPublicKey key = signedMessageToKey(TEST_MESSAGE_BYTES, signatureData);
        assertThat(key, equalTo(PUBLIC_KEY));
    }

    @Test
    public void testPublicKeyFromPrivateKey() {
        assertThat(Sign.publicKeyFromPrivate(PRIVATE_KEY), equalTo(PUBLIC_KEY));
    }

    @Test
    public void testInvalidSignature() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> signedMessageToKey(TEST_MESSAGE_BYTES,
                new Sign.SignatureData((byte) 27, new byte[]{1}, new byte[]{0})));
        assertThat(thrown.getMessage(), is("r must be 32 bytes."));
    }

    @Test
    public void verifySignature() {
        Sign.SignatureData signatureData = signMessage(TEST_MESSAGE_BYTES, KEY_PAIR);
        assertTrue(Sign.verifySignature(TEST_MESSAGE_BYTES, signatureData, PUBLIC_KEY));
    }

}
