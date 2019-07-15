package io.neow3j.model;

import io.neow3j.model.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;

public class ContractParameterTest {

    private ContractParameter contractParameter;

    @Before
    public void setUp() {
        // TODO 14.07.19 claude:
        // Write test for all static creation methods in ContractParameter.
        this.contractParameter = ContractParameter.string("value");
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
        assertThat(result, not(eq(0)));
    }

    @Test
    public void testToString() {
        String result = contractParameter.toString();
        assertThat(result, is("ContractParameter{paramName='null', paramType=STRING, value=value}"));
    }
}