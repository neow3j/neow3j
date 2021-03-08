package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.array;
import static io.neow3j.contract.ContractParameter.bool;
import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.byteArrayFromString;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.contract.ContractParameter.signature;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContractParameterTest {

    private ContractParameter contractParameter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        ContractParameter fromHex = byteArray("0x796573");
        ContractParameter fromByteArray = byteArray(new byte[]{0x79, 0x65, 0x73});
        assertThat(fromHex, is(fromByteArray));
    }

    @Test
    public void testByteArrayParamCreationFromString() {
        ContractParameter p = byteArrayFromString("Neo");
        assertThat(((byte[]) p.getValue()), is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testByteArrayParamCreationFromInvalidHexString() {
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
        ContractParameter integer = integer(1);
        ContractParameter bool = bool(true);
        ContractParameter string = string("testString");
        ContractParameter byteArray = byteArray(new byte[]{5, 8});
        ContractParameter cParam =
                hash160(new Hash160("0xa2b524b68dfe43a9d56af84f443c6b9843b8028c"));

        ArrayList<ContractParameter> paramList = new ArrayList<>();
        paramList.add(integer);
        paramList.add(bool);
        paramList.add(string);
        paramList.add(byteArray);
        paramList.add(cParam);
        ContractParameter expected = array(paramList);

        ContractParameter arrayFromObjects = array(1, true, "testString", new byte[]{5, 8}, cParam);

        assertThat(arrayFromObjects, is(expected));
    }

    @Test
    public void testArrayParamCreationFromObjects_failUnSupportedObject() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "provided object could not be casted into a supported contract");
        array(new Object());
    }

    @Test
    public void testArrayParamCreationFromObjects_Null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "provided object could not be casted into a supported contract");
        array((Object) null);
    }

    @Test
    public void testArrayParam_Null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot add a null object to an array contract parameter");
        array((ContractParameter) null);
    }

    @Test
    public void testArrayParam_Empty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("At least one parameter is required");
        array();
    }

    @Test
    public void testSignatureParamCreationFromValidString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                     "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature(sig);

        assertArrayEquals(Numeric.hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromValidStringStrip0x() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                     "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = signature("0x" + sig);

        assertArrayEquals(Numeric.hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromByteArray() {
        String sigString = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b" +
                           "6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        byte[] sig = Numeric.hexStringToByteArray(sigString);
        ContractParameter p = signature(sig);

        assertArrayEquals(sig, (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignatureParamCreationFromTooShortString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                     "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f2";
        signature(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignatureParamCreationFromTooLongString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                     "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213ff";
        ContractParameter.signature(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignatureParamCreationFromNoHexString() {
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
    public void testHash160_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The script hash argument must not be null");
        hash160(null);
    }

    @Test
    public void testHash256ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(hashValue);

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, Numeric.toHexStringNoPrefix((byte[]) (p.getValue())));
    }

    @Test
    public void testHash256ParameterCreationFromValidByteArray() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = hash256(Numeric.hexStringToByteArray(hashValue));

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, Numeric.toHexStringNoPrefix((byte[]) (p.getValue())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromTooShortString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6c";
        hash256(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromTooLongString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cfaa";
        hash256(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromNoHexString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cg";
        hash256(sig);
    }

    @Test
    public void testPublicKeyParamCreationFromByteArray() {
        byte[] pubKey = Numeric.hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816");
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublicKeyParamCreationFromInvalidByteArray() {
        // one byte too short
        byte[] pubKey = Numeric.hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test
    public void testPublicKeyParamCreationFromHexString() {
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(Numeric.hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublicKeyParamCreationFromInvalidHexString() {
        // one byte too short.
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        ContractParameter p = publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(Numeric.hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
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
        assertThat(contractParameter.equals(this.contractParameter), is(true));
        assertNotEquals(contractParameter, string("test"));
        assertNotEquals(contractParameter, integer(1));
    }

    @Test
    public void testHashCode() {
        int result = contractParameter.hashCode();
        assertNotEquals(0, result);
    }

}
