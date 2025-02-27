package io.neow3j.compiler;

import io.neow3j.crypto.Sign;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.constants.NamedCurve;
import io.neow3j.devpack.contracts.CryptoLib;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.test.TestProperties.cryptoLibHash;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.signature;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptoLibIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            CryptoLibIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void recoverSecp256K1() throws IOException {
        String messageHashHex = "5ae8317d34d1e595e3fa7247db80c0af4320cce1116de187f8f7e2e099c0d8d0";
        String sigHex = "45c0b7f8c09a9e1f1cea0c25785594427b6bf8f9f878a8af0b1abbb48e16d0920d8becd0c220f67c51217eecfd7184ef0732481c843857e6bc7fc095c4f6b78801";
        String expectedRecoveredPubKeyHex = "034a071e8a6e10aada2b8cf39fa3b5fb3400b04e99ea8ae64ceea1a977dbeaf5d5";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(messageHashHex), byteArray(sigHex));
        assertThat(response.getInvocationResult().getFirstStackItem().getHexString(), is(expectedRecoveredPubKeyHex));
    }

    @Test
    public void sha256() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("0102030405"));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is("74f81fe167d99b4cb41d6d0ccda82278caee9f3e2f25d5e5a3936ff3dcec60d0"));
    }

    @Test
    public void ripemd160() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("0102030405"));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is("eb825c4b24f425077a067cc3bef457783f5ad705"));
    }

    @Test
    public void murmur32() throws IOException {
        // Verified by https://github.com/shorelabs/murmurhash-online (MurmurHash3)
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("6e656f77336a"), integer(425653234));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(reverseHexString("fcee1b3a")));
    }

    @Test
    public void keccak256() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("68656c6c6f20776f726c64"));
        assertThat(response.getInvocationResult().getFirstStackItem().getHexString(),
                is("47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad"));
    }

    @Test
    public void verifyWithECDsa() throws IOException {
        String message = "0102030405";
        String pubKey = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String signature =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(message),
                publicKey(pubKey), signature(signature), integer(NamedCurve.Secp256r1));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());

        message = "0102030406"; // small change in message.
        response = ct.callInvokeFunction(testName, byteArray(message), publicKey(pubKey),
                signature(signature), integer(NamedCurve.Secp256r1));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void verifyWithECDsaWithSignatureData() throws IOException {
        String message = "010203040506";
        Account account = Account.fromWIF("L2Zb82MN9mh5gZ759q3CwmDCqHTsUD2SGC5GsEw2L451PE4L2Gjh");
        Sign.SignatureData signatureData = signMessage(hexStringToByteArray(message),
                account.getECKeyPair());
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(message),
                publicKey(account.getECKeyPair().getPublicKey().getEncoded(true)),
                signature(signatureData), integer(NamedCurve.Secp256r1));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(reverseHexString(cryptoLibHash())));
    }

    static class CryptoLibIntegrationTestContract {

        public static ECPoint recoverSecp256K1(ByteString messageHash, ByteString signature) {
            return new CryptoLib().recoverSecp256K1(messageHash, signature);
        }

        public static ByteString sha256(ByteString value) {
            return new CryptoLib().sha256(value);
        }

        public static ByteString ripemd160(ByteString value) {
            return new CryptoLib().ripemd160(value);
        }

        public static ByteString murmur32(ByteString value, int seed) {
            return new CryptoLib().murmur32(value, seed);
        }

        public static ByteString keccak256(ByteString value) {
            return new CryptoLib().keccak256(value);
        }

        public static boolean verifyWithECDsa(ByteString message, ECPoint pubKey,
                ByteString signature, byte curve) {
            return new CryptoLib().verifyWithECDsa(message, pubKey, signature, curve);
        }

        public static boolean verifyWithECDsaWithSignatureData(ByteString message, ECPoint pubKey,
                ByteString signature, byte curve) {
            return new CryptoLib().verifyWithECDsa(message, pubKey, signature, curve);
        }

        public static Hash160 getHash() {
            return new CryptoLib().getHash();
        }

    }

}
