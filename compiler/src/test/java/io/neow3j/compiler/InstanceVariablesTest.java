package io.neow3j.compiler;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InstanceVariablesTest {

    @Test
    public void usageOfInstanceInitializerThrowsCompilerException() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(IllegalUseOfInstanceInitializer.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                IllegalUseOfInstanceInitializer.class.getSimpleName(),
                "has an explicit instance constructor")));
    }

    @Test
    public void usageOfInstanceVariablesThrowsCompilerException() {
        CompilerException thrown = assertThrows(CompilerException.class, () ->
                new Compiler().compile(IllegalUseOfInstanceVariables.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                IllegalUseOfInstanceVariables.class.getSimpleName(), "has non-static fields")));
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
