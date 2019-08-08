package io.neow3j.contract;

import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.util.Arrays;

import static io.neow3j.contract.ContractFunctionProperties.unpackIsPayable;
import static io.neow3j.contract.ContractFunctionProperties.unpackNeedsDynamicInvoke;
import static io.neow3j.contract.ContractFunctionProperties.unpackNeedsStorage;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ContractFunctionPropertiesTest {

    @Test
    public void test_One_ParamType() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                true
        );

        byte[] expected = Numeric.hexStringToByteArray("57550107");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Two_ParamType() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING, ContractParameterType.ARRAY),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                true
        );

        byte[] expected = Numeric.hexStringToByteArray("5755020710");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Five_ParamType() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING, ContractParameterType.ARRAY,
                        ContractParameterType.BOOLEAN, ContractParameterType.INTEGER, ContractParameterType.INTEGER),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                true
        );

        byte[] expected = Numeric.hexStringToByteArray("5755050710010202");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Storage_True() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.BYTE_ARRAY,
                true,
                false,
                false
        );

        byte[] expected = Numeric.hexStringToByteArray("51550107");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Storage_And_NeedsDynamicInvoke_True() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                false
        );

        byte[] expected = Numeric.hexStringToByteArray("53550107");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_All_True() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                true
        );

        byte[] expected = Numeric.hexStringToByteArray("57550107");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_All_False() {
        ContractFunctionProperties fp = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.BYTE_ARRAY,
                false,
                false,
                false
        );

        byte[] expected = Numeric.hexStringToByteArray("00550107");
        byte[] result = fp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Deserialize() throws IllegalAccessException, InstantiationException {
        ContractFunctionProperties expected = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING, ContractParameterType.ARRAY,
                        ContractParameterType.BOOLEAN, ContractParameterType.INTEGER, ContractParameterType.INTEGER),
                ContractParameterType.BYTE_ARRAY,
                true,
                true,
                true
        );

        byte[] raw = Numeric.hexStringToByteArray("5755050710010202");

        ContractFunctionProperties result = NeoSerializableInterface.from(raw, ContractFunctionProperties.class);
        assertThat(result, is(expected));
    }

    @Test
    public void testPackAndUnpackFlagsValue() {
        ContractFunctionProperties fp1 = buildFunctionProperties(false, false, false);
        ContractFunctionProperties fp2 = buildFunctionProperties(true, false, false);
        ContractFunctionProperties fp3 = buildFunctionProperties(true, true, false);
        ContractFunctionProperties fp4 = buildFunctionProperties(true, true, true);
        ContractFunctionProperties fp5 = buildFunctionProperties(true, false, true);
        ContractFunctionProperties fp6 = buildFunctionProperties(false, false, true);
        ContractFunctionProperties fp7 = buildFunctionProperties(false, true, true);
        ContractFunctionProperties fp8 = buildFunctionProperties(false, true, false);
        assertThat(Integer.toBinaryString(fp1.packFlagsValue()), is("0"));
        assertThat(Integer.toBinaryString(fp2.packFlagsValue()), is("1"));
        assertThat(Integer.toBinaryString(fp3.packFlagsValue()), is("11"));
        assertThat(Integer.toBinaryString(fp4.packFlagsValue()), is("111"));
        assertThat(Integer.toBinaryString(fp5.packFlagsValue()), is("101"));
        assertThat(Integer.toBinaryString(fp6.packFlagsValue()), is("100"));
        assertThat(Integer.toBinaryString(fp7.packFlagsValue()), is("110"));
        assertThat(Integer.toBinaryString(fp8.packFlagsValue()), is("10"));

        assertThat(unpackNeedsStorage(0), is(false));
        assertThat(unpackNeedsStorage(1), is(true));
        assertThat(unpackNeedsStorage(3), is(true));
        assertThat(unpackNeedsStorage(7), is(true));
        assertThat(unpackNeedsStorage(5), is(true));
        assertThat(unpackNeedsStorage(4), is(false));
        assertThat(unpackNeedsStorage(6), is(false));
        assertThat(unpackNeedsStorage(2), is(false));

        assertThat(unpackNeedsDynamicInvoke(0), is(false));
        assertThat(unpackNeedsDynamicInvoke(1), is(false));
        assertThat(unpackNeedsDynamicInvoke(3), is(true));
        assertThat(unpackNeedsDynamicInvoke(7), is(true));
        assertThat(unpackNeedsDynamicInvoke(5), is(false));
        assertThat(unpackNeedsDynamicInvoke(4), is(false));
        assertThat(unpackNeedsDynamicInvoke(6), is(true));
        assertThat(unpackNeedsDynamicInvoke(2), is(true));

        assertThat(unpackIsPayable(0), is(false));
        assertThat(unpackIsPayable(1), is(false));
        assertThat(unpackIsPayable(3), is(false));
        assertThat(unpackIsPayable(7), is(true));
        assertThat(unpackIsPayable(5), is(true));
        assertThat(unpackIsPayable(4), is(true));
        assertThat(unpackIsPayable(6), is(true));
        assertThat(unpackIsPayable(2), is(false));
    }

    private ContractFunctionProperties buildFunctionProperties(boolean needsStorage,
                                                               boolean needsDynamicInvoke,
                                                               boolean isPayable) {
        return new ContractFunctionProperties(
                null,
                null,
                needsStorage,
                needsDynamicInvoke,
                isPayable
        );
    }

    @Test
    public void testSerializeAndDeserialize1() throws IllegalAccessException, InstantiationException {
        ContractFunctionProperties properties = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.STRING),
                ContractParameterType.INTEROP_INTERFACE,
                true, true, true);

        byte[] rawProperties = properties.toArray();
        assertThat(rawProperties, is(Numeric.hexStringToByteArray("5702f0000107")));

        ContractFunctionProperties deserializedProps = NeoSerializableInterface.from(rawProperties,
                ContractFunctionProperties.class);
        assertThat(deserializedProps, is(properties));
    }

    @Test
    public void testSerializeAndDeserialize2() throws IllegalAccessException, InstantiationException {
        ContractFunctionProperties properties = new ContractFunctionProperties(
                Arrays.asList(ContractParameterType.INTEROP_INTERFACE),
                ContractParameterType.VOID,
                true, true, true);

        byte[] rawProperties = properties.toArray();
        assertThat(rawProperties, is(Numeric.hexStringToByteArray("5702ff0001f0")));

        ContractFunctionProperties deserializedProps = NeoSerializableInterface.from(rawProperties,
                ContractFunctionProperties.class);
        assertThat(deserializedProps, is(properties));
    }
}
