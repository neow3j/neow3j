package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Map;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.List;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class StorageIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageIntegrationTest.StorageIntegrationTestContract.class.getName());

    private static final String DATA = "0001020304";
    private static final String KEY = "01";

    @BeforeClass
    public static void setUp() throws Throwable {
        ContractParameter key = byteArray(KEY);
        ContractParameter data = byteArray(DATA);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);
    }

    @Test
    public void getByByteArrayKey() throws IOException {
        ContractParameter key = byteArray("01");
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
    }

    @Test
    public void getByByteStringKey() throws IOException {
        ContractParameter key = byteArray("01");
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
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
        int v = 10;
        ContractParameter key = byteArray("02");
        ContractParameter value = integer(10);
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
        int v = 10;
        ContractParameter key = byteArray("02");
        ContractParameter value = integer(10);
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

    @Ignore("Depends on https://github.com/neow3j/neow3j/issues/488")
    @Test
    public void findByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0), is(KEY));
        assertThat(entry.get(1), is(DATA));
    }

    @Ignore("Depends on https://github.com/neow3j/neow3j/issues/488")
    @Test
    public void findByByteArrayPrefix() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0), is(KEY));
        assertThat(entry.get(1), is(DATA));
    }

    static class StorageIntegrationTestContract {

        static StorageContext ctx = Storage.getStorageContext();

        public static void storeData(byte[] key, byte[] data) {
            Storage.put(ctx, key, data);
        }

        public static ByteString getByByteArrayKey(byte[] key) {
            return Storage.get(ctx, key);
        }

        public static ByteString getByByteStringKey(ByteString key) {
            return Storage.get(ctx, key);
        }

        public static ByteString putByteArrayKeyByteArrayValue(byte[] key, byte[] value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteArrayKeyByteStringValue(byte[] key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteArrayKeyIntegerValue(byte[] key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyByteArrayValue(ByteString key, byte[] value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyByteStringValue(ByteString key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyIntegerValue(ByteString key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByByteArrayKey(byte[] key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByByteStringKey(ByteString key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static Map.Entry<byte[], byte[]> findByByteStringPrefix(ByteString prefix) {
            Iterator<Map.Entry<byte[], byte[]>> it = Storage.find(ctx, prefix);
            it.next();
            Map.Entry<byte[], byte[]> entry = it.getValue();
            return entry;
        }

        public static Map.Entry<byte[], byte[]> findByByteArrayPrefix(byte[] prefix) {
            Iterator<Map.Entry<byte[], byte[]>> it = Storage.find(ctx, prefix);
            it.next();
            Map.Entry<byte[], byte[]> entry = it.getValue();
            return entry;
        }

    }
}
