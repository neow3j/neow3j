package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Iterator.Struct;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StorageIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            StorageIntegrationTest.StorageIntegrationTestContract.class.getName());

    private static final String KEY1_HEX = "01";
    private static final String DATA1 = "0001020304";

    private static final String KEY2_STRING = "hello";
    private static final String DATA2 = "world";

    private static final String KEY3_HEX = "04";
    private static final BigInteger INTEGER3 = BigInteger.valueOf(42);

    private static final String KEY4_STRING = "neow3j";
    private static final BigInteger INTEGER4 = BigInteger.valueOf(13);

    @BeforeClass
    public static void setUp() throws Throwable {
        ContractParameter key = byteArray(KEY1_HEX);
        ContractParameter data = byteArray(DATA1);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);

        key = byteArrayFromString(KEY2_STRING);
        data = byteArrayFromString(DATA2);
        ct.invokeFunctionAndAwaitExecution("storeData", key, data);

        key = byteArray(KEY3_HEX);
        data = integer(INTEGER3);
        ct.invokeFunctionAndAwaitExecution("storeInteger", key, data);

        key = string(KEY4_STRING);
        data = integer(INTEGER4);
        ct.invokeFunctionAndAwaitExecution("storeInteger", key, data);
    }

    @Test
    public void getByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByStringKey() throws IOException {
        ContractParameter key = string(KEY2_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    // getByteArray

    @Test
    public void getByteArrayByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByteArrayByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getByteArrayByStringKey() throws IOException {
        ContractParameter key = string(KEY2_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    // getString

    @Test
    public void getStringByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getStringByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void getStringByStringKey() throws IOException {
        ContractParameter key = string(KEY2_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    // getInteger

    @Test
    public void getIntegerByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));
    }

    @Test
    public void getIntegerByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));
    }

    @Test
    public void getIntegerByStringKey() throws IOException {
        ContractParameter key = string(KEY4_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER4));
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
    public void putByteArrayKeyHash160Value() throws IOException {
        Hash160 v = ct.getDefaultAccount().getScriptHash();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putByteArrayKeyHash256Value() throws IOException {
        Hash256 v = ct.getDeployTxHash();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
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
    public void putByteStringKeyHash160Value() throws IOException {
        Hash160 v = ct.getDefaultAccount().getScriptHash();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putByteStringKeyHash256Value() throws IOException {
        Hash256 v = ct.getDeployTxHash();
        ContractParameter key = byteArray("02");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
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
    public void putStringKeyHash160Value() throws IOException {
        io.neow3j.types.Hash160 v = ct.getDefaultAccount().getScriptHash();
        ContractParameter key = string("key");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putStringKeyHash256Value() throws IOException {
        Hash256 v = ct.getDeployTxHash();
        ContractParameter key = string("key");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    @Test
    public void deleteByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction("getByByteArrayKey", key)
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction("getByByteStringKey", key)
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByStringKey() throws IOException {
        ContractParameter key = string(KEY2_STRING);
        InvocationResult res = ct.callInvokeFunction("getByStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void findByByteStringPrefix() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0).getHexString(), is(KEY1_HEX));
        assertThat(entry.get(1).getHexString(), is(DATA1));
    }

    @Test
    public void findByByteArrayPrefix() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0).getHexString(), is(KEY1_HEX));
        assertThat(entry.get(1).getHexString(), is(DATA1));
    }

    @Test
    public void findByStringPrefix() throws IOException {
        ContractParameter key = string(KEY2_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0).getString(), is(KEY2_STRING));
        assertThat(entry.get(1).getString(), is(DATA2));
    }

    @Test
    public void findWithFindOptionValuesOnly() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0).getHexString(), is(DATA1));
        assertThat(entry.get(1).getHexString(), is("102030"));
    }

    @Test
    public void findWithFindOptionDeserializeValues() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        Map<StackItem, StackItem> map = res.getStack().get(0).getMap();
        assertTrue(map.containsKey(new ByteStringStackItem(KEY1_HEX)));
        assertTrue(map.containsKey(new ByteStringStackItem("0102")));
    }

    static class StorageIntegrationTestContract {

        static StorageContext ctx = Storage.getStorageContext();

        public static void storeData(byte[] key, byte[] data) {
            Storage.put(ctx, key, data);
        }

        public static void storeInteger(byte[] key, int value) {
            Storage.put(ctx, key, value);
        }

        // get

        public static ByteString getByByteArrayKey(byte[] key) {
            return Storage.get(ctx, key);
        }

        public static ByteString getByByteStringKey(ByteString key) {
            return Storage.get(ctx, key);
        }

        public static ByteString getByStringKey(String key) {
            return Storage.get(ctx, key);
        }

        // getByteArray

        public static byte[] getByteArrayByByteArrayKey(byte[] key) {
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByByteStringKey(ByteString key) {
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByStringKey(String key) {
            return Storage.getByteArray(ctx, key);
        }

        // getString

        public static String getStringByByteArrayKey(byte[] key) {
            return Storage.getString(ctx, key);
        }

        public static String getStringByByteStringKey(ByteString key) {
            return Storage.getString(ctx, key);
        }

        public static String getStringByStringKey(String key) {
            return Storage.getString(ctx, key);
        }

        // getInteger

        public static int getIntegerByByteArrayKey(byte[] key) {
            return Storage.getInteger(ctx, key);
        }

        public static int getIntegerByByteStringKey(ByteString key) {
            return Storage.getInteger(ctx, key);
        }

        public static int getIntegerByStringKey(String key) {
            return Storage.getInteger(ctx, key);
        }

        // putByteArray

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

        public static ByteString putByteArrayKeyHash160Value(byte[] key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteArrayKeyHash256Value(byte[] key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // putByteString

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

        public static ByteString putByteStringKeyHash160Value(ByteString key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putByteStringKeyHash256Value(ByteString key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // putString

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

        public static ByteString putStringKeyHash160Value(String key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putStringKeyHash256Value(String key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // delete

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

        // find

        public static Map.Entry<ByteString, ByteString> findByByteStringPrefix(ByteString prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Map.Entry<ByteString, ByteString> findByByteArrayPrefix(byte[] prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Map.Entry<ByteString, ByteString> findByStringPrefix(String prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static io.neow3j.devpack.List<ByteString> findWithFindOptionValuesOnly() {
            Storage.put(ctx, hexToBytes("0102"), hexToBytes("102030"));
            Iterator<ByteString> it = Storage.find(ctx, hexToBytes("01"), FindOptions.ValuesOnly);
            io.neow3j.devpack.List<ByteString> list = new io.neow3j.devpack.List<>();
            it.next();
            list.add(it.get());
            it.next();
            list.add(it.get());
            return list;
        }

        public static io.neow3j.devpack.Map<ByteString, ByteString> findWithFindOptionDeserializeValues() {
            Storage.put(ctx, hexToBytes("0102"), hexToBytes("102030"));
            byte findOption = FindOptions.DeserializeValues & FindOptions.PickField0;
            Iterator<Struct<ByteString, ByteString>> it = Storage.find(ctx,
                    hexToBytes("01"), findOption);
            io.neow3j.devpack.Map<ByteString, ByteString> map = new io.neow3j.devpack.Map<>();
            it.next();
            Struct<ByteString, ByteString> entry = it.get();
            map.put(entry.key, entry.value);
            it.next();
            entry = it.get();
            map.put(entry.key, entry.value);
            return map;
        }
    }

}
