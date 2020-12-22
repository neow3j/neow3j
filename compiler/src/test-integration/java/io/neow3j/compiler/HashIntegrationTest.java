package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashIntegrationTest extends ContractTest {


    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(HashIntegrationTestContract.class.getName());
    }

    @Test
    public void getZeroHash160() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(zeroHash));
    }

    @Test
    public void isHash160Zero() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000";
        String nonZeroHash = "0000000000000000000000000000000000000001";
        NeoInvokeFunction response = callInvokeFunction(byteArray(zeroHash),
                byteArray(nonZeroHash));
        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(0));
    }

    @Test
    public void isHash160Valid() throws IOException {
        String validHash = "0000000000000000000000000000000000000001";
        String invalidHash = "00000000000000000000000000000000000001"; // One byte to short.
        NeoInvokeFunction response = callInvokeFunction(byteArray(validHash),
                byteArray(invalidHash));
        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(0));
    }

    @Test
    public void createHash160FromValidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = callInvokeFunction(byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void createHash160FromInvalidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b7b"; // One byte to long.
        NeoInvokeFunction response = callInvokeFunction(byteArray(hash));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void createHash160FromValidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = callInvokeFunction(string(hash));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void createHash160FromInvalidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b7b"; // One byte to long.
        NeoInvokeFunction response = callInvokeFunction(string(hash));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void hash160ToByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void hash160ToString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void getZeroHash256() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(zeroHash));
    }

    @Test
    public void isHash256Zero() throws IOException {
        String zeroHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String nonZeroHash = "0000000000000000000000000000000000000000000000000000000000000001";
        NeoInvokeFunction response = callInvokeFunction(byteArray(zeroHash),
                byteArray(nonZeroHash));
        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(0));
    }

    @Test
    public void isHash256Valid() throws IOException {
        String validHash = "0000000000000000000000000000000000000000000000000000000000000001";
        // One byte to short.
        String invalidHash = "00000000000000000000000000000000000000000000000000000000000001";
        NeoInvokeFunction response = callInvokeFunction(byteArray(validHash),
                byteArray(invalidHash));
        ArrayStackItem array = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(array.get(0).asInteger().getValue().intValue(), is(1));
        assertThat(array.get(1).asInteger().getValue().intValue(), is(0));
    }

    @Test
    public void createHash256FromValidByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction(byteArray(hash));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void createHash256FromInvalidByteArray() throws IOException {
        // One byte to long.
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b00000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction(byteArray(hash));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void createHash256FromValidString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction(string(hash));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void createHash256FromInvalidString() throws IOException {
        // One byte to long.
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b00000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction(string(hash));
        assertThat(response.getInvocationResult().getState(), is(VM_STATE_FAULT));
    }

    @Test
    public void hash256ToByteArray() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    @Test
    public void hash256ToString() throws IOException {
        String hash = "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000";
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(hash));
    }

    static class HashIntegrationTestContract {

        public static Hash160 getZeroHash160() {
            return Hash160.zero();
        }

        public static boolean[] isHash160Zero(byte[] arr1, byte[] arr2) {
            boolean[] b = new boolean[2];
            b[0] = new Hash160(arr1).isZero();
            b[1] = new Hash160(arr2).isZero();
            return b;
        }

        public static boolean[] isHash160Valid(byte[] arr1, byte[] arr2) {
            boolean[] b = new boolean[2];
            b[0] = new Hash160(arr1).isValid();
            b[1] = new Hash160(arr2).isValid();
            return b;
        }

        public static Hash160 createHash160FromValidByteArray(byte[] b) {
            return new Hash160(b);
        }

        public static Hash160 createHash160FromInvalidByteArray(byte[] b) {
            return new Hash160(b);
        }

        public static Hash160 createHash160FromValidString(String s) {
            return new Hash160(s);
        }

        public static Hash160 createHash160FromInvalidString(String s) {
            return new Hash160(s);
        }

        public static byte[] hash160ToByteArray() {
            Hash160 point = new Hash160(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b");
            return point.toByteArray();
        }

        public static String hash160ToString() {
            Hash160 point = new Hash160(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b");
            return point.toString();
        }

        public static Hash256 getZeroHash256() {
            return Hash256.zero();
        }

        public static boolean[] isHash256Zero(byte[] arr1, byte[] arr2) {
            boolean[] b = new boolean[2];
            b[0] = new Hash256(arr1).isZero();
            b[1] = new Hash256(arr2).isZero();
            return b;
        }

        public static boolean[] isHash256Valid(byte[] arr1, byte[] arr2) {
            boolean[] b = new boolean[2];
            b[0] = new Hash256(arr1).isValid();
            b[1] = new Hash256(arr2).isValid();
            return b;
        }

        public static Hash256 createHash256FromValidByteArray(byte[] b) {
            return new Hash256(b);
        }

        public static Hash256 createHash256FromInvalidByteArray(byte[] b) {
            return new Hash256(b);
        }

        public static Hash256 createHash256FromValidString(String s) {
            return new Hash256(s);
        }

        public static Hash256 createHash256FromInvalidString(String s) {
            return new Hash256(s);
        }

        public static byte[] hash256ToByteArray() {
            Hash256 point = new Hash256(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000");
            return point.toByteArray();
        }

        public static String hash256ToString() {
            Hash256 point = new Hash256(
                    "03b4af8d061b6b320cce6c63bc4ec7894dce107b000000000000000000000000");
            return point.toString();
        }
    }


}
