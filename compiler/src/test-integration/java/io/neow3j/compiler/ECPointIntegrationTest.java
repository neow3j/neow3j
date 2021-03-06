package io.neow3j.compiler;

import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.types.ContractParameter.byteArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ECPointIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            ECPointIntegrationTestContract.class.getName());

    @Test
    public void createEcPointFromValidByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(publicKey));
    }

    @Test
    public void createEcPointFromInvalidByteArray() throws IOException {
        // Public key byte array is one byte too short
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void createEcPointFromValidString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(publicKey));
    }

    @Test
    public void createEcPointFromInvalidString() throws IOException {
        // Public key byte array is one byte too short
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void ecPointToByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.publicKey(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(Numeric.hexStringToByteArray(publicKey)));
    }

    @Test
    public void ecPointAsByteString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.publicKey(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(publicKey));
    }

    @Test
    public void ecPointToByteArrayFromStringLiteral() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(Numeric.hexStringToByteArray(publicKey)));
    }

    static class ECPointIntegrationTestContract {

        public static ECPoint createEcPointFromValidByteArray(byte[] b) {
            return new ECPoint(b);
        }

        public static ECPoint createEcPointFromInvalidByteArray(byte[] b) {
            return new ECPoint(b);
        }

        public static ECPoint createEcPointFromValidString(ByteString s) {
            return new ECPoint(s);
        }

        public static ECPoint createEcPointFromInvalidString(ByteString s) {
            return new ECPoint(s);
        }

        public static byte[] ecPointToByteArrayFromStringLiteral() {
            ECPoint point = new ECPoint(StringLiteralHelper.hexToBytes(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
            return point.toByteArray();
        }

        public static byte[] ecPointToByteArray(ECPoint ecPoint) {
            return ecPoint.toByteArray();
        }

        public static ByteString ecPointAsByteString(ECPoint ecPoint) {
            return ecPoint.toByteString();
        }
    }

}
