package io.neow3j.types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContractParameterTypeTest {

    private ContractParameterType contractParameterType;

    @BeforeAll
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

    @Test
    public void testValueOf_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> ContractParameterType.valueOf((byte) 0xab));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(ContractParameterType.fromJsonValue("String"), is(ContractParameterType.STRING));
    }

    @Test
    public void testFromJsonValue_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> ContractParameterType.fromJsonValue("Anything"));
    }

}
