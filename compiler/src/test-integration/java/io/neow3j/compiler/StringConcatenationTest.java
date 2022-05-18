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
    public void concatInStaticVariableWithChar() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("onetwon"));
    }

    @Test
    public void concatStringWithMethodReturn() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("awesome token"));
    }

    @Test
    public void concatStringWithMethodReturnChar() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("neow3j"));
    }

    @Test
    public void concatStringWithStaticStringValue() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("hello world!"));
    }

    @Test
    public void concatStringWithStaticCharValue() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("A and B"));
    }

    @Test
    public void concatStringWithStaticCharWrapperValue() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("A and B"));
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

    @Test
    public void concatWithCharValueFromStruct() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("hello C"));
    }

    @Test
    public void concatWithCharWrapperValueFromStruct() throws IOException {
        InvocationResult response = ct.callInvokeFunction(testName).getInvocationResult();
        assertThat(response.getStack().get(0).getString(), is("hello N"));
    }

    static class StringConcatenation {

        private static final String staticString = "one" + "two" + NeoToken.symbol();
        private static final String staticStringWithChar = "one" + "two" + getCharVal();

        static String value1 = "hello";
        static final int value2 = 51;
        static char charVal = 'B';
        static Character charWrapperVal = 'B';


        public static String getStringValue() {
            return "token";
        }

        public static char getCharVal() {
            return 'n';
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

        public static String concatInStaticVariableWithChar() {
            return staticStringWithChar;
        }

        public static String concatStringWithMethodReturn() {
            return "awesome " + getStringValue();
        }

        public static String concatStringWithMethodReturnChar() {
            return getCharVal() + "eow3j";
        }

        public static String concatStringWithStaticStringValue() {
            return value1 + " world!";
        }

        public static String concatStringWithStaticCharValue() {
            return "A and " + charVal;
        }

        public static String concatStringWithStaticCharWrapperValue() {
            return "A and " + charWrapperVal;
        }

        public static String concatStringWithStaticFinalIntegerValue() {
            return "area" + value2;
        }

        public static String concatWithValueFromStruct() {
            return "hello " + new POJO("world", 'C', 'N').strVal;
        }

        public static String concatWithCharValueFromStruct() {
            return "hello " + new POJO("world", 'C', 'N').charVal;
        }

        public static String concatWithCharWrapperValueFromStruct() {
            return "hello " + new POJO("world", 'C', 'N').charWrapperVal;
        }

        @Struct
        static class POJO {
            String strVal;
            char charVal;

            Character charWrapperVal;

            POJO(String strVal, char charVal, Character charWrapperVal) {
                this.strVal = strVal;
                this.charVal = charVal;
                this.charWrapperVal = charWrapperVal;
            }
        }

    }

}
