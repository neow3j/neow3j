package io.neow3j.compiler;

import io.neow3j.devpack.Helper;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.ContractParameter;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StringConcatenationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(StringConcatenation.class.getName());

    @Test
    public void concatTwoStrings() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("one"),
                ContractParameter.string("two"));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("onetwo"));
    }

    @Test
    public void concatStringsFromMixedSources() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                ContractParameter.string("one"),
                ContractParameter.string("two"),
                ContractParameter.byteArray("4e656f")); // byte array representation of "Neo".

        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("onetwothreeNeoNEO"));
    }

    @Test
    public void concatInStaticVariable() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);

        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("onetwoNEO"));
    }

    @Test
    public void concatStringWithMethodReturn() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("awesome token"));
    }

    @Test
    public void concatStringWithStaticStringValue() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("hello world!"));
    }

    @Test
    public void concatStringWithStaticFinalIntegerValue() throws IOException {
        // Static final int values can be concatenated because their value is concatenated already by the Java compiler.
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("area51"));
    }

    @Test
    public void concatWithValueFromStruct() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("hello world"));
    }

    static class StringConcatenation {

        private static final String staticString = "one" + "two" + NeoToken.symbol();
        static String value1 = "hello";
        static final int value2 = 51;


        public static String getStringValue() {
            return "token";
        }

        public static String concatTwoStrings(String s1, String s2) {
            return s1 + s2;
        }

        public static String concatStringsFromMixedSources(String s1, String s2, byte[] s3) {
            return s1 + s2 + "three" + Helper.toString(s3) + NeoToken.symbol();
        }

        public static String concatInStaticVariable() {
            return staticString;
        }

        public static String concatStringWithMethodReturn() {
            return "awesome " + getStringValue();
        }

        public static String concatStringWithStaticStringValue() {
            return value1 + " world!";
        }

        public static String concatStringWithStaticFinalIntegerValue() {
            return "area" + value2;
        }

        public static String concatWithValueFromStruct() {
            return "hello " + new POJO("world").value;
        }

        @Struct
        static class POJO {
            String value;

            POJO(String value) {
                this.value = value;
            }
        }

    }

}
