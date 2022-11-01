package io.neow3j.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static io.neow3j.types.ContractParameter.mapToContractParameter;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.ContractParameter.signature;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
public class ContractParameterTest {

    private ContractParameter contractParameter;

    @BeforeEach
    public void setUp() {
        this.contractParameter = string("value");
    }

    @Test
    public void testStringParamCreation() {
        String value = "value";
        ContractParameter p = string(value);
        assertEquals(value, p.getValue());
        assertEquals(ContractParameterType.STRING, p.getType());
    }

    @Test
    public void testByteArrayParamCreation() {
        byte[] bytes = new byte[]{0x01, 0x01};
        ContractParameter p = byteArray(bytes);
        assertThat((byte[]) p.getValue(), is(bytes));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getType());
    }

    @Test
    public void testByteArrayParamCreationFromHexString() {
        ContractParameter p = byteArray("0xa602");
        assertThat((byte[]) p.getValue(), is(new byte[]{(byte) 0xa6, 0x02}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getType());
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
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getType());
    }

    @Test
    public void testByteArrayParamCreationFromInvalidHexString() {
        String value = "value";
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> byteArray(value));
        assertThat(thrown.getMessage(), is("Argument is not a valid hex number."));
    }

    @Test
    public void testArrayParamCreationFromList() {
        List<ContractParameter> params = new ArrayList<>();
        ContractParameter p1 = string("value");
        ContractParameter p2 = byteArray("0x0101");
        params.add(p1);
        params.add(p2);
        ContractParameter p = array(params);

        assertEquals(ContractParameterType.ARRAY, p.getType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(p1, ((ContractParameter[]) p.getValue())[0]);
        assertEquals(p2, ((ContractParameter[]) p.getValue())[1]);
    }

    @Test
    public void testArrayParamCreationFromArray() {
        ContractParameter p1 = string("value");
        ContractParameter p2 = byteArray("0x0101");
        ContractParameter p = array(p1, p2);

        assertEquals(ContractParameterType.ARRAY, p.getType());
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
        Hash256 txHash = new Hash256("257d342421fb5373a4d2ee7254ee7a968da66b2179b27c855e0462434c6386fd");
        BigInteger bigInt = BigInteger.valueOf(l);
        Account a = Account.create();

        ContractParameter expected = array(Arrays.asList(integer(1), integer(bigInt) /* the long */, bool(b), string(s),
                byteArray(bytes), hash160(contractHash), hash256(txHash), integer(bigInt), hash160(a)));

        ContractParameter arrayFromObjects = array(i, l, b, s, bytes, contractHash, txHash, bigInt, a);

        assertThat(arrayFromObjects, is(expected));
    }

    @Test
    public void testArrayParamCreationFromObjects_failUnSupportedObject() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> array(new Object()));
        assertThat(thrown.getMessage(),
                is("The provided object could not be casted into a supported contract parameter type."));
    }

    @Test
    public void testArrayParam_Empty() {
        ContractParameter param = array();
        ContractParameter[] value = (ContractParameter[]) param.getValue();
        assertThat(value.length, is(0));
    }

    @Test
    public void testArrayParam_Null() {
        ContractParameter param = array((Object) null);
        ContractParameter[] value = (ContractParameter[]) param.getValue();
        assertThat(value.length, is(1));
        assertEquals(value[0].getType(), ContractParameterType.ANY);
    }

    @Test
    public void testNestedArrayParamCreationFromObject() {
        List<Object> params = new ArrayList<>();
        String p1 = "value";
        String p2 = "0x0101";
        BigInteger p3 = BigInteger.valueOf(420);

        List<Object> p4 = new ArrayList<>();
        int p4_1 = 1024;
        String p4_2 = "neow3j";
        p4.add(p4_1);
        p4.add(p4_2);

        List<Object> p4_3 = new ArrayList<>();
        BigInteger p4_3_1 = BigInteger.TEN;
        p4_3.add(p4_3_1);
        p4.add(p4_3);
        byte p5 = 55;

        params.add(p1);
        params.add(p2);
        params.add(p3);
        params.add(p4);
        params.add(p5);
        ContractParameter p = array(params);

        assertEquals(ContractParameterType.ARRAY, p.getType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(string(p1), ((ContractParameter[]) p.getValue())[0]);
        assertEquals(string(p2), ((ContractParameter[]) p.getValue())[1]);
        assertEquals(integer(p3), ((ContractParameter[]) p.getValue())[2]);
        assertEquals(array(p4_1, p4_2, array(p4_3)), ((ContractParameter[]) p.getValue())[3]);
        assertEquals(integer(p5), ((ContractParameter[]) p.getValue())[4]);
    }

    @Test
    public void testSignatureParamCreationFromValidString() {
        String sig =
                "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature(sig);

        assertArrayEquals(hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getType());
    }

    @Test
    public void testSignatureParamCreationFromValidStringStrip0x() {
        String sig =
                "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature("0x" + sig);

        assertArrayEquals(hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getType());
    }

    @Test
    public void testSignatureParamCreationFromByteArray() {
        String sigString =
                "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        byte[] sig = hexStringToByteArray(sigString);
        ContractParameter p = signature(sig);

        assertArrayEquals(sig, (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getType());
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
        String sig =
                "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f2";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> signature(sig));
        assertThat(thrown.getMessage(), is("Signature is expected to have a length of 64 bytes, but had 63."));
    }

    @Test
    public void testSignatureParamCreationFromTooLongString() {
        String sig =
                "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213ff";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> signature(sig));
        assertThat(thrown.getMessage(), is("Signature is expected to have a length of 64 bytes, but had 65."));
    }

    @Test
    public void testSignatureParamCreationFromNoHexString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585t2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> signature(sig));
        assertThat(thrown.getMessage(), is("Argument is not a valid hex number."));
    }

    @Test
    public void testBooleanParameterCreation() {
        ContractParameter p = bool(false);

        assertEquals(ContractParameterType.BOOLEAN, p.getType());
        assertEquals(false, p.getValue());

        p = bool(true);

        assertEquals(ContractParameterType.BOOLEAN, p.getType());
        assertEquals(true, p.getValue());
    }

    @Test
    public void testIntegerParameterCreation() {
        ContractParameter p = integer(10);

        assertEquals(ContractParameterType.INTEGER, p.getType());
        assertEquals(BigInteger.TEN, p.getValue());

        p = integer(BigInteger.ONE.negate());

        assertEquals(ContractParameterType.INTEGER, p.getType());
        assertEquals(BigInteger.ONE.negate(), p.getValue());
    }

    @Test
    public void testHash160ParameterCreationFromValidScriptHash() {
        String hashString = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        Hash160 hash = new Hash160(hashString);
        ContractParameter p = hash160(hash);

        assertEquals(ContractParameterType.HASH160, p.getType());
        assertEquals(hashString, p.getValue().toString());
    }

    @Test
    public void testHash160ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        ContractParameter p = hash160(new Hash160(hashValue));

        assertEquals(ContractParameterType.HASH160, p.getType());
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
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> hash160((Hash160) null));
        assertThat(thrown.getMessage(), is("The script hash argument must not be null."));
    }

    @Test
    public void testHash256() {
        Hash256 hash = new Hash256("576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf");
        ContractParameter p = hash256(hash);

        assertThat(p.getType(), is(ContractParameterType.HASH256));
        assertThat(p.getValue(), is(hash));
    }

    @Test
    public void testHash256ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(hashValue);

        assertEquals(ContractParameterType.HASH256, p.getType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash256ParameterCreationFromValidByteArray() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(hexStringToByteArray(hashValue));

        assertEquals(ContractParameterType.HASH256, p.getType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash256ParamCreationFromTooShortString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> hash256(sig));
        assertThat(thrown.getMessage(), is("A Hash256 parameter must be 32 bytes but was 31 bytes."));
    }

    @Test
    public void testHash256ParamCreationFromTooLongString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cfaa";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> hash256(sig));
        assertThat(thrown.getMessage(), is("A Hash256 parameter must be 32 bytes but was 33 bytes."));
    }

    @Test
    public void testHash256ParamCreationFromNoHexString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cg";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> hash256(sig));
        assertThat(thrown.getMessage(), is("Argument is not a valid hex number."));
    }

    @Test
    public void testPublicKeyParamCreationFromECPublicKey() {
        ECKeyPair.ECPublicKey publicKey =
                new ECKeyPair.ECPublicKey("03b4af8efe55d98b44eedfcfaa39642fd5d53ad543d18d3cc2db5880970a4654f6");
        ContractParameter p = publicKey(publicKey);

        assertThat((byte[]) p.getValue(), is(publicKey.getEncoded(true)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getType());
    }

    @Test
    public void testPublicKeyParamCreationFromByteArray() {
        byte[] pubKey = hexStringToByteArray("03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816");
        ContractParameter p = publicKey(pubKey);

        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getType());
    }

    @Test
    public void testPublicKeyParamCreationFromInvalidByteArray() {
        // One byte too short
        byte[] pubKey = hexStringToByteArray("03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> publicKey(pubKey));
        assertThat(thrown.getMessage(), is("Public key argument must be 33 bytes but was 32 bytes."));
    }

    @Test
    public void testPublicKeyParamCreationFromHexString() {
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ContractParameter p = publicKey(pubKey);

        assertThat((byte[]) p.getValue(), is(hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getType());
    }

    @Test
    public void testPublicKeyParamCreationFromInvalidHexString() {
        // One byte too short.
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> publicKey(pubKey));
        assertThat(thrown.getMessage(), is("Public key argument must be 33 bytes but was 32 bytes."));
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
    public void testMap_withNestedObjects() {
        Map<Object, Object> map = new HashMap<>();
        map.put("one", "first");
        map.put("two", 2);

        int map1Key = 5;
        HashMap<Object, Object> map1 = new HashMap<>();
        String map1_1 = "hello";
        int map1_2 = 1234;
        map1.put(map1_1, map1_2);

        map.put(map1Key, map1);

        ContractParameter param = map(map);
        Map<?, ?> value = (Map<?, ?>) param.getValue();

        assertThat(value.keySet(), containsInAnyOrder(string("one"), string("two"), integer(map1Key)));
        assertThat(value.values(), containsInAnyOrder(string("first"), integer(2), map(map1)));
        assertThat(value.get(string("one")), is(string("first")));
        assertThat(value.get(string("two")), is(integer(2)));
        assertThat(value.get(integer(map1Key)), is(map(map1)));
    }

    @Test
    public void testMap_invalidKeyType() {
        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();
        map.put(array(integer(1), string("test")), string("first"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> map(map));
        assertThat(thrown.getMessage(), containsString("The provided map contains an invalid key."));
    }

    @Test
    public void testMap_empty() {
        HashMap<ContractParameter, ContractParameter> map = new HashMap<>();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> map(map));
        assertThat(thrown.getMessage(), is("At least one map entry is required to create a map contract parameter."));
    }

    @Test
    public void testMapToContractParameter() {
        ContractParameter p = mapToContractParameter(integer(12));
        assertThat(((BigInteger) p.getValue()).intValue(), is(12));
        assertThat(p.getType(), is(ContractParameterType.INTEGER));

        p = mapToContractParameter(true);
        assertTrue((Boolean) p.getValue());
        assertThat(p.getType(), is(ContractParameterType.BOOLEAN));

        p = mapToContractParameter(33);
        assertThat(((BigInteger) p.getValue()).intValue(), is(33));
        assertThat(p.getType(), is(ContractParameterType.INTEGER));

        p = mapToContractParameter(2000L);
        assertThat(((BigInteger) p.getValue()).longValue(), is(2000L));
        assertThat(p.getType(), is(ContractParameterType.INTEGER));

        p = mapToContractParameter(new BigInteger("12345"));
        assertThat(((BigInteger) p.getValue()), is(new BigInteger("12345")));
        assertThat(p.getType(), is(ContractParameterType.INTEGER));

        p = mapToContractParameter(new byte[]{0x12, 0x0a, 0x0f});
        assertThat(p.getValue(), is(new byte[]{0x12, 0x0a, 0x0f}));
        assertThat(p.getType(), is(ContractParameterType.BYTE_ARRAY));

        String s = "hello world!";
        p = mapToContractParameter(s);
        assertThat(((String) p.getValue()), is(s));
        assertThat(p.getType(), is(ContractParameterType.STRING));

        Hash160 hash160 = new Hash160("0f2dc86970b191fd8a55aeab983a04889682e433");
        p = mapToContractParameter(hash160);
        assertThat(((Hash160) p.getValue()), is(hash160));
        assertThat(p.getType(), is(ContractParameterType.HASH160));

        Hash256 hash256 = new Hash256("03b4af8d061b6b320cce6c63bc4ec7894dce107b03b4af8d061b6b320cce6c63");
        p = mapToContractParameter(hash256);
        assertThat(((Hash256) p.getValue()), is(hash256));
        assertThat(p.getType(), is(ContractParameterType.HASH256));

        Account a = Account.create();
        p = mapToContractParameter(a);
        assertThat(((Hash160) p.getValue()), is(a.getScriptHash()));
        assertThat(p.getType(), is(ContractParameterType.HASH160));

        p = mapToContractParameter(a.getECKeyPair().getPublicKey());
        assertThat(p.getValue(), is(a.getECKeyPair().getPublicKey().getEncoded(true)));
        assertThat(p.getType(), is(ContractParameterType.PUBLIC_KEY));

        Sign.SignatureData signatureData = Sign.signMessage("Test message.", a.getECKeyPair());
        p = mapToContractParameter(signatureData);
        assertThat(p.getValue(), is(signatureData.getConcatenated()));
        assertThat(p.getType(), is(ContractParameterType.SIGNATURE));

        p = mapToContractParameter(null);
        assertNull(p.getValue());
        assertThat(p.getType(), is(ContractParameterType.ANY));
    }

    @Test
    public void testMapToContractParameter_list() {
        ArrayList<Object> list = new ArrayList<>();
        list.add("neow3j");
        list.add(1024);
        ArrayList<Object> subList = new ArrayList<>();
        subList.add(12);
        subList.add(false);
        list.add(subList);
        Sign.SignatureData signatureData = Sign.signMessage("Test message.", Account.create().getECKeyPair());
        list.add(signatureData);
        ContractParameter p = mapToContractParameter(list);

        ContractParameter[] pList = (ContractParameter[]) p.getValue();
        assertThat(pList.length, is(4));
        assertThat(p.getType(), is(ContractParameterType.ARRAY));

        assertThat(((String) pList[0].getValue()), is("neow3j"));
        assertThat(pList[0].getType(), is(ContractParameterType.STRING));
        assertThat(((BigInteger) pList[1].getValue()).intValue(), is(1024));
        assertThat(pList[1].getType(), is(ContractParameterType.INTEGER));
        ContractParameter[] pSubList = (ContractParameter[]) pList[2].getValue();
        assertThat(pList[2].getType(), is(ContractParameterType.ARRAY));
        assertThat(pSubList.length, is(2));
        assertThat(((BigInteger) pSubList[0].getValue()).intValue(), is(12));
        assertThat(pSubList[0].getType(), is(ContractParameterType.INTEGER));
        assertFalse((Boolean) pSubList[1].getValue());
        assertThat(pSubList[1].getType(), is(ContractParameterType.BOOLEAN));

        assertThat(pList[3].getValue(), is(signatureData.getConcatenated()));
        assertThat(pList[3].getType(), is(ContractParameterType.SIGNATURE));
    }

    @Test
    public void testMapToContractParameter_map() {
        Map<Object, Object> map = new HashMap<>();
        int map1Key = 16;
        HashMap<Object, Object> map1 = new HashMap<>();
        String map1_1 = "halo";
        int map1_2 = 1234;
        map1.put(map1_1, map1_2);
        map.put(map1Key, map1);
        map.put("twelve", 12);
        map.put(true, 10);

        ContractParameter p = mapToContractParameter(map);
        Map<?, ?> value = (Map<?, ?>) p.getValue();

        assertThat(value.keySet(), containsInAnyOrder(string("twelve"), bool(true), integer(map1Key)));
        assertThat(value.values(), containsInAnyOrder(integer(12), integer(10), map(map1)));
        assertThat(value.get(integer(map1Key)), is(map(map1)));
        assertThat(value.get(bool(true)), is(integer(10)));
        assertThat(value.get(string("twelve")), is(integer(12)));
    }

    @Test
    public void testGetParamType() {
        assertThat(contractParameter.getType(), is(ContractParameterType.STRING));
    }

    @Test
    public void testGetValue() {
        assertThat(contractParameter.getValue(), is("value"));
    }

    @Test
    public void testEquals() {
        assertNotEquals("o", contractParameter);
        assertEquals(contractParameter, string("value"));
        assertNotEquals(contractParameter, string("test"));
        assertNotEquals(contractParameter, integer(1));

        ContractParameter param1 = hash160(Hash160.ZERO);
        ContractParameter param2 = hash160(Hash160.ZERO);
        assertEquals(param1, param2);

        param1 = new ContractParameter(null);
        param2 = hash160(Hash160.ZERO);
        assertNotEquals(param1, param2);

        param1 = new ContractParameter(ContractParameterType.HASH160, null);
        param2 = hash160(Hash160.ZERO);
        assertNotEquals(param1, param2);

        param1 = new ContractParameter(ContractParameterType.HASH160, null);
        param2 = new ContractParameter(ContractParameterType.HASH160, null);
        assertEquals(param1, param2);

        param1 = hash256(Hash256.ZERO);
        param2 = hash256(Hash256.ZERO);
        assertEquals(param1, param2);

        param1 = hash256(Hash256.ZERO);
        param2 = hash160(Hash160.ZERO);
        assertNotEquals(param1, param2);

        param1 = signature(
                "01020304010203040102030401020304010203040102030401020304010203040102030401020304010203040102030401020304010203040102030401020304");
        param2 = signature(
                "01020304010203040102030401020304010203040102030401020304010203040102030401020304010203040102030401020304010203040102030401020304");
        assertEquals(param1, param2);

        param1 = publicKey("010203040102030401020304010203040102030401020304010203040102030401");
        param2 = publicKey("010203040102030401020304010203040102030401020304010203040102030401");
        assertEquals(param1, param2);
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
                "                    \"value\":\"fe26f525c17b58f63a4d106fba973ec34cc99bfe2501c9f672cc145b483e398b\"\n" +
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
        assertThat((Hash160) arr2[6].getValue(), is(new Hash160("69ecca587293047be4c59159bf8bc399985c160d")));
        assertThat((Hash256) arr2[7].getValue(),
                is(new Hash256("fe26f525c17b58f63a4d106fba973ec34cc99bfe2501c9f672cc145b483e398b")));
        assertThat(arr2[8].getValue(), is(nullValue()));
        Map<ContractParameter, ContractParameter> map =
                (Map<ContractParameter, ContractParameter>) arr2[9].getValue();
        List<Object> keys = map.keySet().stream().map(ContractParameter::getValue).collect(Collectors.toList());
        List<Object> values = map.values().stream().map(ContractParameter::getValue).collect(Collectors.toList());
        assertThat(keys, containsInAnyOrder(new BigInteger("5"), new byte[]{0x01, 0x02, 0x03}));
        assertThat(values, containsInAnyOrder("value", new BigInteger("5")));

        // Must not fail.
        ScriptBuilder b = new ScriptBuilder();
        b.pushParam(p);
    }

}
