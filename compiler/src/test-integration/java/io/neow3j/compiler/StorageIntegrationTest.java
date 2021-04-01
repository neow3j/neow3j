package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.FindOptions;
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
import static io.neow3j.contract.ContractParameter.byteArrayFromString;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class StorageIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageIntegrationTest.StorageIntegrationTestContract.class.getName());

    private static final String DATA1 = "0001020304";
    private static final String KEY1 = "01";

    private static final String DATA2 = "world";
    private static final String KEY2 = "hello";

    @BeforeClass
    public static void setUp() throws Throwable {
        ContractParameter key = byteArray(KEY1);
        ContractParameter data = byteArray(DATA1);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);

        key = byteArrayFromString(KEY2);
        data = byteArrayFromString(DATA2);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);
    }

    @Test
    public void getByByteArrayKey() throws IOException {
        ContractParameter key = byteArray("01");
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByByteStringKey() throws IOException {
        ContractParameter key = byteArray("01");
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByStringKey() throws IOException {
        ContractParameter key = string("hello");
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
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
    public void putByteArrayKeyStringValue() throws IOException {
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
    public void putByteStringKeyStringValue() throws IOException {
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
    public void putStringKeyByteArrayValue() throws IOException {
        String v = "050607";
        ContractParameter key = string("key");
        ContractParameter value = byteArray(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(v));
    }

    @Test
    public void putStringKeyByteStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = string("key");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putStringKeyStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = string("key");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putStringKeyIntegerValue() throws IOException {
        int v = 10;
        ContractParameter key = string("key");
        ContractParameter value = integer(10);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(v));
    }

    @Test
    public void deleteByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY1);
        InvocationResult res = ct.callInvokeFunction("getByByteArrayKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY1);
        InvocationResult res = ct.callInvokeFunction("getByByteStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByStringKey() throws IOException {
        ContractParameter key = string(KEY2);
        InvocationResult res = ct.callInvokeFunction("getByStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Ignore("Depends on https://github.com/neow3j/neow3j/issues/488")
    @Test
    public void findByByteStringPrefix() throws IOException {
        ContractParameter key = byteArray(KEY1);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0), is(KEY1));
        assertThat(entry.get(1), is(DATA1));
    }

    @Ignore("Depends on https://github.com/neow3j/neow3j/issues/488")
    @Test
    public void findByByteArrayPrefix() throws IOException {
        ContractParameter key = byteArray(KEY1);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0), is(KEY1));
        assertThat(entry.get(1), is(DATA1));
    }

//    @Ignore("Depends on https://github.com/neow3j/neow3j/issues/488")
    @Test
    public void findByStringPrefix() throws IOException {
        ContractParameter key = string(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0), is(KEY2));
        assertThat(entry.get(1), is(DATA2));
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

        public static ByteString getByStringKey(String key) {
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

        public static ByteString putByteArrayKeyStringValue(byte[] key, String value) {
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

        public static ByteString putByteStringKeyStringValue(ByteString key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyIntegerValue(ByteString key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyByteArrayValue(String key, byte[] value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyByteStringValue(String key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyStringValue(String key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyIntegerValue(String key, int value) {
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

        public static ByteString deleteByStringKey(String key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static Map.Entry<ByteString, ByteString> findByByteStringPrefix(ByteString prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.getValue();
        }

        public static Map.Entry<ByteString, ByteString> findByByteArrayPrefix(byte[] prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.getValue();
        }

        public static Map.Entry<ByteString, ByteString> findByStringPrefix(String prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.getValue();
        }

//        public static ByteString findWithFindOptions() {
//            Storage.put(ctx, hexToBytes("0102"), hexToBytes("102030"));
//            Iterator<ByteString> it = Storage.find(ctx, hexToBytes("01"),
//                    (byte) (FindOptions.DeserializeValues | FindOptions.ValuesOnly));
//            it.next();
//            ByteString entry = it.getValue();
//            return entry;
//        }

    }
}
