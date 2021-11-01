package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.protocol.core.response.InvocationResult;
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
import java.nio.charset.StandardCharsets;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
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
    private static final byte[] DATA_BYTEARRAY = new byte[]{4, 5, 6};

    private static final String KEY2 = "hello";
    private static final String DATA2 = "world";

    private static final String KEY3 = "neow";
    private static final Integer DATA3 = 13;

    private static final Integer KEY4 = 1001;
    private static final String DATA4 = "moon";

    private static final Integer KEY5 = 1234;
    private static final Integer DATA5 = 255;

    @BeforeClass
    public static void setUp() throws Throwable {
        String storeData = "storeData";
        String storeInteger = "storeInteger";

        ContractParameter key = byteArray(KEY);
        ContractParameter data = byteArray(DATA);
        ct.invokeFunctionAndAwaitExecution(storeData, key, data);

        key = byteArrayFromString(KEY2);
        data = byteArrayFromString(DATA2);
        ct.invokeFunctionAndAwaitExecution(storeData, key, data);

        key = byteArrayFromString(KEY3);
        data = integer(DATA3);
        ct.invokeFunctionAndAwaitExecution(storeInteger, key, data);

        key = integer(KEY4);
        data = byteArrayFromString(DATA4);
        ct.invokeFunctionAndAwaitExecution(storeData, key, data);

        key = integer(KEY5);
        data = integer(DATA5);
        ct.invokeFunctionAndAwaitExecution(storeInteger, key, data);
    }

    // region get bytestring key

    @Test
    public void getByByteStringKey() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
    }

    @Test
    public void getByteArrayByByteStringKey() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA_BYTEARRAY));
    }

    @Test
    public void getStringByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    @Test
    public void getIntegerByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY3);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA3)));
    }

    // endregion get bytestring key
    // region get bytearray key

    @Test
    public void getByByteArrayKey() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
    }

    @Test
    public void getByteArrayByByteArrayKey() throws IOException {
        ContractParameter param = byteArray(KEY);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA_BYTEARRAY));
    }

    @Test
    public void getStringByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    @Test
    public void getIntegerByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY3);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA3)));
    }

    // endregion get bytearray key
    // region get string key

    @Test
    public void getByStringKey() throws IOException {
        ContractParameter param = string(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    @Test
    public void getByteArrayByStringKey() throws IOException {
        ContractParameter param = string(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(),
                is(DATA2.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void getStringByStringKey() throws IOException {
        ContractParameter param = string(KEY2);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA2));
    }

    @Test
    public void getIntegerByStringKey() throws IOException {
        ContractParameter param = string(KEY3);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA3)));
    }

    // endregion get string key
    // region get integer key

    @Test
    public void getByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA4));
    }

    @Test
    public void getByteArrayByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA4.getBytes()));
    }

    @Test
    public void getStringByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA4));
    }

    @Test
    public void getIntegerByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA5)));
    }

    // endregion get integer key
    // region put bytearray key

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
    public void putByteArrayKeyStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = byteArray("02");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
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

    // endregion put bytearray key
    // region put bytestring key

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
    public void putByteStringKeyStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = byteArray("02");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
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

    // endregion put bytestring key
    // region put string key

    @Test
    public void putStringKeyByteArrayValue() throws IOException {
        String v = "050607";
        ContractParameter key = string("aa");
        ContractParameter value = byteArray(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(v));
    }

    @Test
    public void putStringKeyByteStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = string("aa");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putStringKeyIntegerValue() throws IOException {
        int v = 11;
        ContractParameter key = string("aa");
        ContractParameter value = integer(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger().intValue(), is(v));
    }

    @Test
    public void putStringKeyStringValue() throws IOException {
        String v = "hello, world!";
        ContractParameter key = string("aa");
        ContractParameter value = string(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putStringKeyHash160Value() throws IOException {
        Hash160 v = ct.getDefaultAccount().getScriptHash();
        ContractParameter key = string("aa");
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putStringKeyHash256Value() throws IOException {
        Hash256 v = ct.getDeployTxHash();
        ContractParameter key = string("aa");
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    // endregion put string key
    // region put integer key

    @Test
    public void putIntegerKeyByteArrayValue() throws IOException {
        ContractParameter key = integer(513);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putIntegerKeyByteStringValue() throws IOException {
        ContractParameter key = integer(513);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putIntegerKeyIntegerValue() throws IOException {
        ContractParameter key = integer(513);
        ContractParameter value = integer(28);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(value.getValue()));
    }

    @Test
    public void putIntegerKeyStringValue() throws IOException {
        ContractParameter key = integer(513);
        ContractParameter value = string("hello neow3j");
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello neow3j"));
    }

    @Test
    public void putIntegerKeyHash160Value() throws IOException {
        Hash160 v = ct.getClient1().getScriptHash();
        ContractParameter key = integer(515);
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putIntegerKeyHash256Value() throws IOException {
        Hash256 v = ct.getBlockHashOfDeployTx();
        ContractParameter key = integer(143);
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    // endregion put integer key
    // region delete

    @Test
    public void deleteByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res =
                ct.callInvokeFunction("getByByteArrayKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY);
        InvocationResult res =
                ct.callInvokeFunction("getByByteStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA));
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

    @Test
    public void deleteByInteger() throws IOException {
        ContractParameter key = integer(KEY5);
        InvocationResult res = ct.callInvokeFunction("getByIntegerKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA5)));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    // endregion delete

    static class StorageMapIntegrationTestContract {

        static ByteString prefix = hexToBytes("0001");
        static StorageContext ctx = Storage.getStorageContext();
        static StorageMap map = new StorageMap(ctx, prefix.toByteArray());

        // region store data

        public static void storeData(ByteString key, ByteString data) {
            Storage.put(ctx, prefix.concat(key), data);
        }

        public static void storeInteger(ByteString key, int data) {
            Storage.put(ctx, prefix.concat(key), data);
        }

        // endregion store data
        // region get bytestring key

        public static ByteString getByByteStringKey(ByteString s) {
            return map.get(s);
        }

        public static byte[] getByteArrayByByteStringKey(ByteString s) {
            return map.getByteArray(s);
        }

        public static String getStringByByteStringKey(ByteString s) {
            return map.getString(s);
        }

        public static int getIntegerByByteStringKey(ByteString s) {
            return map.getInteger(s);
        }

        // endregion get bytestring key
        // region get bytearray key

        public static ByteString getByByteArrayKey(byte[] b) {
            return map.get(b);
        }

        public static byte[] getByteArrayByByteArrayKey(byte[] b) {
            return map.getByteArray(b);
        }

        public static String getStringByByteArrayKey(byte[] b) {
            return map.getString(b);
        }

        public static int getIntegerByByteArrayKey(byte[] b) {
            return map.getInteger(b);
        }

        // endregion get bytearray key
        // region get string key

        public static ByteString getByStringKey(String s) {
            return map.get(s);
        }

        public static byte[] getByteArrayByStringKey(String s) {
            return map.getByteArray(s);
        }

        public static String getStringByStringKey(String s) {
            return map.getString(s);
        }

        public static int getIntegerByStringKey(String s) {
            return map.getInteger(s);
        }

        // endregion get string key
        // region get integer key

        public static ByteString getByIntegerKey(int i) {
            return map.get(i);
        }

        public static byte[] getByteArrayByIntegerKey(int i) {
            return map.getByteArray(i);
        }

        public static String getStringByIntegerKey(int i) {
            return map.getString(i);
        }

        public static int getIntegerByIntegerKey(int i) {
            return map.getInteger(i);
        }

        // endregion get integer key
        // region put bytearray key

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

        public static ByteString putByteArrayKeyStringValue(byte[] key, String value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteArrayKeyHash160Value(byte[] key,
                io.neow3j.devpack.Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteArrayKeyHash256Value(byte[] key,
                io.neow3j.devpack.Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion put bytearray key
        // region put bytestring key

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

        public static ByteString putByteStringKeyStringValue(ByteString key, String value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyHash160Value(ByteString key,
                io.neow3j.devpack.Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putByteStringKeyHash256Value(ByteString key,
                io.neow3j.devpack.Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion put bytestring key
        // region put string key

        public static ByteString putStringKeyByteArrayValue(String key, byte[] value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyByteStringValue(String key, ByteString value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyIntegerValue(String key, int value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyStringValue(String key, String value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyHash160Value(String key,
                io.neow3j.devpack.Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putStringKeyHash256Value(String key,
                io.neow3j.devpack.Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion put string key
        // region put integer key

        public static ByteString putIntegerKeyByteArrayValue(int key, byte[] value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putIntegerKeyByteStringValue(int key, ByteString value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putIntegerKeyIntegerValue(int key, int value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putIntegerKeyStringValue(int key, String value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putIntegerKeyHash160Value(int key,
                io.neow3j.devpack.Hash160 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putIntegerKeyHash256Value(int key,
                io.neow3j.devpack.Hash256 value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion put integer key
        // region delete

        public static ByteString deleteByByteArrayKey(byte[] key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByByteStringKey(ByteString key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByStringKey(String key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByInteger(int key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion delete

    }

}
