package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.FindOptions;
import io.neow3j.devpack.InteropInterface;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class InstanceOfIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            InstanceOfIntegrationTestContract.class.getName());

    @Test
    public void instanceOfString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("Neo"));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void instanceOfByteString() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.byteArray("010203"));
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
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.bool(true));
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


    static class InstanceOfIntegrationTestContract {

        public static boolean instanceOfString(Object obj) {
            return obj instanceof String;
        }

        public static boolean instanceOfByteString(Object obj) {
            return obj instanceof ByteString;
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


    }

}
