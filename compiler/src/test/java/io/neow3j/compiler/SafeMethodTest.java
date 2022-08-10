package io.neow3j.compiler;

import io.neow3j.devpack.annotations.Safe;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SafeMethodTest {

    @Test
    public void methodShouldBeMarkedSafeIfAnnotatedWithSafeAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compile(SafeMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods();
        assertTrue(methods.get(0).isSafe());
        assertTrue(methods.get(2).isSafe());
    }

    @Test
    public void methodShouldNotBeMarkedSafeIfNotAnnotatedWithSafeAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compile(SafeMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods();
        assertFalse(methods.get(1).isSafe());
    }

    static class SafeMethodTestContract {

        @Safe
        public static void safeMethod() {
        }

        public static void unsafeMethod() {
        }

        @Safe
        public static void secondSafeMethod() {
        }

    }

}
