package io.neow3j.compiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.annotations.Safe;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class SafeMethodTest {

    @Test
    public void methodShouldBeMarkedSafeIfAnnotatedWithSafeAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(SafeMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods();
        assertTrue(methods.get(0).isSafe());
        assertTrue(methods.get(2).isSafe());
    }

    @Test
    public void methodShouldNotBeMarkedSafeIfNotAnnotatedWithSafeAnnotation() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(SafeMethodTestContract.class.getName());
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
