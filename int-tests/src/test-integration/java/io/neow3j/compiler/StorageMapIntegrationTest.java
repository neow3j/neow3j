package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.StorageMap;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.StackItemType;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageMapIntegrationTest {

    // Store data to a key parameter that contains the prefix
    private static final String STORE_DATA_FULLKEY = "storeDataFullKey";
    // Store data to a key parameter that will be prepended by a default prefix
    private static final String STORE_DATA = "storeData";
    private static final String STORE_WITH_INT_KEY = "storeWithIntKey";
    private static final String STORE_INT = "storeInteger";
    private static final String REMOVE_DATA = "removeData";

    // Keys and data to tests StorageMap initialization
    private static final byte PREFIX1 = 0x01;
    private static final String KEY1 = "01";
    private static final String DATA1 = "0001020304";

    private static final byte[] PREFIX2 = new byte[]{(byte) 0xa1, (byte) 0xb1};
    private static final int PREFIX2_INT = -20063;
    private static final String KEY2 = "02";
    private static final String DATA2 = "0ab105c802";

    private static final String PREFIX3 = "prefix";
    private static final String KEY3 = "03";
    private static final String DATA3 = "0ab105c802";

    // keys and data to test StorageMap interactions
    private static final String KEY4 = "0203";
    private static final String DATA4 = "040506";
    private static final byte[] DATA4_BYTEARRAY = new byte[]{4, 5, 6};

    private static final String KEY5 = "hello";
    private static final String DATA5 = "world";

    private static final String KEY6 = "neow";
    private static final Integer DATA6 = 13;

    private static final Integer KEY7 = 1001;
    private static final String DATA7 = "moon";

    private static final Integer KEY8 = 1234;
    private static final Integer DATA8 = 255;

    private static final Integer KEY9 = 42;
    private static final boolean BOOLEAN9 = false;

    private static final Integer KEY_WITHOUT_VALUE = 8;
    private static final String KEY_HEX_WITHOUT_VALUE = "08";

    private static Hash256 hash256;

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            StorageMapIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public void setUp() throws Throwable {
        byte[] bytes = concatenate(PREFIX1, hexStringToByteArray(KEY1));
        ContractParameter key = byteArray(bytes);
        ContractParameter data = byteArray(DATA1);
        hash256 = ct.invokeFunctionAndAwaitExecution(STORE_DATA_FULLKEY, key, data);

        bytes = concatenate(PREFIX2, hexStringToByteArray(KEY2));
        key = byteArray(bytes);
        data = byteArray(DATA2);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA_FULLKEY, key, data);

        bytes = concatenate(PREFIX3.getBytes(StandardCharsets.UTF_8), hexStringToByteArray(KEY3));
        key = byteArray(bytes);
        data = byteArray(DATA3);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA_FULLKEY, key, data);

        key = byteArray(KEY4);
        data = byteArray(DATA4);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArrayFromString(KEY5);
        data = byteArrayFromString(DATA5);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArrayFromString(KEY6);
        data = integer(DATA6);
        ct.invokeFunctionAndAwaitExecution(STORE_INT, key, data);

        key = integer(KEY7);
        data = byteArrayFromString(DATA7);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = integer(KEY8);
        data = integer(DATA8);
        ct.invokeFunctionAndAwaitExecution(STORE_INT, key, data);

        key = integer(KEY9);
        data = bool(BOOLEAN9);
        ct.invokeFunctionAndAwaitExecution(STORE_INT, key, data);
    }

    // region create map

    @Test
    public void createMapWithBytePrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, integer(PREFIX1), byteArray(KEY1))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA1));
    }

    @Test
    public void createMapWithByteArrayPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, byteArray(PREFIX2), byteArray(KEY2))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithByteStringPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, byteArray(PREFIX2), byteArray(KEY2))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithIntegerPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, integer(PREFIX2_INT), byteArray(KEY2))
                .getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA2));
    }

    @Test
    public void createMapWithStringPrefix() throws IOException {
        InvocationResult res = ct.callInvokeFunction(testName, string(PREFIX3), byteArray(KEY3))
                .getInvocationResult();
        // The method tries to write with a read-only storage context, i.e., it should FAULT.
        assertThat(res.getStack().get(0).getHexString(), is(DATA3));
    }

    // endregion create map
    // region get bytestring key

    @Test
    public void getByByteStringKey() throws IOException {
        ContractParameter param = byteArray(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA4));
    }

    @Test
    public void getHash160ByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getHash256ByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getByteArrayByByteStringKey() throws IOException {
        ContractParameter param = byteArray(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA4_BYTEARRAY));
    }

    @Test
    public void getStringByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    @Test
    public void getBooleanByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getIntByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));
    }

    @Test
    public void getIntOrZeroByByteStringKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));

        // Test that instructions return 0 if no value was found for the provided key.
        param = byteArrayFromString(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion get bytestring key
    // region get bytearray key

    @Test
    public void getByByteArrayKey() throws IOException {
        ContractParameter param = byteArray(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA4));
    }

    @Test
    public void getHash160ByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getHash256ByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getByteArrayByByteArrayKey() throws IOException {
        ContractParameter param = byteArray(KEY4);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA4_BYTEARRAY));
    }

    @Test
    public void getStringByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    @Test
    public void getBooleanByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getIntByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));
    }

    @Test
    public void getIntOrZeroByByteArrayKey() throws IOException {
        ContractParameter param = byteArrayFromString(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));

        // Test that instructions return 0 if no value was found for the provided key.
        param = byteArrayFromString(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion get bytearray key
    // region get string key

    @Test
    public void getByStringKey() throws IOException {
        ContractParameter param = string(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    @Test
    public void getHash160ByStringKey() throws Throwable {
        ContractParameter key = string(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getHash256ByStringKey() throws Throwable {
        ContractParameter key = string(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getByteArrayByStringKey() throws IOException {
        ContractParameter param = string(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(),
                is(DATA5.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void getStringByStringKey() throws IOException {
        ContractParameter param = string(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    @Test
    public void getBooleanByStringKey() throws IOException {
        ContractParameter param = string(KEY5);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getIntByStringKey() throws IOException {
        ContractParameter param = string(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));
    }

    @Test
    public void getIntOrZeroByStringKey() throws IOException {
        ContractParameter param = string(KEY6);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA6)));

        // Test that instructions return 0 if no value was found for the provided key.
        param = string(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion get string key
    // region get integer key

    @Test
    public void getByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY7);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA7));
    }

    @Test
    public void getHash160ByIntegerKey() throws Throwable {
        ContractParameter key = integer(1234567891);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_WITH_INT_KEY, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getHash256ByIntegerKey() throws Throwable {
        ContractParameter key = integer(1234567892);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_WITH_INT_KEY, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, key);
    }

    @Test
    public void getByteArrayByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY7);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA7.getBytes()));
    }

    @Test
    public void getStringByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY7);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA7));
    }

    @Test
    public void getBooleanByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY9);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    @Test
    public void getIntByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY8);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA8)));
    }

    @Test
    public void getIntOrZeroByIntegerKey() throws IOException {
        ContractParameter param = integer(KEY8);
        InvocationResult res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA8)));

        // Test that instructions return 0 if no value was found for the provided key.
        param = integer(KEY_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, param).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
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
        ContractParameter key = byteArray(KEY4);
        InvocationResult res =
                ct.callInvokeFunction("getByByteArrayKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA4));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY4);
        InvocationResult res =
                ct.callInvokeFunction("getByByteStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA4));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByStringKey() throws IOException {
        ContractParameter key = string(KEY5);
        InvocationResult res = ct.callInvokeFunction("getByStringKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    @Test
    public void deleteByInteger() throws IOException {
        ContractParameter key = integer(KEY8);
        InvocationResult res = ct.callInvokeFunction("getByIntegerKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(DATA8)));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    // endregion delete
    // region find

    @Test
    public void findWithRemovePrefixOption() throws IOException {
        List<StackItem> iterator = ct.callAndTraverseIterator(testName);

        assertThat(iterator, hasSize(6)); // key-value pairs 4 to 9

        StackItem found1 = iterator.get(0);
        assertThat(found1.getType(), is(StackItemType.STRUCT));
        assertThat(found1.getList().get(0).getHexString(), is(KEY4));
        assertThat(found1.getList().get(1).getByteArray(), is(DATA4_BYTEARRAY));

        StackItem found5 = iterator.get(4);
        assertThat(found5.getList().get(0).getInteger().intValue(), is(KEY8));
        assertThat(found5.getList().get(1).getInteger().intValue(), is(DATA8));
    }

    // endregion find

    static class StorageMapIntegrationTestContract {

        // default prefix
        static ByteString prefix = hexToBytes("0001");
        static StorageContext ctx = Storage.getStorageContext();
        static StorageMap map = new StorageMap(ctx, prefix.toByteArray());

        // region store and delete data

        public static void storeDataFullKey(byte[] fullKey, byte[] data) {
            Storage.put(ctx, fullKey, data);
        }

        public static void storeData(ByteString key, ByteString data) {
            map.put(key, data);
        }

        public static void storeWithIntKey(int key, ByteString data) {
            map.put(key, data);
        }

        public static void storeInteger(ByteString key, int data) {
            map.put(key, data);
        }

        public static void removeData(ByteString key) {
            map.delete(key);
        }

        // endregion store and delete data
        // region initialize StorageMap

        public static ByteString createMapWithByteStringPrefix(ByteString prefix, ByteString key) {
            assert prefix instanceof ByteString;
            StorageMap map = new StorageMap(ctx, prefix);
            return map.get(key);
        }

        public static ByteString createMapWithByteArrayPrefix(byte[] prefix, ByteString key) {
            byte[] prefixByteArray = new ByteString(prefix).toByteArray();
            assert prefixByteArray instanceof byte[];
            StorageMap map = new StorageMap(ctx, prefixByteArray);
            return map.get(key);
        }

        public static ByteString createMapWithStringPrefix(String prefix, ByteString key) {
            assert prefix instanceof String;
            StorageMap map = new StorageMap(ctx, prefix);
            return map.get(key);
        }

        public static ByteString createMapWithIntegerPrefix(Integer prefix, ByteString key) {
            assert prefix instanceof Integer;
            StorageMap map = new StorageMap(ctx, prefix);
            return map.get(key);
        }

        public static ByteString createMapWithBytePrefix(Byte prefix, ByteString key) {
            assert prefix instanceof Byte;
            StorageMap map = new StorageMap(ctx, prefix);
            return map.get(key);
        }

        // endregion initialize StorageMap
        // region get bytestring key

        public static ByteString getByByteStringKey(ByteString s) {
            return map.get(s);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByByteStringKey(ByteString s) {
            return map.getHash160(s);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByByteStringKey(ByteString s) {
            return map.getHash256(s);
        }

        public static byte[] getByteArrayByByteStringKey(ByteString s) {
            return map.getByteArray(s);
        }

        public static String getStringByByteStringKey(ByteString s) {
            return map.getString(s);
        }

        public static boolean getBooleanByByteStringKey(ByteString s) {
            return map.getBoolean(s);
        }

        public static int getIntByByteStringKey(ByteString s) {
            return map.getInt(s);
        }

        public static int getIntOrZeroByByteStringKey(ByteString s) {
            return map.getIntOrZero(s);
        }

        // endregion get bytestring key
        // region get bytearray key

        public static ByteString getByByteArrayKey(byte[] b) {
            return map.get(b);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByByteArrayKey(byte[] s) {
            return map.getHash160(s);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByByteArrayKey(byte[] s) {
            return map.getHash256(s);
        }

        public static byte[] getByteArrayByByteArrayKey(byte[] b) {
            return map.getByteArray(b);
        }

        public static String getStringByByteArrayKey(byte[] b) {
            return map.getString(b);
        }

        public static boolean getBooleanByByteArrayKey(byte[] b) {
            return map.getBoolean(b);
        }

        public static int getIntByByteArrayKey(byte[] b) {
            return map.getInt(b);
        }

        public static int getIntOrZeroByByteArrayKey(byte[] b) {
            return map.getIntOrZero(b);
        }

        // endregion get bytearray key
        // region get string key

        public static ByteString getByStringKey(String s) {
            return map.get(s);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByStringKey(String s) {
            return map.getHash160(s);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByStringKey(String s) {
            return map.getHash256(s);
        }

        public static byte[] getByteArrayByStringKey(String s) {
            return map.getByteArray(s);
        }

        public static String getStringByStringKey(String s) {
            return map.getString(s);
        }

        public static boolean getBooleanByStringKey(String s) {
            return map.getBoolean(s);
        }

        public static int getIntByStringKey(String s) {
            return map.getInt(s);
        }

        public static int getIntOrZeroByStringKey(String s) {
            return map.getIntOrZero(s);
        }

        // endregion get string key
        // region get integer key

        public static ByteString getByIntegerKey(int i) {
            return map.get(i);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByIntegerKey(int i) {
            return map.getHash160(i);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByIntegerKey(int i) {
            return map.getHash256(i);
        }

        public static byte[] getByteArrayByIntegerKey(int i) {
            return map.getByteArray(i);
        }

        public static String getStringByIntegerKey(int i) {
            return map.getString(i);
        }

        public static boolean getBooleanByIntegerKey(int i) {
            return map.getBoolean(i);
        }

        public static int getIntByIntegerKey(int i) {
            return map.getInt(i);
        }

        public static int getIntOrZeroByIntegerKey(int i) {
            return map.getIntOrZero(i);
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
        // region find

        public static Iterator findWithRemovePrefixOption() {
            return (Iterator<Map.Entry<ByteString, ByteString>>) map.find(FindOptions.RemovePrefix);
        }

        // endregion find

    }

}
