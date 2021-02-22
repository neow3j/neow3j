package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DeploymentMethodTest {

    private static final String DEPLOY_METHOD_NAME = "_deploy";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void whenUsingTheOnDeploymentAnnotationTheDeployMethodShouldBeInTheContractManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compileClass(DeploymentMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(DEPLOY_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.VOID));
    }

    @Test
    public void throwExceptionWhenDeployMethodHasIllegalSignature() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("doDeploy", "Object", "boolean", "void")));
        new Compiler().compileClass(DeploymentMethodIllegalReturnTypeTestContract.class.getName());

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("doDeploy", "Object", "boolean", "void")));
        new Compiler().compileClass(
                DeploymentMethodIllegalParameterTypeTestContract.class.getName());

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "doDeploy", "Object", "Boolean", "void")));
        new Compiler().compileClass(
                DeploymentMethodMissingParameterTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenMultipleDeployMethodsAreUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("multiple methods", DEPLOY_METHOD_NAME)));
        new Compiler().compileClass(MultipleDeploymentMethodsTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenTwoMethodSignatureAnnotationsAreUsedOnTheSameMethod()
            throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("annotatedMethod", "multiple annotations", "specific method signature")));
        new Compiler().compileClass(MultipleMethodSignatureAnnotationsTestContract.class.getName());
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
