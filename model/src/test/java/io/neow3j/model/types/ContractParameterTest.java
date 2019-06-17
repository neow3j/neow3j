package io.neow3j.model.types;

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
        this.contractParameter = new ContractParameter("paramName", ContractParameterType.STRING, "value");
    }

    @Test
    public void testGetParamName() {
        assertThat(contractParameter.getParamName(), is("paramName"));
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
    }

    @Test
    public void testHashCode() {
        int result = contractParameter.hashCode();
        assertThat(result, not(eq(0)));
    }

    @Test
    public void testToString() {
        String result = contractParameter.toString();
        assertThat(result, is("ContractParameter{paramName='paramName', paramType=STRING, value=value}"));
    }
}