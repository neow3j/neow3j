package io.neow3j.compiler;

import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.types.ContractParameterType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeploymentMethodTest {

    private static final String DEPLOY_METHOD_NAME = "_deploy";

    @Test
    public void whenUsingTheOnDeploymentAnnotationTheDeployMethodShouldBeInTheContractManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compile(DeploymentMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(DEPLOY_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.VOID));
    }

    @Test
    public void throwExceptionWhenDeployMethodHasIllegalSignature() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(DeploymentMethodIllegalReturnTypeTestContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("doDeploy", "Object", "boolean", "void")));

        thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(DeploymentMethodIllegalParameterTypeTestContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("doDeploy", "Object", "boolean", "void")));

        thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(DeploymentMethodMissingParameterTestContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("doDeploy", "Object", "boolean", "void")));
    }

    @Test
    public void throwExceptionWhenMultipleDeployMethodsAreUsed() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(MultipleDeploymentMethodsTestContract.class.getName()));
        assertThat(thrown.getMessage(), stringContainsInOrder(asList("multiple methods", DEPLOY_METHOD_NAME)));
    }

    @Test
    public void throwExceptionWhenTwoMethodSignatureAnnotationsAreUsedOnTheSameMethod() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(MultipleMethodSignatureAnnotationsTestContract.class.getName()));
        assertThat(thrown.getMessage(),
                stringContainsInOrder(asList("annotatedMethod", "multiple annotations", "specific method signature")));
    }

    static class DeploymentMethodTestContract {

        @OnDeployment
        public static void doDeploy(Object data, boolean update) {
        }
    }

    static class DeploymentMethodIllegalReturnTypeTestContract {

        @OnDeployment
        public static int doDeploy(Object data, boolean update) {
            return 41;
        }
    }

    static class DeploymentMethodIllegalParameterTypeTestContract {

        @OnDeployment
        public static void doDeploy(Object data, int wrongParam) {
        }
    }

    static class DeploymentMethodMissingParameterTestContract {

        @OnDeployment
        public static void doDeploy(boolean update) {
        }
    }

    static class MultipleDeploymentMethodsTestContract {

        @OnDeployment
        public static void doDeploy1(Object data, boolean update) {
        }

        @OnDeployment
        public static void doDeploy2(Object data, boolean update) {
        }
    }

    static class MultipleMethodSignatureAnnotationsTestContract {

        @OnDeployment
        @OnVerification
        public static void annotatedMethod(Object data, boolean update) {
        }
    }

}
