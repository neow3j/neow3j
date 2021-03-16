package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.utils.ContractCompilationTestRule;
import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.contracts.NeoToken;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class StringConcatenationTest extends ContractTest {

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(privateNetContainer)
            .around(
                    new ContractCompilationTestRule(
                            StringConcatenation.class.getName(),
                            privateNetContainer
                    )
            );

    @Test
    public void concatTwoStrings() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("one"),
                ContractParameter.string("two"));
        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("onetwo"));
    }

    @Test
    public void concatStringsFromMixedSources() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("one"),
                ContractParameter.string("two"),
                ContractParameter.byteArray("4e656f")); // byte array representation of "Neo".

        assertThat(response.getInvocationResult().getStack().get(0).getString(),
                is("onetwothreeNeoNEO"));
    }

    @Test
    public void concatInStaticVariable() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).getString(), is("onetwoNEO"));
    }

    static class StringConcatenation {

        private static final String staticString = "one" + "two" + NeoToken.symbol();

        public static String concatTwoStrings(String s1, String s2) {
            return s1 + s2;
        }

        public static String concatStringsFromMixedSources(String s1, String s2, byte[] s3) {
            return s1 + s2 + "three" + Helper.toByteString(s3) + NeoToken.symbol();
        }

        public static String concatInStaticVariable() {
            return staticString;
        }

    }
}

