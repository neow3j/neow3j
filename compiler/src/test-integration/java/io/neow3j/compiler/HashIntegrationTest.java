package io.neow3j.compiler;

import io.neow3j.contract.Hash160;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.model.types.NeoVMStateType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HashIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            HashIntegrationTestContract.class.getName());

    @Test
    public void getZeroHash160() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(zeroHash));
    }

    @Test
    public void isHash160Zero() throws IOException {
        Hash160 zeroHash = new Hash160("0000000000000000000000000000000000000000");
        Hash160 nonZeroHash = new Hash160("0000000000000000000000000000000000000001");
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(zeroHash),
                hash160(nonZeroHash));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    @Test
    public void isHash160Valid() throws IOException {
        String validHash = "0000000000000000000000000000000000000001";
        String invalidHash = "00000000000000000000000000000000000001"; // One byte short.
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(validHash),
                byteArray(invalidHash));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    @Test
    public void createHash160FromValidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(hash));
    }

    @Test
    public void createHash160FromInvalidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b7b"; // One byte to long.
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void createHash160FromValidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(hash));
    }

    @Test
    public void createHash160FromInvalidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b7b"; // One byte to long.
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string(hash));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void hash160ToByteArray() throws IOException {
        Hash160 hash = new Hash160("03b4af8d061b6b320cce6c63bc4ec7894dce107b");
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getAddress(),
                is(hash.toAddress()));
    }

    @Test
    public void hash160ToString() throws IOException {
        Hash160 hash = new Hash160("03b4af8d061b6b320cce6c63bc4ec7894dce107b");
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash160(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(hash.toString())));
    }

    @Test
    public void getZeroHash256() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(zeroHash));
    }

    @Test
    public void isHash256Zero() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String nonZeroHash = "0000000000000000000000000000000000000000000000000000000000000001";
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, hash256(zeroHash), hash256(nonZeroHash));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    @Test
    public void isHash256Valid() throws IOException {
        String validHash = "0000000000000000000000000000000000000000000000000000000000000001";
        // One byte to short.
        String invalidHash = "00000000000000000000000000000000000000000000000000000000000001";
        NeoInvokeFunction response =
                ct.callInvokeFunction(testName, hash256(validHash), byteArray(invalidHash));
        List<StackItem> array = response.getInvocationResult().getStack().get(0).getList();
        assertTrue(array.get(0).getBoolean());
        assertFalse(array.get(1).getBoolean());
    }

    @Test
    public void createHash256FromValidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(hash));
    }

    @Test
    public void createHash256FromInvalidByteArray() throws IOException {
        // One byte to long.
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b00000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void createHash256FromValidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(hash));
    }

    @Test
    public void createHash256FromInvalidString() throws IOException {
        // One byte to long.
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b00000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray(hash));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void hash256ToByteArray() throws IOException {
        String hash256 = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(hash256));
        assertThat(response.getInvocationResult().getStack().get(0).getByteArray(),
                is(Numeric.hexStringToByteArray(Numeric.reverseHexString(hash256))));
    }

    @Test
    public void hash256ToString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(hash));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(hash)));
    }

    @Test
    public void hash160FromStringLiteral() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is("03b4af8d061b6b320cce6c63bc4ec7894dce107b"));
    }

    @Test
    public void hash256FromStringLiteral() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is("03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000"));
    }


    static class HashIntegrationTestContract {

        public static io.neow3j.devpack.Hash160 getZeroHash160() {
            return io.neow3j.devpack.Hash160.zero();
        }

        public static boolean[] isHash160Zero(io.neow3j.devpack.Hash160 hash1,
                io.neow3j.devpack.Hash160 hash2) {
            boolean[] b = new boolean[2];
            b[0] = hash1.isZero();
            b[1] = hash2.isZero();
            return b;
        }

        public static boolean[] isHash160Valid(io.neow3j.devpack.Hash160 h1,
                io.neow3j.devpack.Hash160 h2) {
            boolean[] b = new boolean[2];
            b[0] = h1.isValid();
            b[1] = h2.isValid();
            return b;
        }

        public static io.neow3j.devpack.Hash160 createHash160FromValidByteArray(byte[] b) {
            return new io.neow3j.devpack.Hash160(b);
        }

        public static io.neow3j.devpack.Hash160 createHash160FromInvalidByteArray(byte[] b) {
            return new io.neow3j.devpack.Hash160(b);
        }

        public static io.neow3j.devpack.Hash160 createHash160FromValidString(ByteString s) {
            return new io.neow3j.devpack.Hash160(s);
        }

        public static io.neow3j.devpack.Hash160 createHash160FromInvalidString(ByteString s) {
            return new io.neow3j.devpack.Hash160(s);
        }

        public static byte[] hash160ToByteArray(io.neow3j.devpack.Hash160 hash160) {
            return hash160.toByteArray();
        }

        public static ByteString hash160ToString(io.neow3j.devpack.Hash160 hash160) {
            return hash160.asByteString();
        }

        public static Hash256 getZeroHash256() {
            return Hash256.zero();
        }

        public static boolean[] isHash256Zero(Hash256 hash1, Hash256 hash2) {
            boolean[] b = new boolean[2];
            b[0] = hash1.isZero();
            b[1] = hash2.isZero();
            return b;
        }

        public static boolean[] isHash256Valid(Hash256 hash256_1, Hash256 hash256_2) {
            boolean[] b = new boolean[2];
            b[0] = hash256_1.isValid();
            b[1] = hash256_2.isValid();
            return b;
        }

        public static Hash256 createHash256FromValidByteArray(byte[] b) {
            return new Hash256(b);
        }

        public static Hash256 createHash256FromInvalidByteArray(byte[] b) {
            return new Hash256(b);
        }

        public static Hash256 createHash256FromValidString(ByteString s) {
            return new Hash256(s);
        }

        public static Hash256 createHash256FromInvalidString(ByteString s) {
            return new Hash256(s);
        }

        public static byte[] hash256ToByteArray(Hash256 hash256) {
            return hash256.toByteArray();
        }

        public static ByteString hash256ToString(Hash256 hash256) {
            return hash256.asByteString();
        }

        public static io.neow3j.devpack.Hash160 hash160FromStringLiteral() {
            return new io.neow3j.devpack.Hash160(StringLiteralHelper.hexToBytes(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b"));
        }

        public static Hash256 hash256FromStringLiteral() {
            return new Hash256(StringLiteralHelper.hexToBytes(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000"));
        }
    }

}
