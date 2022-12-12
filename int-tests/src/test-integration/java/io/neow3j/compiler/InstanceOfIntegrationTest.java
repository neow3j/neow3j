package io.neow3j.compiler;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.InteropInterface;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstanceOfIntegrationTest {

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            InstanceOfIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void instanceOfString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("Neo"));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfByteString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.byteArray("010203"));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfHash160() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.hash160(Hash160.ZERO));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfHash256() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.hash256(Hash256.ZERO));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfECPoint() throws IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.publicKey(ECKeyPair.createEcKeyPair().getPublicKey()));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfInteger() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.integer(10));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfBoolean() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(true));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfMap() throws IOException {
        java.util.Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.map(map));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfList() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.array("element1", "element2"));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfArray() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.array("element1", "element2"));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfInteropInterface() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfStruct() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfBuffer() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfInStaticVariable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfInIfElse() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello, world!"),
                bool(true));
        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("hello, world!"));
    }


    static class InstanceOfIntegrationTestContract {

        public static boolean instanceOfString(Object obj) {
            return obj instanceof String;
        }

        public static boolean instanceOfByteString(Object obj) {
            return obj instanceof ByteString;
        }

        public static boolean instanceOfHash160(Object obj) {
            return obj instanceof io.neow3j.devpack.Hash160;
        }

        public static boolean instanceOfHash256(Object obj) {
            return obj instanceof io.neow3j.devpack.Hash256;
        }

        public static boolean instanceOfECPoint(Object obj) {
            return obj instanceof io.neow3j.devpack.ECPoint;
        }

        public static boolean instanceOfInteger(Object obj) {
            return obj instanceof Integer;
        }

        public static boolean instanceOfBoolean(Object obj) {
            return obj instanceof Boolean;
        }

        public static boolean instanceOfMap(Object obj) {
            return obj instanceof Map;
        }

        public static boolean instanceOfList(Object obj) {
            return obj instanceof List;
        }

        public static boolean instanceOfArray(Object obj) {
            return obj instanceof String[];
        }

        public static boolean instanceOfInteropInterface() {
            Object obj = Storage.getStorageContext();
            return obj instanceof InteropInterface;
        }

        public static boolean instanceOfStruct() {
            StorageContext ctx = Storage.getStorageContext();
            ByteString prefix = StringLiteralHelper.hexToBytes("010203");
            Storage.put(ctx, prefix, "value");
            Iterator it = Storage.find(ctx, prefix, FindOptions.None);
            it.next();
            Object obj = it.get();
            return obj instanceof Iterator.Struct;
        }

        public static boolean instanceOfBuffer() {
            Object obj = new byte[]{0x01, 0x02, 0x03};
            return obj instanceof byte[];
        }

        static boolean var = "hello" instanceof String;

        public static boolean instanceOfInStaticVariable() {
            return var;
        }

        public static ByteString instanceOfInIfElse(Object s, boolean b) {
            if (s instanceof ByteString && b) {
                return (ByteString) s;
            }
            return new ByteString("");
        }

    }

}
