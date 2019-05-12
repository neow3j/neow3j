package io.neow3j.model.types;

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
        assertThat(this.contractParameterType.byteValue(), is((byte) 0x07));
    }

    @Test
    public void testValueOf() {
        assertThat(ContractParameterType.valueOf((byte) 0x07), is(ContractParameterType.STRING));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(ContractParameterType.fromJsonValue("String"), is(ContractParameterType.STRING));
    }
}