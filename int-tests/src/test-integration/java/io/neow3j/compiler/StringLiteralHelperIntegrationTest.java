package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.devpack.StringLiteralHelper.addressToScriptHash;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.devpack.StringLiteralHelper.stringToInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringLiteralHelperIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(StringLiterals.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void addressToScriptHashInMethod() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String littleEndianScriptHash = Numeric.reverseHexString("0f46dc4287b70117ce8354924b5cb3a47215ad93");
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(littleEndianScriptHash));
    }

    @Test
    public void addressToScriptHashInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String littleEndianScriptHash = Numeric.reverseHexString("0f46dc4287b70117ce8354924b5cb3a47215ad93");
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(littleEndianScriptHash));
    }

    @Test
    public void addressToScriptHashInLocalVariable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String littleEndianScriptHash = Numeric.reverseHexString("0f46dc4287b70117ce8354924b5cb3a47215ad93");
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(littleEndianScriptHash));
    }

    @Test
    public void hexStringToByteArrayInMethod() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(expected));
    }

    @Test
    public void hexStringToByteArrayInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(expected));
    }

    @Test
    public void hexStringToByteArrayInLocalVariable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        String expected = "010203";
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(), is(expected));
    }

    @Test
    public void stringToIntegerInMethod() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(expected));
    }

    @Test
    public void stringToIntegerInStaticVariableInitialization() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(expected));
    }

    @Test
    public void stringToIntegerInLocalVariable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        BigInteger expected = new BigInteger("1000000000000000000000000000000");
        assertThat(response.getInvocationResult().getStack().get(0).getInteger(), is(expected));
    }

    static class StringLiterals {

        private static final Hash160 scriptHash =
                addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");

        private static final ByteString bytes = hexToBytes("0x010203");

        private static final int integer = stringToInt("1000000000000000000000000000000");

        public static Hash160 addressToScriptHashInMethod() {
            return addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
        }

        public static Hash160 addressToScriptHashInStaticVariableInitialization() {
            return scriptHash;
        }

        public static Hash160 addressToScriptHashInLocalVariable() {
            Hash160 bytes = addressToScriptHash("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
            return bytes;
        }

        public static ByteString hexStringToByteArrayInMethod() {
            return hexToBytes("0x010203");
        }

        public static ByteString hexStringToByteArrayInStaticVariableInitialization() {
            return bytes;
        }

        public static ByteString hexStringToByteArrayInLocalVariable() {
            ByteString bytes = hexToBytes("0x010203");
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
