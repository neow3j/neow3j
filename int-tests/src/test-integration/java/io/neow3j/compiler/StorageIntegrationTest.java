package io.neow3j.compiler;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.ECPoint;
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
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static final String KEY_HASH160_HEX = "f68f181731a47036a99f04dad90043a744edec0f";
    private static final Hash160 KEY_HASH160 = new Hash160(KEY_HASH160_HEX);

    private static final String KEY_HASH256_HEX = "257d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd";
    private static final Hash256 KEY_HASH256 = new Hash256(KEY_HASH256_HEX);

    private static final String KEY_ECPOINT_HEX = "03afcc76bedc01df1d28f95f4e173fdb1d7271c974a46e7cf277e7776737bebdf2";
    private static final ECPublicKey KEY_ECPOINT = new ECPublicKey(hexStringToByteArray(KEY_ECPOINT_HEX));

    private static final String ECPOINT_HEX_VAL = "02e986f4c8e007554e0c8d38861c273cc1a20eaf828417221fab6e4be9060ffcb7";
    private static final ECPublicKey ECPOINT_VAL = new ECPublicKey(hexStringToByteArray(ECPOINT_HEX_VAL));

    private static final String ECPOINT_HEX_VAL_2 =
            "0232e49fe7e4beea4b38d5cb71270ff63617aa7b59d071d26d37e5888bf23e2f51";
    private static final ECPublicKey ECPOINT_VAL_2 = new ECPublicKey(hexStringToByteArray(ECPOINT_HEX_VAL_2));

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
        ct.invokeFunction(STORE_DATA, key, data);

        key = byteArray(KEY3_HEX);
        data = integer(INTEGER3);
        ct.invokeFunction(STORE_INT, key, data);

        key = string(KEY4_STRING);
        data = integer(INTEGER4);
        ct.invokeFunction(STORE_INT, key, data);

        key = integer(KEY5_INT);
        data = byteArrayFromString(DATA5);
        ct.invokeFunction(STORE_DATA, key, data);

        key = byteArray(KEY6_HEX);
        data = bool(BOOLEAN_6);
        ct.invokeFunction(STORE_DATA, key, data);

        key = byteArray(KEY7_HEX);
        data = bool(BOOLEAN_7);
        ct.invokeFunction(STORE_DATA, key, data);

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

    @Test
    public void getByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArrayFromString("hello bongo cat!"));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("hello bongo cat!"));
    }

    @Test
    public void getByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                byteArrayFromString("hello neow3j, you're awesome!"));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("hello neow3j, you're awesome!"));
    }

    @Test
    public void getByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), byteArrayFromString("ec point"));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("ec point"));
    }

    // endregion
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

    @Test
    public void getHash160ByHash160Key() throws Throwable {
        Hash160 hash160 = new Hash160("bcf358d5d5e8d6ef0d1c822aa30cf7e670cf5ee2");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), hash160(hash160));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash160ByHash256Key() throws Throwable {
        Hash160 hash160 = new Hash160("bcf358d5d5e8d6ef0d1c822aa30cf7e670cf5ee3");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), hash160(hash160));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    @Test
    public void getHash160ByECPointKey() throws Throwable {
        Hash160 hash160 = new Hash160("bcf358d5d5e8d6ef0d1c822aa30cf7e670cf664d");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), hash160(hash160));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getAddress(), is(hash160.toAddress()));
    }

    // endregion
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

    @Test
    public void getHash256ByHash160Key() throws Throwable {
        Hash256 hash256 = new Hash256("0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29536b899");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArray(hash256.toLittleEndianArray()));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(reverseArray(hash256.toArray())));
    }

    @Test
    public void getHash256ByHash256Key() throws Throwable {
        Hash256 hash256 = new Hash256("0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29536b899");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), hash256(hash256));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertArrayEquals(res.getStack().get(0).getByteArray(), hash256.toLittleEndianArray());
    }

    @Test
    public void getHash256ByECPointKey() throws Throwable {
        Hash256 hash256 = new Hash256("0x3d1e051247f246f60dd2ba4f90f799578b5d394157b1f2b012c016b29535b52c");
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), hash256(hash256));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertArrayEquals(res.getStack().get(0).getByteArray(), hash256.toLittleEndianArray());
    }

    // endregion
    // region getECPoint

    @Test
    public void getECPointByByteArrayKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getECPointByByteStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getECPointByStringKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getECPointByIntegerKey() throws Throwable {
        ContractParameter key = byteArrayFromString(testName);
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, key, data);

        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
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
    public void getECPointByHash256Key() throws Throwable {
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), data);

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    @Test
    public void getECPointByECPointKey() throws Throwable {
        ContractParameter data = publicKey(ECPOINT_VAL);
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), data);

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getHexString(), is(ECPOINT_HEX_VAL));
    }

    // endregion
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

    @Test
    public void getByteArrayByHash160Key() throws Throwable {
        byte[] bytes = {0xa, 0xe};
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), byteArray(bytes));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertArrayEquals(res.getStack().get(0).getByteArray(), bytes);
    }

    @Test
    public void getByteArrayByHash256Key() throws Throwable {
        byte[] bytes = {0xa, 0xe};
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), byteArray(bytes));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertArrayEquals(res.getStack().get(0).getByteArray(), bytes);
    }

    @Test
    public void getByteArrayByECPointKey() throws Throwable {
        byte[] bytes = {0x4, 0xd};
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), byteArray(bytes));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BUFFER));
        assertArrayEquals(res.getStack().get(0).getByteArray(), bytes);
    }

    // endregion
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

    @Test
    public void getStringByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                string("hello there"));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("hello there"));
    }

    @Test
    public void getStringByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                string("hello there!"));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("hello there!"));
    }

    @Test
    public void getStringByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), string("hello trinity!"));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getString(), is("hello trinity!"));
    }

    // endregion
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

    @Test
    public void getBooleanByHash160Key() throws Throwable {
        boolean bool = true;
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), bool(bool));

        ContractParameter key = hash160(KEY_HASH160);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getBooleanByHash256Key() throws Throwable {
        boolean bool = true;
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), bool(bool));

        ContractParameter key = hash256(KEY_HASH256);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertThat(res.getStack().get(0).getBoolean(), is(true));
    }

    @Test
    public void getBooleanByECPointKey() throws Throwable {
        boolean bool = false;
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), bool(bool));

        ContractParameter key = publicKey(KEY_ECPOINT);
        InvocationResult res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BOOLEAN));
        assertFalse(res.getStack().get(0).getBoolean());
    }

    // endregion
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

    @Test
    public void getIntByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), integer(200));

        InvocationResult res = ct.callInvokeFunction(testName, hash160(KEY_HASH160)).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.INTEGER));
        assertThat(res.getFirstStackItem().getInteger(), is(BigInteger.valueOf(200)));
    }

    @Test
    public void getIntOrZeroByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)), integer(333));

        InvocationResult res = ct.callInvokeFunction(testName, hash160(KEY_HASH160)).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(333)));

        // Test that instructions return 0 if no value was found for the provided key.
        ContractParameter key = hash160(new Hash160("0x6a3828e0378f9f331c69476f016fe91f5bba8dbd"));
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getIntByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), integer(111));

        InvocationResult res = ct.callInvokeFunction(testName, hash256(KEY_HASH256)).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.INTEGER));
        assertThat(res.getFirstStackItem().getInteger(), is(BigInteger.valueOf(111)));
    }

    @Test
    public void getIntOrZeroByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)), integer(555));

        InvocationResult res = ct.callInvokeFunction(testName, hash256(KEY_HASH256)).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(555)));

        // Test that instructions return 0 if no value was found for the provided key.
        Hash256 keyNoValue = new Hash256("0xb804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        ContractParameter key = hash256(keyNoValue);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.ZERO));
    }

    @Test
    public void getIntByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), integer(357));

        InvocationResult res = ct.callInvokeFunction(testName, publicKey(KEY_ECPOINT)).getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.INTEGER));
        assertThat(res.getFirstStackItem().getInteger(), is(BigInteger.valueOf(357)));
    }

    @Test
    public void getIntOrZeroByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX), integer(579));

        InvocationResult res = ct.callInvokeFunction(testName, publicKey(KEY_ECPOINT)).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(BigInteger.valueOf(579)));

        // Test that instructions return 0 if no value was found for the provided key.
        ECPublicKey keyNoValue = new ECPublicKey("0333af5eb246f3b853580e1ec889e7cf9e4f18390a9487cc330a44ba14b1c5350b");
        ContractParameter key = publicKey(keyNoValue);
        res = ct.callInvokeFunction(testName, key).getInvocationResult();
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
    public void putByteArrayKeyBooleanValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
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
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
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
    public void putByteStringKeyBooleanValue() throws IOException {
        ContractParameter key = byteArray("02");
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
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
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
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
    public void putStringKeyBooleanValue() throws IOException {
        ContractParameter key = string("key");
        ContractParameter value = bool(true);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x01}));
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

    @Test
    public void putStringKeyECPointValue() throws IOException {
        ContractParameter key = string("key");
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
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
    public void putIntegerKeyBooleanValue() throws IOException {
        ContractParameter key = integer(136);
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
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

    @Test
    public void putIntegerKeyECPointValue() throws IOException {
        ContractParameter key = integer(140);
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
    // region put hash160 key

    @Test
    public void putHash160KeyByteArrayValue() throws IOException {
        String v = "moooon";
        Hash160 scriptHash = new Hash160("606e6a4a3772eb178d341133fe17f71cd60ba4b2");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = byteArrayFromString(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putHash160KeyByteStringValue() throws IOException {
        String s = "hello there";
        Hash160 scriptHash = new Hash160("56dd2a8942506015369a2919eed12f6c76d52aa9");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = byteArrayFromString(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putHash160KeyStringValue() throws IOException {
        String s = "wow";
        Hash160 scriptHash = new Hash160("7b7ac2887bb643c28a2778bfd73ab94a496cfb36");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = string(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putHash160KeyIntegerValue() throws IOException {
        BigInteger i = BigInteger.valueOf(144);
        Hash160 scriptHash = new Hash160("3a0fac5f669d100de5ee6e2a0168bbe1c909d7ac");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = integer(i);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(i));
    }

    @Test
    public void putHash160KeyBooleanValue() throws IOException {
        Hash160 scriptHash = new Hash160("cd6560ebb044dcb2c9663b51b31e60fc8119760e");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
    }

    @Test
    public void putHash160KeyHash160Value() throws IOException {
        Hash160 scriptHash = ct.getClient1().getScriptHash();
        Hash160 scriptHashKey = new Hash160("867f6c34acd21a6c7a76d30218981abcb9400a4a");
        ContractParameter key = hash160(scriptHashKey);
        ContractParameter value = hash160(scriptHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(scriptHash.toAddress()));
    }

    @Test
    public void putHash160KeyHash256Value() throws IOException {
        Hash256 txHash = ct.getDeployTxHash();
        Hash160 scriptHash = new Hash160("33716e70a3019234b5e2ee70f56c1e7375423cd2");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = hash256(txHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(txHash.toLittleEndianArray()));
    }

    @Test
    public void putHash160KeyECPointValue() throws IOException {
        Hash160 scriptHash = new Hash160("33716e70a3019234b5e2ee70f56c1e7375423cd2");
        ContractParameter key = hash160(scriptHash);
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
    // region put hash256 key

    @Test
    public void putHash256KeyByteArrayValue() throws IOException {
        String v = "moooon";
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = byteArrayFromString(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(v));
    }

    @Test
    public void putHash256KeyByteStringValue() throws IOException {
        String s = "hello there";
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = byteArrayFromString(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putHash256KeyStringValue() throws IOException {
        String s = "wow";
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = string(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putHash256KeyIntegerValue() throws IOException {
        BigInteger i = BigInteger.valueOf(144);
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = integer(i);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getInteger(), is(i));
    }

    @Test
    public void putHash256KeyBooleanValue() throws IOException {
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
    }

    @Test
    public void putHash256KeyHash160Value() throws IOException {
        Hash160 scriptHash = ct.getClient1().getScriptHash();
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = hash160(scriptHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(scriptHash.toAddress()));
    }

    @Test
    public void putHash256KeyHash256Value() throws IOException {
        Hash256 txHash = ct.getDeployTxHash();
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = hash256(txHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(txHash.toLittleEndianArray()));
    }

    @Test
    public void putHash256KeyECPointValue() throws IOException {
        Hash256 hash256 = new Hash256("c702c345d7b2878a98359c2fac09fea222af5769e100e0462b4bf1b6fcca0ee2");
        ContractParameter key = hash256(hash256);
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
    // region put ecpoint key

    @Test
    public void putECPointKeyByteArrayValue() throws IOException {
        String v = "hi";
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = byteArrayFromString(v);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getString(), is(v));
    }

    @Test
    public void putECPointKeyByteStringValue() throws IOException {
        String s = "hello there";
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = byteArrayFromString(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getString(), is(s));
    }

    @Test
    public void putECPointKeyStringValue() throws IOException {
        String s = "wowwow";
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = string(s);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getString(), is(s));
    }

    @Test
    public void putECPointKeyIntegerValue() throws IOException {
        BigInteger i = BigInteger.valueOf(144);
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = integer(i);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getFirstStackItem().getInteger(), is(i));
    }

    @Test
    public void putECPointKeyBooleanValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = bool(false);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(new byte[]{0x00}));
    }

    @Test
    public void putECPointKeyHash160Value() throws IOException {
        Hash160 scriptHash = ct.getClient1().getScriptHash();
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = hash160(scriptHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getAddress(), is(scriptHash.toAddress()));
    }

    @Test
    public void putECPointKeyHash256Value() throws IOException {
        Hash256 txHash = ct.getDeployTxHash();
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = hash256(txHash);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(txHash.toLittleEndianArray()));
    }

    @Test
    public void putECPointKeyECPointValue() throws IOException {
        ContractParameter key = publicKey(ECPOINT_VAL_2);
        ContractParameter value = publicKey(ECPOINT_VAL);
        InvocationResult res = ct.callInvokeFunction(testName, key, value).getInvocationResult();
        assertThat(res.getStack().get(0).getByteArray(), is(hexStringToByteArray(ECPOINT_HEX_VAL)));
    }

    // endregion
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


    @Test
    public void deleteByHash160Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH160_HEX)),
                byteArrayFromString("delete by hash160"));

        InvocationResult res = ct.callInvokeFunction("getByHash160Key", hash160(KEY_HASH160))
                .getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("delete by hash160"));

        res = ct.callInvokeFunction(testName, hash160(KEY_HASH160)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());
    }

    @Test
    public void deleteByHash256Key() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(reverseHexString(KEY_HASH256_HEX)),
                byteArrayFromString("delete by hash256"));

        InvocationResult res = ct.callInvokeFunction("getByHash256Key", hash256(KEY_HASH256))
                .getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("delete by hash256"));

        res = ct.callInvokeFunction(testName, hash256(KEY_HASH256)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());
    }

    @Test
    public void deleteByECPointKey() throws Throwable {
        ct.invokeFunctionAndAwaitExecution(STORE_DATA, byteArray(KEY_ECPOINT_HEX),
                byteArrayFromString("delete by ecpoint"));

        InvocationResult res = ct.callInvokeFunction("getByECPointKey", publicKey(KEY_ECPOINT))
                .getInvocationResult();
        assertThat(res.getFirstStackItem().getType(), is(StackItemType.BYTE_STRING));
        assertThat(res.getFirstStackItem().getString(), is("delete by ecpoint"));

        res = ct.callInvokeFunction(testName, publicKey(KEY_ECPOINT)).getInvocationResult();
        assertNull(res.getFirstStackItem().getValue());
    }

    // endregion
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

    @Test
    public void findBackwards() throws IOException {
        List<StackItem> forwardsIteratorList = ct.callAndTraverseIterator("findFindOptionsNone");
        List<StackItem> backwardsIteratorList = ct.callAndTraverseIterator(testName);

        assertThat(forwardsIteratorList, hasSize(3));
        assertThat(backwardsIteratorList, hasSize(3));
        assertThat(forwardsIteratorList.get(0), is(backwardsIteratorList.get(2)));
        assertThat(forwardsIteratorList.get(1), is(backwardsIteratorList.get(1)));
        assertThat(forwardsIteratorList.get(2), is(backwardsIteratorList.get(0)));
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

        // endregion
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

        public static ByteString getByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.get(ctx, key);
        }

        public static ByteString getByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.get(ctx, key);
        }

        public static ByteString getByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.get(ctx, key);
        }

        // endregion
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

        public static io.neow3j.devpack.Hash160 getHash160ByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash160 getHash160ByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash160 getHash160ByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            io.neow3j.devpack.Hash160 hash = Storage.getHash160(ctx, key);
            assert io.neow3j.devpack.Hash160.isValid(hash);
            return hash;
        }

        // endregion
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

        public static io.neow3j.devpack.Hash256 getHash256ByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash256 getHash256ByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        public static io.neow3j.devpack.Hash256 getHash256ByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            io.neow3j.devpack.Hash256 hash = Storage.getHash256(ctx, key);
            assert io.neow3j.devpack.Hash256.isValid(hash);
            return hash;
        }

        // endregion
        // region getECPoint

        public static ECPoint getECPointByByteArrayKey(byte[] key) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByByteStringKey(ByteString key) {
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByStringKey(String key) {
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByIntegerKey(int key) {
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        public static ECPoint getECPointByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            ECPoint point = Storage.getECPoint(ctx, key);
            assert ECPoint.isValid(point);
            return point;
        }

        // endregion
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

        public static byte[] getByteArrayByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.getByteArray(ctx, key);
        }

        public static byte[] getByteArrayByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.getByteArray(ctx, key);
        }

        // endregion
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

        public static String getStringByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.getString(ctx, key);
        }

        public static String getStringByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.getString(ctx, key);
        }

        public static String getStringByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.getString(ctx, key);
        }

        // endregion
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

        public static boolean getBooleanByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.getBoolean(ctx, key);
        }

        public static boolean getBooleanByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.getBoolean(ctx, key);
        }

        public static boolean getBooleanByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.getBoolean(ctx, key);
        }

        // endregion
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

        public static int getIntByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByHash160Key(io.neow3j.devpack.Hash160 key) {
            assert io.neow3j.devpack.Hash160.isValid(key);
            return Storage.getIntOrZero(ctx, key);
        }

        public static int getIntByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByHash256Key(io.neow3j.devpack.Hash256 key) {
            assert io.neow3j.devpack.Hash256.isValid(key);
            return Storage.getIntOrZero(ctx, key);
        }

        public static int getIntByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.getInt(ctx, key);
        }

        public static int getIntOrZeroByECPointKey(ECPoint key) {
            assert ECPoint.isValid(key);
            return Storage.getIntOrZero(ctx, key);
        }

        // endregion
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

        public static ByteString putByteArrayKeyBooleanValue(byte[] key, boolean value) {
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

        public static ByteString putByteArrayKeyECPointValue(byte[] key, ECPoint value) {
            byte[] k = new ByteString(key).toByteArray();
            assert k instanceof byte[];
            assert ECPoint.isValid(value);
            Storage.put(ctx, k, value);
            return Storage.get(ctx, k);
        }

        // endregion
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

        public static ByteString putByteStringKeyBooleanValue(ByteString key, boolean value) {
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

        public static ByteString putByteStringKeyECPointValue(ByteString key, ECPoint value) {
            Storage.put(ctx, key, value);
            assert ECPoint.isValid(value);
            return Storage.get(ctx, key);
        }

        // endregion
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

        public static ByteString putStringKeyBooleanValue(String key, boolean value) {
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

        public static ByteString putStringKeyECPointValue(String key, ECPoint value) {
            Storage.put(ctx, key, value);
            assert ECPoint.isValid(value);
            return Storage.get(ctx, key);
        }

        // endregion
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

        public static ByteString putIntegerKeyBooleanValue(int key, boolean value) {
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

        public static ByteString putIntegerKeyECPointValue(int key, ECPoint value) {
            assert ECPoint.isValid(value);
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key);
        }

        // endregion
        // region put hash160 key

        public static ByteString putHash160KeyByteArrayValue(io.neow3j.devpack.Hash160 key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyByteStringValue(io.neow3j.devpack.Hash160 key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyStringValue(io.neow3j.devpack.Hash160 key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyIntegerValue(io.neow3j.devpack.Hash160 key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyBooleanValue(io.neow3j.devpack.Hash160 key, boolean value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyHash160Value(io.neow3j.devpack.Hash160 key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyHash256Value(io.neow3j.devpack.Hash160 key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash160KeyECPointValue(io.neow3j.devpack.Hash160 key, ECPoint value) {
            Storage.put(ctx, key, value);
            assert ECPoint.isValid(value);
            return Storage.get(ctx, key.toByteString());
        }

        // endregion
        // region put hash256 key

        public static ByteString putHash256KeyByteArrayValue(io.neow3j.devpack.Hash256 key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyByteStringValue(io.neow3j.devpack.Hash256 key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyStringValue(io.neow3j.devpack.Hash256 key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyIntegerValue(io.neow3j.devpack.Hash256 key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyBooleanValue(io.neow3j.devpack.Hash256 key, boolean value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyHash160Value(io.neow3j.devpack.Hash256 key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyHash256Value(io.neow3j.devpack.Hash256 key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putHash256KeyECPointValue(io.neow3j.devpack.Hash256 key, ECPoint value) {
            assert ECPoint.isValid(value);
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        // endregion
        // region put ecpoint key

        public static ByteString putECPointKeyByteArrayValue(ECPoint key, byte[] value) {
            byte[] v = new ByteString(value).toByteArray();
            assert v instanceof byte[];
            Storage.put(ctx, key, v);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyByteStringValue(ECPoint key, ByteString value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyStringValue(ECPoint key, String value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyIntegerValue(ECPoint key, int value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyBooleanValue(ECPoint key, boolean value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyHash160Value(ECPoint key,
                io.neow3j.devpack.Hash160 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyHash256Value(ECPoint key,
                io.neow3j.devpack.Hash256 value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        public static ByteString putECPointKeyECPointValue(ECPoint key, ECPoint value) {
            Storage.put(ctx, key, value);
            return Storage.get(ctx, key.toByteString());
        }

        // endregion
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

        public static ByteString deleteByHash160Key(io.neow3j.devpack.Hash160 key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByHash256Key(io.neow3j.devpack.Hash256 key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        public static ByteString deleteByECPointKey(ECPoint key) {
            Storage.delete(ctx, key);
            return Storage.get(ctx, key);
        }

        // endregion delete
        // region find

        public static Iterator.Struct<ByteString, ByteString> findByByteStringPrefix(ByteString prefix) {
            Iterator<Iterator.Struct<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Iterator.Struct<ByteString, ByteString> findByByteArrayPrefix(byte[] prefix) {
            byte[] p = new ByteString(prefix).toByteArray();
            assert p instanceof byte[];
            Iterator<Iterator.Struct<ByteString, ByteString>> it = Storage.find(ctx, p, FindOptions.None);
            it.next();
            return it.get();
        }

        public static Iterator.Struct<ByteString, ByteString> findByStringPrefix(String prefix) {
            Iterator<Iterator.Struct<ByteString, ByteString>> it = Storage.find(ctx, prefix,
                    FindOptions.None);
            it.next();
            return it.get();
        }

        public static Iterator.Struct<ByteString, ByteString> findByIntegerPrefix(int prefix) {
            Iterator<Iterator.Struct<ByteString, ByteString>> it = Storage.find(ctx, prefix,
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
            int findOption = FindOptions.DeserializeValues & FindOptions.PickField0;
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

        // endregion
        // region find backwards

        public static Iterator<Iterator.Struct<ByteString, ByteString>> findFindOptionsNone() {
            prepareStorageForFindBackwards();
            return Storage.find(ctx, hexToBytes("ff"), FindOptions.None);
        }

        public static Iterator<Iterator.Struct<ByteString, ByteString>> findBackwards() {
            prepareStorageForFindBackwards();
            return Storage.find(ctx, hexToBytes("ff"), FindOptions.Backwards);
        }

        private static void prepareStorageForFindBackwards() {
            Storage.put(ctx, hexToBytes("ff00"), hexToBytes("112233"));
            Storage.put(ctx, hexToBytes("ff01"), hexToBytes("445566"));
            Storage.put(ctx, hexToBytes("ff02"), hexToBytes("778899"));
        }

        // endregion

    }

}
