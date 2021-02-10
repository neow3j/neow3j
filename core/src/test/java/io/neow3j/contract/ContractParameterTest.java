package io.neow3j.contract;

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
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

public class ContractParameterTest {

    private ContractParameter contractParameter;

    @Before
    public void setUp() {
        this.contractParameter = ContractParameter.string("value");
    }

    @Test
    public void testStringParamCreation() {
        String value = "value";
        ContractParameter p = ContractParameter.string(value);
        assertEquals(value, p.getValue());
        assertEquals(ContractParameterType.STRING, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreation() {
        byte[] bytes = new byte[]{0x01, 0x01};
        ContractParameter p = ContractParameter.byteArray(bytes);
        assertThat((byte[]) p.getValue(), is(bytes));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreationFromHexString() {
        ContractParameter p = ContractParameter.byteArray("0xa602");
        assertThat((byte[]) p.getValue(), is(new byte[]{(byte) 0xa6, 0x02}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test
    public void testByteArrayParamCreationFromString() {
        ContractParameter p = ContractParameter.byteArrayFromString("Neo");
        assertThat(((byte[]) p.getValue()), is(new byte[]{(byte) 0x4e, (byte) 0x65, (byte) 0x6f}));
        assertEquals(ContractParameterType.BYTE_ARRAY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testByteArrayParamCreationFromInvalidHexString() {
        String value = "value";
        ContractParameter.byteArray(value);
    }

    @Test
    public void testArrayParamCreationFromList() {
        List<ContractParameter> params = new ArrayList<>();
        ContractParameter p1 = ContractParameter.string("value");
        ContractParameter p2 = ContractParameter.byteArray("0x0101");
        params.add(p1);
        params.add(p2);
        ContractParameter p = ContractParameter.array(params);

        assertEquals(ContractParameterType.ARRAY, p.getParamType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(p1, ((ContractParameter[]) p.getValue())[0]);
        assertEquals(p2, ((ContractParameter[]) p.getValue())[1]);
    }

    @Test
    public void testArrayParamCreationFromArray() {
        ContractParameter p1 = ContractParameter.string("value");
        ContractParameter p2 = ContractParameter.byteArray("0x0101");
        ContractParameter p = ContractParameter.array(p1, p2);

        assertEquals(ContractParameterType.ARRAY, p.getParamType());
        assertEquals(ContractParameter[].class, p.getValue().getClass());
        assertEquals(p1, ((ContractParameter[]) p.getValue())[0]);
        assertEquals(p2, ((ContractParameter[]) p.getValue())[1]);
    }

    @Test
    public void testSignatureParamCreationFromValidString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = ContractParameter.signature(sig);

        assertArrayEquals(Numeric.hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromValidStringStrip0x() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        ContractParameter p = ContractParameter.signature("0x" + sig);

        assertArrayEquals(Numeric.hexStringToByteArray(sig), (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test
    public void testSignatureParamCreationFromByteArray() {
        String sigString = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b" +
                "6185ceab38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f213";
        byte[] sig = Numeric.hexStringToByteArray(sigString);
        ContractParameter p = ContractParameter.signature(sig);

        assertArrayEquals(sig, (byte[]) p.getValue());
        assertEquals(ContractParameterType.SIGNATURE, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignatureParamCreationFromTooShortString() {
        String sig = "d8485d4771e9112cca6ac7e6b75fc52585a2e7ee9a702db4a39dfad0f888ea6c22b6185ceab" +
                "38d8322b67737a5574d8b63f4e27b0d208f3f9efcdbf56093f2";
        ContractParameter.signature(sig);
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
        ContractParameter.signature(sig);
    }

    @Test
    public void testBooleanParameterCreation() {
        ContractParameter p = ContractParameter.bool(false);

        assertEquals(ContractParameterType.BOOLEAN, p.getParamType());
        assertEquals(false, p.getValue());

        p = ContractParameter.bool(true);

        assertEquals(ContractParameterType.BOOLEAN, p.getParamType());
        assertEquals(true, p.getValue());
    }

    @Test
    public void testIntegerParameterCreation() {
        ContractParameter p = ContractParameter.integer(10);

        assertEquals(ContractParameterType.INTEGER, p.getParamType());
        assertEquals(BigInteger.TEN, p.getValue());

        p = ContractParameter.integer(BigInteger.ONE.negate());

        assertEquals(ContractParameterType.INTEGER, p.getParamType());
        assertEquals(BigInteger.ONE.negate(), p.getValue());
    }

    @Test
    public void testHash160ParameterCreationFromValidScriptHash() {
        String hashString = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        ScriptHash hash = new ScriptHash(hashString);
        ContractParameter p = ContractParameter.hash160(hash);

        assertEquals(ContractParameterType.HASH160, p.getParamType());
        assertEquals(hashString, p.getValue().toString());
    }

    @Test
    public void testHash160ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6f";
        ContractParameter p = ContractParameter.hash160(new ScriptHash(hashValue));

        assertEquals(ContractParameterType.HASH160, p.getParamType());
        assertEquals(hashValue, p.getValue().toString());
    }

    @Test
    public void testHash256ParameterCreationFromValidString() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = ContractParameter.hash256(hashValue);

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, Numeric.toHexStringNoPrefix((byte[]) (p.getValue())));
    }

    @Test
    public void testHash256ParameterCreationFromValidByteArray() {
        String hashValue = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cf";
        ContractParameter p = ContractParameter.hash256(Numeric.hexStringToByteArray(hashValue));

        assertEquals(ContractParameterType.HASH256, p.getParamType());
        assertEquals(hashValue, Numeric.toHexStringNoPrefix((byte[]) (p.getValue())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromTooShortString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6c";
        ContractParameter.hash256(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromTooLongString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cfaa";
        ContractParameter.hash256(sig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHash256ParamCreationFromNoHexString() {
        String sig = "576f6f6c6f576f6f6c6f576f6f6c6f576f6f6c6ff6c6f576f6f6c6f576f6f6cg";
        ContractParameter.hash256(sig);
    }

    @Test
    public void testPublicKeyParamCreationFromByteArray() {
        byte[] pubKey = Numeric.hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816");
        ContractParameter p = ContractParameter.publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublicKeyParamCreationFromInvalidByteArray() {
        // one byte too short
        byte[] pubKey = Numeric.hexStringToByteArray(
                "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368");
        ContractParameter p = ContractParameter.publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(pubKey));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test
    public void testPublicKeyParamCreationFromHexString() {
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e136816";
        ContractParameter p = ContractParameter.publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(Numeric.hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPublicKeyParamCreationFromInvalidHexString() {
        // one byte too short.
        String pubKey = "03b4af8d061b6b320cce6c63bc4ec7894dce107bfc5f5ef5c68a93b4ad1e1368";
        ContractParameter p = ContractParameter.publicKey(pubKey);
        assertThat((byte[]) p.getValue(), is(Numeric.hexStringToByteArray(pubKey)));
        assertEquals(ContractParameterType.PUBLIC_KEY, p.getParamType());
    }

    @Test
    public void testGetParamType() {
        assertThat(contractParameter.getParamType(), Is.is(ContractParameterType.STRING));
    }

    @Test
    public void testGetValue() {
        assertThat(contractParameter.getValue(), is("value"));
    }

    @Test
    public void testEquals() {
        assertThat(contractParameter.equals("o"), is(false));
        assertThat(contractParameter.equals(this.contractParameter), is(true));
    }

    @Test
    public void testHashCode() {
        int result = contractParameter.hashCode();
        assertNotEquals(0, result);
    }
}
