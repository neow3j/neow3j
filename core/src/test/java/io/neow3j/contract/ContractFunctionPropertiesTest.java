package io.neow3j.contract;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.util.Arrays;

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

}
