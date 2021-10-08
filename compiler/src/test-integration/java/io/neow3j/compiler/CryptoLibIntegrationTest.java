package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.constants.NamedCurve;
import io.neow3j.devpack.contracts.CryptoLib;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.test.TestProperties.cryptoLibHash;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.signature;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CryptoLibIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            CryptoLibIntegrationTestContract.class.getName());

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
                byteArray(signature), integer(NamedCurve.Secp256r1));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(cryptoLibHash())));
    }

    static class CryptoLibIntegrationTestContract {

        public static ByteString sha256(ByteString value) {
            return CryptoLib.sha256(value);
        }

        public static ByteString ripemd160(ByteString value) {
            return CryptoLib.ripemd160(value);
        }

        public static boolean verifyWithECDsa(ByteString message, ECPoint pubKey,
                ByteString signature, byte curve) {
            return CryptoLib.verifyWithECDsa(message, pubKey, signature, curve);
        }

        public static Hash160 getHash() {
            return CryptoLib.getHash();
        }
    }

}
