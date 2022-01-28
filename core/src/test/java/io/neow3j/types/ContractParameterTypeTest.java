package io.neow3j.types;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
        ContractParameterType.valueOf((byte) 0xab);
    }

    @Test
    public void testFromJsonValue() {
        assertThat(ContractParameterType.fromJsonValue("String"), is(ContractParameterType.STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonValue_NotFound() {
        ContractParameterType.fromJsonValue("Anything");
    }

}
