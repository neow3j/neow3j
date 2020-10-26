package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.neo.NEO;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringConcatenationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(StringConcatenation.class.getName());
    }

    @Test
    public void concatTwoStrings() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("one"),
                ContractParameter.string("two"));

        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("onetwo"));
    }

    @Test
    public void concatStringsFromMixedSources() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.string("one"),
                ContractParameter.string("two"),
                ContractParameter.byteArray("4e656f")); // byte array representation of "Neo".

        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("onetwothreeNeoNEO"));
    }

    @Test
    public void concatInStaticVariable() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("onetwoNEO"));
    }

    static class StringConcatenation {

        private static final String staticString = "one" + "two" + Runtime.getPlatform();

        public static String concatTwoStrings(String s1, String s2) {
            return s1 + s2;
        }

        public static String concatStringsFromMixedSources(String s1, String s2, byte[] s3) {
            return s1 + s2 + "three" + Helper.toByteString(s3) + NEO.name();
        }

        public static String concatInStaticVariable() {
            return staticString;
        }

    }
}

