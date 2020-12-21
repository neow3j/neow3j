package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.ByteArrayTest.ByteArrays;
import io.neow3j.devpack.ECPoint;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ECPointIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ECPointIntegrationTestContract.class.getName());
    }

    @Test
    public void createEcPointFromValidByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = callInvokeFunction(byteArray(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(publicKey));
    }

    @Test
    public void createEcPointFromInvalidByteArray() throws IOException {
        // Public key byte array is one byte to short
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        NeoInvokeFunction response = callInvokeFunction(byteArray(publicKey));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void createEcPointFromValidString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = callInvokeFunction(string(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(publicKey));
    }

    @Test
    public void createEcPointFromInvalidString() throws IOException {
        // Public key byte array is one byte to short
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        NeoInvokeFunction response = callInvokeFunction(string(publicKey));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void ecPointToByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(publicKey));
    }

    @Test
    public void ecPointToString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(publicKey));
    }

    static class ECPointIntegrationTestContract {

        public static ECPoint createEcPointFromValidByteArray(byte[] b) {
            return new ECPoint(b);
        }

        public static ECPoint createEcPointFromInvalidByteArray(byte[] b) {
            return new ECPoint(b);
        }

        public static ECPoint createEcPointFromValidString(String s) {
            return new ECPoint(s);
        }

        public static ECPoint createEcPointFromInvalidString(String s) {
            return new ECPoint(s);
        }

        public static byte[] ecPointToByteArray() {
            ECPoint point = new ECPoint(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");
            return point.toByteArray();
        }

        public static String ecPointToString() {
            ECPoint point = new ECPoint(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");
            return point.toString();
        }
    }

}
