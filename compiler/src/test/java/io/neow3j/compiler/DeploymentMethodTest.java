package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.neo.Runtime;
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

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void whenUsingTheOnDeploymentAnnotationTheDeployMethodShouldBeInTheContractManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compileClass(DeploymentMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(Compiler.DEPLOY_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.VOID));
    }

    @Test
    public void throwExceptionWhenDeployMethodHasIllegalSignature() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                OnDeployment.class.getSimpleName(),
                "required to have a boolean parameter and a void return type")));
        new Compiler().compileClass(DeploymentMethodIllegalReturnTypeTestContract.class.getName());

        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                OnDeployment.class.getSimpleName(),
                "required to have a boolean parameter and a void return type")));
        new Compiler().compileClass(
                DeploymentMethodIllegalParameterTypeTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenMultipleDeployMethodsAreUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "More than one method is marked", OnDeployment.class.getSimpleName(),
                Compiler.DEPLOY_METHOD_NAME)));
        new Compiler().compileClass(MultipleDeploymentMethodsTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenDeployMethodIsUsedWithoutAnnotation() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(Compiler.DEPLOY_METHOD_NAME,
                "method which is not annotated", OnDeployment.class.getSimpleName())));
        new Compiler().compileClass(DeploymentMethodWithoutAnnotationContract.class.getName());

    }

    static class DeploymentMethodTestContract {

        @OnDeployment
        public static void doDeploy(boolean update) {
            return;
        }
    }

    static class DeploymentMethodIllegalReturnTypeTestContract {

        @OnDeployment
        public static int doDeploy(boolean update) {
            return 41;
        }

    }

    static class DeploymentMethodIllegalParameterTypeTestContract {

        @OnDeployment
        public static void doDeploy(int wrongParam) {
            return;
        }

    }

    static class MultipleDeploymentMethodsTestContract {

        @OnDeployment
        public static void doDeploy1(boolean update) {
            return;
        }

        @OnDeployment
        public static void doDeploy2(boolean update) {
            return;
        }

    }

    static class DeploymentMethodWithoutAnnotationContract {

        public static void _deploy(boolean update) {
            return;
        }

    }

}
