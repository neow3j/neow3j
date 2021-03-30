package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash256;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class StorageMapIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageMapIntegrationTest.StorageMapIntegrationTestContract.class.getName());

    private static final String KEY = "0203";
    private static final String DATA = "040506";

    @BeforeClass
    public static void setUp() throws Throwable {
        ContractParameter key = byteArray(KEY);
        ContractParameter data = byteArray(DATA);
        Hash256 tx = ct.invokeFunctionAndAwaitExecution("putData", key, data);
    }

    @Test
    public void getByByteString() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is("040506"));
    }

    @Test
    public void getByByteArray() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is("040506"));
    }

    @Test
    public void putByteArrayKeyByteArrayValue() throws IOException {
        String v = "050607";
        ContractParameter key = byteArray("02");
        ContractParameter value = byteArray(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(v));
    }

    @Test
    public void putByteArrayKeyByteStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = byteArray("02");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putByteArrayKeyIntegerValue() throws IOException {
        int v = 11;
        ContractParameter key = byteArray("02");
        ContractParameter value = integer(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(v));
    }

    @Test
    public void putByteStringKeyByteArrayValue() throws IOException {
        String v = "050607";
        ContractParameter key = byteArray("02");
        ContractParameter value = byteArray(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(v));
    }

    @Test
    public void putByteStringKeyByteStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = byteArray("02");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putByteStringKeyIntegerValue() throws IOException {
        int v = 11;
        ContractParameter key = byteArray("02");
        ContractParameter value = integer(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(v));
    }

    @Test
    public void deleteByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    static class StorageMapIntegrationTestContract {

        static ByteString prefix = hexToBytes("0001");
        static StorageContext ctx = Storage.getStorageContext();
        static StorageMap map = new StorageMap(ctx, prefix.toByteArray());

        public static void putData(ByteString key, ByteString data) {
            Storage.put(ctx, prefix.concat(key), data);
        }

        public static ByteString getByByteString(ByteString s) {
            return map.get(s);
        }

        public static ByteString getByByteArray(byte[] b) {
            return map.get(b);
        }

        public static ByteString putByteArrayKeyByteArrayValue(byte[] key, byte[] value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteArrayKeyByteStringValue(byte[] key, ByteString value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteArrayKeyIntegerValue(byte[] key, int value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyByteArrayValue(ByteString key, byte[] value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyByteStringValue(ByteString key, ByteString value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyIntegerValue(ByteString key, int value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByByteArrayKey(byte[] key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByByteStringKey(ByteString key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

    }
}
