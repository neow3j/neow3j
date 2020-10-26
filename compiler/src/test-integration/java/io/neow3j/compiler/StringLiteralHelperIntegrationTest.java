package io.neow3j.compiler;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.StringLiteralHelper.stringToInt;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringLiteralHelperIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(StringLiterals.class.getName());
    }

    @Test
    public void addressToScriptHashInMethod() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String littleEndianScriptHash = Numeric.reverseHexString(
                "cc45cc8987b0e35371f5685431e3c8eeea306722");
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(littleEndianScriptHash));
    }

    @Test
    public void addressToScriptHashInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String littleEndianScriptHash = Numeric.reverseHexString(
                "cc45cc8987b0e35371f5685431e3c8eeea306722");
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(littleEndianScriptHash));
    }

    @Test
    public void addressToScriptHashInLocalVariable() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String littleEndianScriptHash = Numeric.reverseHexString(
                "cc45cc8987b0e35371f5685431e3c8eeea306722");
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(littleEndianScriptHash));
    }

    @Test
    public void hexStringToByteArrayInMethod() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(expected));
    }

    @Test
    public void hexStringToByteArrayInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(expected));
    }

    @Test
    public void hexStringToByteArrayInLocalVariable() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(expected));
    }

    @Test
    public void stringToIntegerInMethod() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
            assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                    is(expected));
    }

    @Test
    public void stringToIntegerInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(expected));
    }

    @Test
    public void stringToIntegerInLocalVariable() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(expected));
    }

    static class StringLiterals {

        private static final byte[] scriptHash =
                addressToScriptHash("AJunErzotcQTNWP2qktA7LgkXZVdHea97H");

        private static final byte[] bytes = hexToBytes("0x010203");

        private static final int integer = stringToInt("1000000000000000000000000000000");

        public static byte[] addressToScriptHashInMethod() {
            return addressToScriptHash("AJunErzotcQTNWP2qktA7LgkXZVdHea97H");
        }

        public static byte[] addressToScriptHashInStaticVariableInitialization() {
            return scriptHash;
        }

        public static byte[] addressToScriptHashInLocalVariable() {
            byte[] bytes = addressToScriptHash("AJunErzotcQTNWP2qktA7LgkXZVdHea97H");
            return bytes;
        }

        public static byte[] hexStringToByteArrayInMethod() {
            return hexToBytes("0x010203");
        }

        public static byte[] hexStringToByteArrayInStaticVariableInitialization() {
            return bytes;
        }

        public static byte[] hexStringToByteArrayInLocalVariable() {
            byte[] bytes = hexToBytes("0x010203");
            return bytes;
        }

        public static int stringToIntegerInMethod() {
            return stringToInt("1000000000000000000000000000000");
        }

        public static int stringToIntegerInStaticVariableInitialization() {
            return integer;
        }

        public static int stringToIntegerInLocalVariable() {
            int integer = stringToInt("1000000000000000000000000000000");
            return integer;
        }
    }
}

