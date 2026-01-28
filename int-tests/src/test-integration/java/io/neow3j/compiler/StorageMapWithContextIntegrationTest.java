package io.neow3j.compiler;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
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

import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageMapWithContextIntegrationTest {

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

    private static final String KEY_HASH160_HEX = "56dd2a8942506015369a2919eed12f6c76d52aa9";
    private static final Hash160 KEY_HASH160 = new Hash160(KEY_HASH160_HEX);

    private static final String KEY_HASH256_HEX = "442050ddb914d41b80481a03938e63b1bb88a28f2acb8e636492205392e9f014";
    private static final Hash256 KEY_HASH256 = new Hash256(KEY_HASH256_HEX);

    private static final String KEY_ECPOINT_HEX = "03afcc76bedc01df1d28f95f4e173fdb1d7271c974a46e7cf277e7776737bebdf2";
    private static final ECPublicKey KEY_ECPOINT = new ECPublicKey(hexStringToByteArray(KEY_ECPOINT_HEX));

    private static final String ECPOINT_HEX_VAL = "02e986f4c8e007554e0c8d38861c273cc1a20eaf828417221fab6e4be9060ffcb7";
    private static final ECPublicKey ECPOINT_VAL = new ECPublicKey(hexStringToByteArray(ECPOINT_HEX_VAL));

    private static final String ECPOINT_HEX_VAL_2 =
            "0232e49fe7e4beea4b38d5cb71270ff63617aa7b59d071d26d37e5888bf23e2f51";
    private static final ECPublicKey ECPOINT_VAL_2 = new ECPublicKey(hexStringToByteArray(ECPOINT_HEX_VAL_2));

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
        InvocationResult res = ct.callInvokeFunction(testName, string(PREFIX3), byteArray(KEY3)).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(DATA3));
    }

    // endregion
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
    public void getECPointByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL_2);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(ECPOINT_HEX_VAL_2));
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

    // endregion
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
    public void getECPointByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));

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

    // endregion
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
    public void getECPointByStringKey() throws Throwable {
        ContractParameter key = string(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(ECPOINT_HEX_VAL));
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

    // endregion
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
    public void getECPointByIntegerKey() throws Throwable {
        ContractParameter key = integer(1234567892);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_WITH_INT_KEY, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
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

    // endregion
    // region get hash160 key

    @Test
    public void getByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArrayFromString("neow3j"));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("neow3j"));
    }

    @Test
    public void getHash160ByHash160Key() throws Throwable {
        Hash160 hash160 = new Hash160("a8387e42ccc4366121e519f6133090f0525e5ec6");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), hash160(hash160));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash256ByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), hash256(hash256));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getECPointByHash160Key() throws Throwable {
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), data);

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getByteArrayByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArrayFromString("My name is Neo."));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(res.getStack().get(0).getString(), is("My name is Neo."));
    }

    @Test
    public void getStringByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), string("Trinity"));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("Trinity"));
    }

    @Test
    public void getBooleanByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), bool(false));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertThat(res.getStack().get(0).getBoolean(), is(false));
    }

    @Test
    public void getIntByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), integer(888));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(888)));
    }

    @Test
    public void getIntOrZeroByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), integer(999));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(999)));

        // Test that instructions return 0 if no value was found for the provided key.
        Hash160 hash160KeyWithoutValue = new Hash160("daacdc4b4de1f4d93160565dd08d4f7b71b6a391");
        key = hash160(hash160KeyWithoutValue);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion
    // region get hash256 key

    @Test
    public void getByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                byteArrayFromString("Moon"));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("Moon"));
    }

    @Test
    public void getHash160ByHash256Key() throws Throwable {
        Hash160 hash160 = new Hash160("a8387e42ccc4366121e519f6133090f0525e5ec6");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), hash160(hash160));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash256ByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), hash256(hash256));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getECPointByHash256Key() throws Throwable {
        ContractParameter value = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), value);

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getByteArrayByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                byteArrayFromString("My name is Trinity."));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(res.getStack().get(0).getString(), is("My name is Trinity."));
    }

    @Test
    public void getStringByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                string("Trinity and Neo"));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("Trinity and Neo"));
    }

    @Test
    public void getBooleanByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), bool(true));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getIntByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), integer(777));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(777)));
    }

    @Test
    public void getIntOrZeroByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), integer(10100));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(10100)));

        // Test that instructions return 0 if no value was found for the provided key.
        Hash256 hash256NoValue = new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a22e");
        key = hash256(hash256NoValue);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion
    // region get ecpoint key

    @Test
    public void getByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), byteArrayFromString("Moon"));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("Moon"));
    }

    @Test
    public void getHash160ByECPointKey() throws Throwable {
        Hash160 hash160 = new Hash160("a8387e42ccc4366121e519f6133090f0525e5ec6");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), hash160(hash160));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash256ByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), hash256(hash256));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getECPointByECPointKey() throws Throwable {
        ContractParameter value = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), value);

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getByteArrayByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL),
                byteArrayFromString("My name is Trinity."));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertThat(res.getStack().get(0).getString(), is("My name is Trinity."));
    }

    @Test
    public void getStringByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL),
                string("Trinity and Neo"));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("Trinity and Neo"));
    }

    @Test
    public void getBooleanByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), bool(true));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getIntByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), integer(777));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(777)));
    }

    @Test
    public void getIntOrZeroByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(ECPOINT_HEX_VAL), integer(10100));

        ContractParameter key = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(10100)));

        // Test that instructions return 0 if no value was found for the provided key.
        ECPublicKey keyNoValue = new ECPublicKey("0333af5eb246f3b853580e1ec889e7cf9e4f18390a9487cc330a44ba14b1c5350b");
        key = publicKey(keyNoValue);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.INTEGER));
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    // endregion
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
    public void putByteArrayKeyBooleanValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
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

    @Test
    public void putByteArrayKeyECPointValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
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
    public void putByteStringKeyBooleanValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
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

    @Test
    public void putByteStringKeyECPointValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
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
    public void putStringKeyBooleanValue() throws IOException {
        ContractParameter key = string("aa");
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
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

    @Test
    public void putStringKeyECPointValue() throws IOException {
        ContractParameter key = string("aa");
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
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
    public void putIntegerKeyBooleanValue() throws IOException {
        ContractParameter key = integer(513);
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
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

    @Test
    public void putIntegerKeyECPointValue() throws IOException {
        ContractParameter key = integer(143);
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
    // region put hash160 key

    @Test
    public void putHash160KeyByteArrayValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putHash160KeyByteStringValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putHash160KeyIntegerValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = integer(28);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(value.getValue()));
    }

    @Test
    public void putHash160KeyBooleanValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
    }

    @Test
    public void putHash160KeyStringValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = string("hello neow3j");
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello neow3j"));
    }

    @Test
    public void putHash160KeyHash160Value() throws IOException {
        Hash160 v = ct.getClient1().getScriptHash();
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putHash160KeyHash256Value() throws IOException {
        Hash256 v = ct.getBlockHashOfDeployTx();
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    @Test
    public void putHash160KeyECPointValue() throws IOException {
        ContractParameter key = hash160(KEY_HASH160);
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
    // region put hash256 key

    @Test
    public void putHash256KeyByteArrayValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putHash256KeyByteStringValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putHash256KeyIntegerValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = integer(28);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(value.getValue()));
    }

    @Test
    public void putHash256KeyBooleanValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
    }

    @Test
    public void putHash256KeyStringValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = string("hello neow3j");
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello neow3j"));
    }

    @Test
    public void putHash256KeyHash160Value() throws IOException {
        Hash160 v = ct.getClient1().getScriptHash();
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putHash256KeyHash256Value() throws IOException {
        Hash256 v = ct.getBlockHashOfDeployTx();
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    @Test
    public void putHash256KeyECPointValue() throws IOException {
        ContractParameter key = hash256(KEY_HASH256);
        ContractParameter value = publicKey(ECPOINT_VAL_2);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL_2));
    }

    // endregion
    // region put ecpoint key

    @Test
    public void putECPointKeyByteArrayValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putECPointKeyByteStringValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = byteArray(new byte[]{1, 2, 5, 6});
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(value.getValue()));
    }

    @Test
    public void putECPointKeyIntegerValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = integer(28);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(value.getValue()));
    }

    @Test
    public void putECPointKeyBooleanValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
    }

    @Test
    public void putECPointKeyStringValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = string("hello neow3j");
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is("hello neow3j"));
    }

    @Test
    public void putECPointKeyHash160Value() throws IOException {
        Hash160 v = ct.getClient1().getScriptHash();
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = hash160(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(v.toAddress()));
    }

    @Test
    public void putECPointKeyHash256Value() throws IOException {
        Hash256 v = ct.getBlockHashOfDeployTx();
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = hash256(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(v.toLittleEndianArray()));
    }

    @Test
    public void putECPointKeyECPointValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    // endregion
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

    @Test
    public void deleteByHash160() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArrayFromString("del by hash160"));

        InvocationResult res = ct.callInvokeFunction("getByHash160Key", hash160(KEY_HASH160)).getInvocationResult();

        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("del by hash160"));

        res = ct.callInvokeFunction(testName, hash160(KEY_HASH160)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)));
    }

    @Test
    public void deleteByHash256() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                byteArrayFromString("del by hash256"));

        InvocationResult res = ct.callInvokeFunction("getByHash256Key", hash256(KEY_HASH256)).getInvocationResult();

        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("del by hash256"));

        res = ct.callInvokeFunction(testName, hash256(KEY_HASH256)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());

        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)));
    }

    @Test
    public void deleteByECPoint() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), byteArrayFromString("del by point"));

        InvocationResult res = ct.callInvokeFunction("getByECPointKey", publicKey(KEY_ECPOINT)).getInvocationResult();

        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("del by point"));

        res = ct.callInvokeFunction(testName, publicKey(KEY_ECPOINT)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());
    }

    // endregion
    // region find

    @Test
    public void findWithRemovePrefixOption() throws Throwable {
        // Stored data from Hash160 and Hash256 conflict with this prefix. Make sure to get rid of this data beforehand.
        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)));
        ct.invokeFunctionAndAwaitExecution(REMOVE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)));

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

    // endregion

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

        // endregion
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

        // endregion
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

        public static ECPoint getECPointByByteStringKey(ByteString s) {
            return map.getECPoint(s);
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

        // endregion
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

        public static ECPoint getECPointByByteArrayKey(byte[] s) {
            return map.getECPoint(s);
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

        // endregion
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

        public static ECPoint getECPointByStringKey(String s) {
            return map.getECPoint(s);
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

        // endregion
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

        public static ECPoint getECPointByIntegerKey(int i) {
            return map.getECPoint(i);
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

        // endregion
        // region get hash160 key

        public static ByteString getByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.get(key);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getHash160(key);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getHash256(key);
        }

        public static ECPoint getECPointByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getECPoint(key);
        }

        public static byte[] getByteArrayByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getByteArray(key);
        }

        public static String getStringByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getString(key);
        }

        public static boolean getBooleanByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getBoolean(key);
        }

        public static int getIntByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getInt(key);
        }

        public static int getIntOrZeroByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return map.getIntOrZero(key);
        }

        // endregion
        // region get hash256 key

        public static ByteString getByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.get(key);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getHash160(key);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getHash256(key);
        }

        public static ECPoint getECPointByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getECPoint(key);
        }

        public static byte[] getByteArrayByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getByteArray(key);
        }

        public static String getStringByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getString(key);
        }

        public static boolean getBooleanByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getBoolean(key);
        }

        public static int getIntByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getInt(key);
        }

        public static int getIntOrZeroByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return map.getIntOrZero(key);
        }

        // endregion
        // region get ecpoint key

        public static ByteString getByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.get(key);
        }

        public static io.neow3j.devpack.Hash160 getHash160ByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getHash160(key);
        }

        public static io.neow3j.devpack.Hash256 getHash256ByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getHash256(key);
        }

        public static ECPoint getECPointByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getECPoint(key);
        }

        public static byte[] getByteArrayByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getByteArray(key);
        }

        public static String getStringByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getString(key);
        }

        public static boolean getBooleanByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getBoolean(key);
        }

        public static int getIntByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getInt(key);
        }

        public static int getIntOrZeroByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return map.getIntOrZero(key);
        }

        // endregion
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

        public static ByteString putByteArrayKeyBooleanValue(byte[] key, boolean value) {
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

        public static ByteString putByteArrayKeyECPointValue(byte[] key, ECPoint value) {
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion
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

        public static ByteString putByteStringKeyBooleanValue(ByteString key, boolean value) {
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

        public static ByteString putByteStringKeyECPointValue(ByteString key, ECPoint value) {
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion
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

        public static ByteString putStringKeyBooleanValue(String key, boolean value) {
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

        public static ByteString putStringKeyECPointValue(String key, ECPoint value) {
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion
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

        public static ByteString putIntegerKeyBooleanValue(int key, boolean value) {
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

        public static ByteString putIntegerKeyECPointValue(int key, ECPoint value) {
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion put integer key
        // region put hash160 key

        public static ByteString putHash160KeyByteArrayValue(io.neow3j.devpack.Hash160 key, byte[] value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyByteStringValue(io.neow3j.devpack.Hash160 key, ByteString value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyIntegerValue(io.neow3j.devpack.Hash160 key, int value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyBooleanValue(io.neow3j.devpack.Hash160 key, boolean value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyStringValue(io.neow3j.devpack.Hash160 key, String value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyHash160Value(io.neow3j.devpack.Hash160 key,
                io.neow3j.devpack.Hash160 value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putHash160KeyHash256Value(io.neow3j.devpack.Hash160 key,
                io.neow3j.devpack.Hash256 value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash160KeyECPointValue(io.neow3j.devpack.Hash160 key, ECPoint value) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        // endregion
        // region put hash256 key

        public static ByteString putHash256KeyByteArrayValue(io.neow3j.devpack.Hash256 key, byte[] value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyByteStringValue(io.neow3j.devpack.Hash256 key, ByteString value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyIntegerValue(io.neow3j.devpack.Hash256 key, int value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyBooleanValue(io.neow3j.devpack.Hash256 key, boolean value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyStringValue(io.neow3j.devpack.Hash256 key, String value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyHash160Value(io.neow3j.devpack.Hash256 key,
                io.neow3j.devpack.Hash160 value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putHash256KeyHash256Value(io.neow3j.devpack.Hash256 key,
                io.neow3j.devpack.Hash256 value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString putHash256KeyECPointValue(io.neow3j.devpack.Hash256 key, ECPoint value) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        // endregion
        // region put ecpoint key

        public static ByteString putECPointKeyByteArrayValue(ECPoint key, byte[] value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyByteStringValue(ECPoint key, ByteString value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyIntegerValue(ECPoint key, int value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyBooleanValue(ECPoint key, boolean value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyStringValue(ECPoint key, String value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyHash160Value(ECPoint key, io.neow3j.devpack.Hash160 value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteArray()));
        }

        public static ByteString putECPointKeyHash256Value(ECPoint key, io.neow3j.devpack.Hash256 value) {
            assert ECPoint.isValid(key);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key.toByteString()));
        }

        public static ByteString putECPointKeyECPointValue(ECPoint key, ECPoint value) {
            assert ECPoint.isValid(key);
            assert ECPoint.isValid(value);
            map.put(key, value);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion
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

        public static ByteString deleteByHash160(io.neow3j.devpack.Hash160 key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByHash256(io.neow3j.devpack.Hash256 key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        public static ByteString deleteByECPoint(ECPoint key) {
            map.delete(key);
            return Storage.get(ctx, prefix.concat(key));
        }

        // endregion
        // region find

        public static Iterator findWithRemovePrefixOption() {
            return (Iterator<Iterator.Struct<ByteString, ByteString>>) map.find(FindOptions.RemovePrefix);
        }

        // endregion

    }

}
