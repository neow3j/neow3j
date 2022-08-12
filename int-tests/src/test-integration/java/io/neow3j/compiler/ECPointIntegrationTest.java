package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Runtime;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ECPointIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(ECPointIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void createECPointFromByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(publicKey));
    }

    @Test
    public void createECPointFromString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(publicKey));
    }

    @Test
    public void isObjectValidECPoint() throws IOException {
        String validPubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        // One byte too short.
        String invalidPubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        int otherValue = 123456;

        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(validPubKey), byteArray(invalidPubKey),
                integer(otherValue));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
        assertFalse(array.get(2).getBoolean());
        assertTrue(array.get(3).getBoolean());
    }

    @Test
    public void ecPointToByteArray() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, publicKey(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(hexStringToByteArray(publicKey)));
    }

    @Test
    public void ecPointAsByteString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, publicKey(publicKey));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(publicKey));
    }

    @Test
    public void ecPointToByteArrayFromStringLiteral() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(hexStringToByteArray(publicKey)));
    }

    @Test
    public void ecPointFromString() throws IOException {
        String publicKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(publicKey));
    }

    @Test
    public void witnessCheckWithECPointFromString() throws IOException {
        ct.signWithDefaultAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    static class ECPointIntegrationTestContract {

        public static ECPoint createECPointFromByteArray(ByteString b) {
            byte[] buffer = b.toByteArray();
            assert buffer instanceof byte[];
            return new ECPoint(buffer);
        }

        public static ECPoint createECPointFromString(ByteString s) {
            return new ECPoint(s);
        }

        public static boolean[] isObjectValidECPoint(Object validECPoint, Object invalidECPoint, Object integer) {
            boolean[] b = new boolean[4];
            b[0] = ECPoint.isValid(validECPoint);
            b[1] = ECPoint.isValid(invalidECPoint);
            b[2] = ECPoint.isValid(integer);
            byte[] buffer = ((ByteString) validECPoint).toByteArray();
            b[3] = ECPoint.isValid(buffer);
            return b;
        }

        public static byte[] ecPointToByteArrayFromStringLiteral() {
            ECPoint point = new ECPoint(
                    hexToBytes("03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816"));
            byte[] pointBuffer = point.toByteArray();
            assert pointBuffer instanceof byte[];
            return pointBuffer;
        }

        public static byte[] ecPointToByteArray(ECPoint ecPoint) {
            byte[] pointBuffer = ecPoint.toByteArray();
            assert pointBuffer instanceof byte[];
            return pointBuffer;
        }

        public static ByteString ecPointAsByteString(ECPoint ecPoint) {
            return ecPoint.toByteString();
        }

        public static ECPoint ecPointFromString() {
            return new ECPoint("0x03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816");
        }

        public static boolean witnessCheckWithECPointFromString() {
            return Runtime.checkWitness(
                    new ECPoint("033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b"));
        }

    }

}
