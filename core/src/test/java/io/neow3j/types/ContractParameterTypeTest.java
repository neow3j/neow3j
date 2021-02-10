package io.neow3j.types;

import io.neow3j.model.types.ContractParameterType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ContractParameterTypeTest {

    private ContractParameterType contractParameterType;

    @Before
    public void setUp() {
        this.contractParameterType = ContractParameterType.STRING;
    }

    @Test
    public void testJsonValue() {
        assertThat(this.contractParameterType.jsonValue(), is("String"));
    }

    @Test
    public void testByteValue() {
        assertThat(this.contractParameterType.byteValue(), is((byte) 0x13));
    }

    @Test
    public void testValueOf() {
        assertThat(ContractParameterType.valueOf((byte) 0x13), is(ContractParameterType.STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_NotFound() {
        assertThat(ContractParameterType.valueOf((byte) 0xab), is(ContractParameterType.STRING));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(ContractParameterType.fromJsonValue("String"), is(ContractParameterType.STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonValue_NotFound() {
        assertThat(ContractParameterType.fromJsonValue("Anything"), is(ContractParameterType.STRING));
    }
}