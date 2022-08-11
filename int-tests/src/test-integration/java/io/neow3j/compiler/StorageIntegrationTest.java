package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.Iterator.Struct;
import io.neow3j.devpack.Storage;
import io.neow3j.devpack.StorageContext;
import io.neow3j.devpack.constants.FindOptions;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
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
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageIntegrationTest {

    private static final String STORE_DATA = "storeData";
    private static final String STORE_INT = "storeInteger";

    private static final String KEY1_HEX = "01";
    private static final String DATA1 = "0001020304";

    private static final String KEY2_STRING = "hello";
    private static final String DATA2 = "world";

    private static final String KEY3_HEX = "04";
    private static final Integer KEY3_INT = 4;
    private static final BigInteger INTEGER3 = BigInteger.valueOf(42);

    private static final String KEY4_STRING = "io/neow3j";
    private static final BigInteger INTEGER4 = BigInteger.valueOf(13);

    private static final Integer KEY5_INT = 42;
    private static final String DATA5 = "neoooww";

    private static final String KEY6_HEX = "12";
    private static final boolean BOOLEAN_6 = true;

    private static final String KEY7_HEX = "13";
    private static final boolean BOOLEAN_7 = false;

    private static final String KEY8_HEX = "14";
    private static final String DATA8 = "00";

    private static final int KEY_WITHOUT_VALUE = 8;
    private static final String KEY_HEX_WITHOUT_VALUE = "08";

    private static Hash256 hash256;
    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(StorageIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @BeforeAll
    public void setUp() throws Throwable {
        ContractParameter key = byteArray(KEY1_HEX);
        ContractParameter data = byteArray(DATA1);
        hash256 = ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArrayFromString(KEY2_STRING);
        data = byteArrayFromString(DATA2);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArray(KEY3_HEX);
        data = integer(INTEGER3);
        ct.invokeFunctionAndAwaitExecution(STORE_INT, key, data);

        key = string(KEY4_STRING);
        data = integer(INTEGER4);
        ct.invokeFunctionAndAwaitExecution(STORE_INT, key, data);

        key = integer(KEY5_INT);
        data = byteArrayFromString(DATA5);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArray(KEY6_HEX);
        data = bool(BOOLEAN_6);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArray(KEY7_HEX);
        data = bool(BOOLEAN_7);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        key = byteArray(KEY8_HEX);
        data = byteArray(DATA8);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
    }

    // region get

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

    @Test
    public void getByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY5_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    // endregion get
    // region getHash160

    @Test
    public void getHash160ByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash160ByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash160ByStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash160ByIntegerKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        Hash160 hash160 = Account.create().getScriptHash();
        ContractParameter data = hash160(hash160);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    // endregion getHash160
    // region getHash256

    @Test
    public void getHash256ByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getHash256ByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getHash256ByStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getHash256ByIntegerKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = hash256(hash256);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    // endregion getHash256
    // region getByteArray

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

    @Test
    public void getByteArrayByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY5_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(DATA5.getBytes()));
    }

    // endregion getByteArray
    // region getString

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

    @Test
    public void getStringByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY5_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
    }

    // endregion getString
    // region getBoolean

    @Test
    public void getBooleanByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY6_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));

        key = byteArray(KEY7_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));

        key = byteArray(KEY8_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    @Test
    public void getBooleanByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY6_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));

        key = byteArray(KEY7_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));

        key = byteArray(KEY8_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    @Test
    public void getBooleanByStringKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));

        key = byteArray(KEY7_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));

        key = byteArray(KEY8_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    @Test
    public void getBooleanByIntegerKey() throws IOException {
        ContractParameter key = byteArray(KEY1_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(true));

        key = byteArray(KEY7_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));

        key = byteArray(KEY8_HEX);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    // endregion getBoolean
    // region getInteger

    @Test
    public void getIntByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));

        // Test that instructions return null if no value was found for the provided key.
        key = byteArray(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.ANY));
        assertNull(res.getStack().get(0).getValue());
    }

    @Test
    public void getIntOrZeroByByteArrayKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));

        // Test that instructions return 0 if no value was found for the provided key.
        key = byteArray(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getIntByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));

        // Test that instructions return null if no value was found for the provided key.
        key = byteArray(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.ANY));
        assertNull(res.getStack().get(0).getValue());
    }

    @Test
    public void getIntOrZeroByByteStringKey() throws IOException {
        ContractParameter key = byteArray(KEY3_HEX);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));

        // Test that instructions return 0 if no value was found for the provided key.
        key = byteArray(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getIntByStringKey() throws IOException {
        ContractParameter key = string(KEY4_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER4));

        // Test that instructions return null if no value was found for the provided key.
        key = string(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.ANY));
        assertNull(res.getStack().get(0).getValue());
    }

    @Test
    public void getIntOrZeroByStringKey() throws IOException {
        ContractParameter key = string(KEY4_STRING);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER4));

        // Test that instructions return 0 if no value was found for the provided key.
        key = string(KEY_HEX_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getIntByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY3_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));
    }

    @Test
    public void getIntOrZeroByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY3_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(INTEGER3));

        // Test that instructions return 0 if no value was found for the provided key.
        key = integer(KEY_WITHOUT_VALUE);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion getInteger
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

    // endregion put bytestring key
    // region put string key

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
        Hash160 v = ct.getDefaultAccount().getScriptHash();
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

    // endregion put string key
    // region put integer key

    @Test
    public void putIntegerKeyByteArrayValue() throws IOException {
        String v = "moooon";
        ContractParameter key = integer(133);
        ContractParameter value = byteArrayFromString(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putIntegerKeyByteStringValue() throws IOException {
        String s = "hello there";
        ContractParameter key = integer(134);
        ContractParameter value = byteArrayFromString(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putIntegerKeyStringValue() throws IOException {
        String s = "wow";
        ContractParameter key = integer(135);
        ContractParameter value = string(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putIntegerKeyIntegerValue() throws IOException {
        BigInteger i = BigInteger.valueOf(144);
        ContractParameter key = integer(136);
        ContractParameter value = integer(i);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(i));
    }

    @Test
    public void putIntegerKeyHash160Value() throws IOException {
        Hash160 scriptHash = ct.getClient1().getScriptHash();
        ContractParameter key = integer(137);
        ContractParameter value = hash160(scriptHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(scriptHash.toAddress()));
    }

    @Test
    public void putIntegerKeyHash256Value() throws IOException {
        Hash256 txHash = ct.getDeployTxHash();
        ContractParameter key = integer(140);
        ContractParameter value = hash256(txHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(txHash.toLittleEndianArray()));
    }

    @Test
    public void putByteKeyStringValue() throws IOException {
        String s = "wow";
        ContractParameter key = integer((byte) 13);
        ContractParameter value = string(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    // endregion put integer key
    // region delete

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
    public void deleteByIntegerKey() throws IOException {
        ContractParameter key = integer(KEY5_INT);
        InvocationResult res = ct.callInvokeFunction("getByIntegerKey", key).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(DATA5));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getValue(), is(nullValue()));
    }

    // endregion delete
    // region find

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
    public void findByIntegerPrefix() throws IOException {
        ContractParameter key = integer(KEY5_INT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        List<StackItem> entry = res.getStack().get(0).getList();
        assertThat(entry.get(0).getInteger(), is(BigInteger.valueOf(KEY5_INT)));
        assertThat(entry.get(1).getString(), is(DATA5));
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

    // endregion find

    static class StorageIntegrationTestContract {

        static StorageContext ctx = Storage.getStorageContext();

        // region store

        public static void storeData(byte[] key, byte[] data) {
            byte[] d = new ByteString(data).toByteArray();
            assert d instanceof byte[];
            Storage.put(ctx, key, d);
        }

        public static void storeInteger(byte[] key, int value) {
            Storage.put(ctx, key, value);
        }

        // endregion store
        // region get

        public static ByteString getByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.get(ctx, k);
        }

        public static ByteString getByByteStringKey(ByteString key) {
            return Storage.get(ctx, key);
        }

        public static ByteString getByStringKey(String key) {
            return Storage.get(ctx, key);
        }

        public static ByteString getByIntegerKey(int key) {
            return Storage.get(ctx, key);
        }

        // endregion get
        // region getHash160

        public static io.neow3j.devpack.Hash160 getHash160ByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash160 getHash160ByByteStringKey(ByteString key) {
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash160 getHash160ByStringKey(String key) {
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash160 getHash160ByIntegerKey(int key) {
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        // endregion getHash160
        // region getHash256

        public static io.neow3j.devpack.Hash256 getHash256ByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash256 getHash256ByByteStringKey(ByteString key) {
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash256 getHash256ByStringKey(String key) {
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash256 getHash256ByIntegerKey(int key) {
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        // endregion getHash256
        // region getByteArray

        public static byte[] getByteArrayByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.getByteArray(ctx, k);
        }

        public static byte[] getByteArrayByByteStringKey(ByteString key) {
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByStringKey(String key) {
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByIntegerKey(int key) {
            return Storage.getByteArray(ctx, key);
        }

        // endregion getByteArray
        // region getString

        public static String getStringByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.getString(ctx, k);
        }

        public static String getStringByByteStringKey(ByteString key) {
            return Storage.getString(ctx, key);
        }

        public static String getStringByStringKey(String key) {
            return Storage.getString(ctx, key);
        }

        public static String getStringByIntegerKey(int key) {
            return Storage.getString(ctx, key);
        }

        // endregion getString
        // region getBoolean

        public static boolean getBooleanByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.getBoolean(ctx, k);
        }

        public static boolean getBooleanByByteStringKey(ByteString key) {
            assert key instanceof ByteString;
            return Storage.getBoolean(ctx, key);
        }

        public static boolean getBooleanByStringKey(String key) {
            assert key instanceof String;
            return Storage.getBoolean(ctx, key);
        }

        public static boolean getBooleanByIntegerKey(Integer key) {
            return Storage.getBoolean(ctx, key);
        }

        // endregion getBoolean
        // region getInteger

        public static int getIntByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.getInt(ctx, k);
        }

        public static int getIntOrZeroByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            return Storage.getIntOrZero(ctx, k);
        }

        public static int getIntByByteStringKey(ByteString key) {
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByByteStringKey(ByteString key) {
            return Storage.getIntOrZero(ctx, key);
        }

        public static int getIntByStringKey(String key) {
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByStringKey(String key) {
            return Storage.getIntOrZero(ctx, key);
        }

        public static int getIntByIntegerKey(int key) {
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByIntegerKey(int key) {
            return Storage.getIntOrZero(ctx, key);
        }

        // endregion getInteger
        // region put bytearray key

        public static ByteString putByteArrayKeyByteArrayValue(byte[] key, byte[] value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, k, v);
            return Storage.get(ctx, k);
        }

        public static ByteString putByteArrayKeyByteStringValue(byte[] key, ByteString value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        public static ByteString putByteArrayKeyStringValue(byte[] key, String value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        public static ByteString putByteArrayKeyIntegerValue(byte[] key, int value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        public static ByteString putByteArrayKeyHash160Value(byte[] key,
                io.neow3j.devpack.Hash160 value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        public static ByteString putByteArrayKeyHash256Value(byte[] key,
                io.neow3j.devpack.Hash256 value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        // endregion put bytearray key
        // region put bytestring key

        public static ByteString putByteStringKeyByteArrayValue(ByteString key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
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

        // endregion put bytestring key
        // region put string key

        public static ByteString putStringKeyByteArrayValue(String key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
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

        // endregion put string key
        // region put integer key

        public static ByteString putIntegerKeyByteArrayValue(int key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
            return Storage.get(ctx, key);
        }

        public static ByteString putIntegerKeyByteStringValue(int key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putIntegerKeyStringValue(int key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putIntegerKeyIntegerValue(int key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putIntegerKeyHash160Value(int key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        public static ByteString putIntegerKeyHash256Value(int key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // just to be safe byte input uses method with int parameter
        public static ByteString putByteKeyStringValue(byte key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // endregion put integer key
        // region delete

        public static ByteString deleteByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            Storage.delete(ctx, k);
            return Storage.get(ctx, k);
        }

        public static ByteString deleteByByteStringKey(ByteString key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByStringKey(String key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByIntegerKey(int key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        // endregion delete
        // region find

        public static Map.Entry<ByteString, ByteString> findByByteStringPrefix(ByteString prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Map.Entry<ByteString, ByteString> findByByteArrayPrefix(byte[] prefix) {
            byte[] p = new ByteString(prefix).toByteArray();
            assert p instanceof byte[];
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, p, FindOptions.None);
            it.next();
            return it.get();
        }

        public static Map.Entry<ByteString, ByteString> findByStringPrefix(String prefix) {
            Iterator<Map.Entry<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Map.Entry<ByteString, ByteString> findByIntegerPrefix(int prefix) {
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

        // endregion find

    }

}

