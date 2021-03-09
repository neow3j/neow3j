package io.neow3j.compiler;

import io.neow3j.devpack.Crypto;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.publicKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CryptoIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(CryptoIntegrationTestContract.class.getName());
    }

    @Test
    public void checkSig() throws IOException {
        String pubKey = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String signature =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        NeoInvokeFunction res = callInvokeFunction(publicKey(pubKey), byteArray(signature));
        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void checkMultisig() throws IOException {
        String pubKey1 = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String pubKey2 = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        String signature1 =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        String signature2 =
                "a30ded6e19be5573a6f6a5ff37c35d4ae76ff35ab4bee03b5b5bfbbef371f812ff70b5b480462807948a2ffb24dd8771484d9ca5a90333f9e6db69a6c8802a63";
        NeoInvokeFunction res = callInvokeFunction(
                array(publicKey(pubKey1), publicKey(pubKey2)),
                array(byteArray(signature1), byteArray(signature2)));
        assertTrue(res.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void hash256() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(byteArray("0102030405"));
        assertThat(res.getInvocationResult().getStack().get(0),
                is("a26baf5a9a07d9eb7ba10f43924dcdf3f75f0abf066cd9f0c76f983121302e01"));
    }

    @Test
    public void hash160() throws IOException {
        NeoInvokeFunction res = callInvokeFunction(byteArray("0102030405"));
        assertThat(res.getInvocationResult().getStack().get(0),
                is("1fcc83c91e862661592480531afa87c3e2f59332"));
    }

    static class CryptoIntegrationTestContract {

        public static void checkSig(ECPoint pubKey, String signature) {
            Crypto.checkSig(pubKey, signature);
        }

        public static void checkMultiSig(ECPoint[] pubKeys, String[] signatures) {
            Crypto.checkMultisig(pubKeys, signatures);
        }

        public static Hash256 hash256(String value) {
            return Crypto.hash256(value);
        }

        public static Hash160 hash160(String value) {
            return Crypto.hash160(value);
        }
    }

}
