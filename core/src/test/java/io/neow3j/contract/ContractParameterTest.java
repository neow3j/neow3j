package io.neow3j.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.map;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.signature;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class ContractParameterTest {

    private ContractParameter contractParameter;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        this.contractParameter = string("value");
    }

    @Test
    public void testStringParamCreation() {
        String value = "value";
        ContractParameter p = string(value);
        assertEquals(value, p.getValue());
        assertEquals(ContractParameterType.STRING, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreation() {
        byte[] bytes = new byte[]{0x01, 0x01};
        ContractParameter p = byteArray(bytes);
        assertThat((byte[]) p.getValue(), is(bytes));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreationFromHexString() {
        ContractParameter p = byteArray("0xa602");
        assertThat((byte[]) p.getValue(), is(new byte[]{(byte) 0xa6, 0x02}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test
    public void testByteArrayParam_equals() {
        ContractParameter fromHex = byteArray(hexStringToByteArray("0x796573"));
        ContractParameter fromByteArray = byteArray(new byte[]{0x79, 0x65, 0x73});
        assertThat(fromHex, is(fromByteArray));
    }

    @Test
    public void testByteArrayParamCreationFromString() {
        ContractParameter p = byteArrayFromString("Neo");
        assertThat(((byte[]) p.getValue()), is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreationFromInvalidHexString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Argument is not a valid hex number.");
        String value = "value";
        byteArray(value);
    }

    @Test
    public void testArrayParamCreationFromList() {
        List<ContractParameter> params = new ArrayList<>();
        ContractParameter p1 = string("value");
        ContractParameter p2 = byteArray("0x0101");
        params.add(p1);
        params.add(p2);
        ContractParameter p = array(params);

        assertEquals(ContractParameterType.ARRAY, p.getParamType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(p1, ((ContractParameter[]) p.getValue())[0]);
        assertEquals(p2, ((ContractParameter[]) p.getValue())[1]);
    }

    @Test
    public void testArrayParamCreationFromArray() {
        ContractParameter p1 = string("value");
        ContractParameter p2 = byteArray("0x0101");
        ContractParameter p = array(p1, p2);

        assertEquals(ContractParameterType.ARRAY, p.getParamType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(p1, ((ContractParameter[]) p.getValue())[0]);
        assertEquals(p2, ((ContractParameter[]) p.getValue())[1]);
    }

    @Test
    public void testArrayParamCreationFromObjects() {
        int i = 1;
        long l = 2000000000000000000L;
        boolean b = true;
        String s = "testString";
        byte[] bytes = new byte[]{5, 8};
        Hash160 contractHash = new Hash160("0xa2b524b68dfe43a9d56af84f443c6b9843b8028c");
        Hash256 txHash =
                new Hash256("257d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd");
        BigInteger bigInt = BigInteger.valueOf(l);
        Account a = Account.create();

        ContractParameter expected = array(Arrays.asList(
                integer(1), integer(bigInt) /* the long */, bool(b), string(s), byteArray(bytes),
                hash160(contractHash), hash256(txHash), integer(bigInt), hash160(a)));

        ContractParameter arrayFromObjects = array(i, l, b, s, bytes, contractHash, txHash, bigInt,
                a);

        assertThat(arrayFromObjects, is(expected));
    }

    @Test
    public void testArrayParamCreationFromObjects_failUnSupportedObject() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage(
                "provided object could not be casted into a supported contract");
        array(new Object());
    }

    @Test
    public void testArrayParam_NullObject() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Cannot add a null object to an array contract parameter");
        array((Object) null);
    }

    @Test
    public void testArrayParam_NullContractParameter() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Cannot add a null object to an array contract parameter");
        array((ContractParameter) null);
    }

    @Test
    public void testArrayParam_Empty() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("At least one parameter is required");
        array();
    }

    @Test
    public void testSignatureParamCreationFromValidString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature(sig);

        assertArrayEquals(hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromValidStringStrip0x() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature("0x" + sig);

        assertArrayEquals(hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromByteArray() {
        String sigString = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b" +
                "6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        byte[] sig = hexStringToByteArray(sigString);
        ContractParameter p = signature(sig);

        assertArrayEquals(sig, (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromSignatureData() {
        Sign.SignatureData signatureData = Sign.SignatureData.fromByteArray(
                hexStringToByteArray(
                        "598235b9c5495cced03e41c0e4e0f7c4e3b8df3a190d33a76d764c5a6eb7581e8875976f63c1848cccc0822d8b8a534537da56a9b41f5e03977f83aae33d3558"));

        ContractParameter p = signature(signatureData);
        assertArrayEquals(signatureData.getConcatenated(), (byte[]) p.getValue());
    }

    @Test
    public void testSignatureParamCreationFromTooShortString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Signature is expected to have a length of 64 bytes, but " +
                "had 63.");
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f2";
        signature(sig);
    }

    @Test
    public void testSignatureParamCreationFromTooLongString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Signature is expected to have a length of 64 bytes, but " +
                "had 65.");
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213ff";
        ContractParameter.signature(sig);
    }

    @Test
    public void testSignatureParamCreationFromNoHexString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Argument is not a valid hex number.");
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585t2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        signature(sig);
    }

    @Test
    public void testBooleanParameterCreation() {
        ContractParameter p = bool(false);

        assertEquals(ContractParameterType.BOOLEAN, p.getParamType());
        assertEquals(false, p.getValue());

        p = bool(true);

        assertEquals(ContractParameterType.BOOLEAN, p.getParamType());
        assertEquals(true, p.getValue());
    }

    @Test
    public void testIntegerParameterCreation() {
        ContractParameter p = integer(10);

        assertEquals(ContractParameterType.INTEGER, p.getParamType());
        assertEquals(BigInteger.TEN, p.getValue());

        p = integer(BigInteger.ONE.negate());

        assertEquals(ContractParameterType.INTEGER, p.getParamType());
        assertEquals(BigInteger.ONE.negate(), p.getValue());
    }

    @Test
    public void testHash160ParameterCreationFromValidScriptHash() {
        String hashString = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        Hash160 hash = new Hash160(hashString);
        ContractParameter p = hash160(hash);

        assertEquals(ContractParameterType.HASH160, p.getParamType());
        assertEquals(hashString, p.getValue().toString());
    }

    @Test
    public void testHash160ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        ContractParameter p = hash160(new Hash160(hashValue));

        assertEquals(ContractParameterType.HASH160, p.getParamType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash160ParameterCreationFromAccount() {
        Account account = Account.create();
        ContractParameter p = hash160(account);

        assertThat(p.getValue(), is(account.getScriptHash()));
    }

    @Test
    public void testHash160_null() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The script hash argument must not be null");
        hash160((Hash160) null);
    }

    @Test
    public void testHash256() {
        Hash256 hash =
                new Hash256("576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf");
        ContractParameter p = hash256(hash);

        assertThat(p.getParamType(), is(ContractParameterType.HASH256));
        assertThat(p.getValue(), is(hash));
    }

    @Test
    public void testHash256ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(hashValue);

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash256ParameterCreationFromValidByteArray() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(hexStringToByteArray(hashValue));

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash256ParamCreationFromTooShortString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be 32 bytes but was 31 bytes.");
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6";
        hash256(sig);
    }

    @Test
    public void testHash256ParamCreationFromTooLongString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be 32 bytes but was 33 bytes.");
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cfaa";
        hash256(sig);
    }

    @Test
    public void testHash256ParamCreationFromNoHexString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("is not a valid hex number");
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cg";
        hash256(sig);
    }

    @Test
    public void testPublicKeyParamCreationFromByteArray() {
        byte[] pubKey = hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816");
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test
    public void testPublicKeyParamCreationFromInvalidByteArray() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be 33 bytes but was 32 bytes.");
        // one byte too short
        byte[] pubKey = hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");
        publicKey(pubKey);
    }

    @Test
    public void testPublicKeyParamCreationFromHexString() {
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test
    public void testPublicKeyParamCreationFromInvalidHexString() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("must be 33 bytes but was 32 bytes.");
        // one byte too short.
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        publicKey(pubKey);
    }

    @Test
    public void testMap() {
        Map<ContractParameter, ContractParameter> map = new HashMap<>();
        map.put(integer(1), string("first"));
        map.put(integer(2), string("second"));
        ContractParameter param = map(map);
        assertThat(param.getValue(), is(map));
    }

    @Test
    public void testMap_withObjects() {
        Map<Object, Object> map = new HashMap<>();
        map.put("one", "first");
        map.put("two", 2);
        ContractParameter param = map(map);
        Map<?, ?> value = (Map<?, ?>) param.getValue();
        assertThat(value.keySet(), containsInAnyOrder(string("one"), string("two")));
        assertThat(value.values(), containsInAnyOrder(string("first"), integer(2)));
    }

    @Test
    public void testMap_invalidKeyType() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("The provided map contains an invalid key.");

        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();
        map.put(array(integer(1), string("test")), string("first"));
        map(map);
    }

    @Test
    public void testMap_empty() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("At least one map entry is required to create a map " +
                "contract parameter.");

        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();
        map(map);
    }

    @Test
    public void testGetParamType() {
        assertThat(contractParameter.getParamType(), is(ContractParameterType.STRING));
    }

    @Test
    public void testGetValue() {
        assertThat(contractParameter.getValue(), is("value"));
    }

    @Test
    public void testEquals() {
        assertThat(contractParameter.equals("o"), is(false));
        assertThat(contractParameter.equals(string("value")), is(true));
        assertNotEquals(contractParameter, string("test"));
        assertNotEquals(contractParameter, integer(1));
    }

    @Test
    public void testHashCode() {
        int result = contractParameter.hashCode();
        assertNotEquals(0, result);
    }

    @Test
    public void deserializeWithAllTypesAndBuildScriptWithParameter() throws IOException {
        String json = "{\n" +
                "    \"type\":\"Array\",\n" +
                "    \"value\": [\n" +
                "        {\n" +
                "            \"type\":\"Integer\",\n" +
                "            \"value\":1000\n" + // integer without quotes
                "        },\n" +
                "        {\n" +
                "            \"type\":\"Integer\",\n" +
                "            \"value\":\"1000\"\n" + // integer with quotes
                "        },\n" +
                "        {\n" +
                "            \"type\":\"Array\",\n" +
                "            \"value\":[\n" +
                "                {\n" +
                "                    \"type\":\"String\",\n" +
                "                    \"value\":\"hello, world!\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"ByteArray\",\n" +
                "                    \"value\":\"AQID\"\n" + // 010203 Base64
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Signature\",\n" +
                "                    \"value\":\"AQID\"\n" + // 010203 Base64
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"PublicKey\",\n" +
                "                    \"value\":\"010203\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Boolean\",\n" +
                "                    \"value\":true\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Boolean\",\n" +
                "                    \"value\":\"true\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Hash160\",\n" +
                "                    \"value\":\"69ecca587293047be4c59159bf8bc399985c160d\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Hash256\",\n" +
                "                    " +
                "\"value\":\"fe26f525c17b58f63a4d106fba973ec34cc99bfe2501c9f672cc145b483e398b\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\":\"Any\",\n" +
                "                    \"value\":\"\"\n" +
                "                },\n" +
                "               {\n" +
                "                   \"type\": \"Map\",\n" +
                "                   \"value\": [\n" +
                "                   {\n" +
                "                       \"key\": \n" +
                "                       {\n" +
                "                           \"type\": \"Integer\",\n" +
                "                           \"value\": \"5\"\n" +
                "                       },\n" +
                "                       \"value\": \n" +
                "                       {\n" +
                "                           \"type\": \"String\",\n" +
                "                           \"value\": \"value\"\n" +
                "                       }\n" +
                "                   },\n" +
                "                   {\n" +
                "                       \"key\": \n" +
                "                       {\n" +
                "                           \"type\": \"ByteArray\",\n" +
                "                           \"value\":\"AQID\"\n" + // 010203 Base64
                "                       },\n" +
                "                       \"value\": \n" +
                "                       {\n" +
                "                           \"type\": \"Integer\",\n" +
                "                           \"value\": \"5\"\n" +
                "                       }\n" +
                "                   }\n" +
                "               ]\n" +
                "               }" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        ObjectMapper m = ObjectMapperFactory.getObjectMapper();
        ContractParameter p = m.readValue(json, ContractParameter.class);

        ContractParameter[] arr = (ContractParameter[]) p.getValue();
        assertThat((BigInteger) arr[0].getValue(), is(new BigInteger("1000")));
        assertThat((BigInteger) arr[1].getValue(), is(new BigInteger("1000")));
        ContractParameter[] arr2 = (ContractParameter[]) arr[2].getValue();
        assertThat((String) arr2[0].getValue(), is("hello, world!"));
        assertThat((byte[]) arr2[1].getValue(), is(new byte[]{0x01, 0x02, 0x03}));
        assertThat((byte[]) arr2[2].getValue(), is(new byte[]{0x01, 0x02, 0x03}));
        assertThat((byte[]) arr2[3].getValue(), is(new byte[]{0x01, 0x02, 0x03}));
        assertThat((boolean) arr2[4].getValue(), is(true));
        assertThat((boolean) arr2[5].getValue(), is(true));
        assertThat((Hash160) arr2[6].getValue(), is(new Hash160(
                "69ecca587293047be4c59159bf8bc399985c160d")));
        assertThat((Hash256) arr2[7].getValue(), is(new Hash256(
                "fe26f525c17b58f63a4d106fba973ec34cc99bfe2501c9f672cc145b483e398b")));
        assertThat(arr2[8].getValue(), is(nullValue()));
        Map<ContractParameter, ContractParameter> map =
                (Map<ContractParameter, ContractParameter>) arr2[9].getValue();
        List<Object> keys = map.keySet().stream().map(ContractParameter::getValue)
                .collect(Collectors.toList());
        List<Object> values = map.values().stream().map(ContractParameter::getValue)
                .collect(Collectors.toList());
        assertThat(keys, containsInAnyOrder(new BigInteger("5"), new byte[]{0x01, 0x02, 0x03}));
        assertThat(values, containsInAnyOrder("value", new BigInteger("5")));

        // Must not fail.
        ScriptBuilder b = new ScriptBuilder();
        b.pushParam(p);
    }
}
