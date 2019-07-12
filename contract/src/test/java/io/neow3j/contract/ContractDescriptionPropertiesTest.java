package io.neow3j.contract;

import io.neow3j.utils.Numeric;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ContractDescriptionPropertiesTest {

    @Test
    public void test_All_Params() {
        ContractDescriptionProperties dp = new ContractDescriptionProperties(
                "Test",
                "0.1",
                "Guil",
                "email@email.com",
                "Anything."
        );

        byte[] expected = Numeric.hexStringToByteArray("09416e797468696e672e0f656d61696c40656d61696c2e636f6d044775696c03302e310454657374");
        byte[] result = dp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Empty_Params() {
        ContractDescriptionProperties dp = new ContractDescriptionProperties(
                "",
                "",
                "",
                "",
                ""
        );

        byte[] expected = Numeric.hexStringToByteArray("0000000000");
        byte[] result = dp.toArray();

        assertThat(expected, is(result));
    }

    @Test
    public void test_Null_Params() {
        ContractDescriptionProperties dp = new ContractDescriptionProperties(
                null,
                null,
                null,
                null,
                null
        );

        byte[] expected = Numeric.hexStringToByteArray("0000000000");
        byte[] result = dp.toArray();

        assertThat(expected, is(result));
    }

}
