package io.neow3j.compiler;

import java.io.IOException;
import java.util.Arrays;

import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InstanceVariablesTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void usageOfInstanceInitializerThrowsCompilerException() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                IllegalUseOfInstanceInitializer.class.getSimpleName(),
                "has an explicit instance constructor")));
        new Compiler().compile(IllegalUseOfInstanceInitializer.class.getName());
    }

    @Test
    public void usageOfInstanceVariablesThrowsCompilerException() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList(
                IllegalUseOfInstanceVariables.class.getSimpleName(),
                "has non-static fields")));
        new Compiler().compile(IllegalUseOfInstanceVariables.class.getName());
    }

    static class IllegalUseOfInstanceInitializer {

        private static String address;

        {
            address = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
        }

        public static String main() {
            return address;
        }

    }

    static class IllegalUseOfInstanceVariables {

        private String address;

        public static String main() {
            return "hello, world";
        }

    }

}
