package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.OnNEP17Payment;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.types.ContractParameter;
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

public class OnNEP17PaymentMethodTest {

    private final static String ONNEP17PAYMENT_METHOD_NAME = "onNEP17Payment";

    @Test
    public void whenUsingTheOnNEP17PaymentAnnotationTheCorrectMethodNameShouldAppearInTheManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compile(OnNep17PaymentMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(ONNEP17PAYMENT_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        ContractParameterType[] paramTypes = methods.get(0).getParameters().stream().map(
                ContractParameter::getType).toArray(ContractParameterType[]::new);
        assertThat(paramTypes, is(new Object[]{ContractParameterType.HASH160,
                ContractParameterType.INTEGER, ContractParameterType.ANY}));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.VOID));

    }

    @Test
    public void OnNep11PaymentMethodIllegalReturnType() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(
                        OnNep17PaymentMethodIllegalReturnTypeTestContract.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                "onPayment", "required to have", Hash160.class.getName(), int.class.getName(),
                Object.class.getName(), void.class.getName())));
    }

    @Test
    public void OnNep17PaymentMethodIllegalParameters() throws IOException {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(
                        OnNep17PaymentMethodIllegalParametersTestContract.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                "onPayment", "required to have", Hash160.class.getName(), int.class.getName(),
                Object.class.getName(), void.class.getName())));
    }

    @Test
    public void throwExceptionWhenMultipleMethodsAreUsed() throws IOException {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(
                        MultipleOnNep17PaymentMethodsTestContract.class.getName())
        );
        assertThat(thrown.getMessage(),
                stringContainsInOrder(asList("multiple methods", ONNEP17PAYMENT_METHOD_NAME)));
    }

    static class OnNep17PaymentMethodTestContract {

        @OnNEP17Payment
        public static void onPayment(Hash160 hash, int i, Object data) {
        }

    }

    static class OnNep17PaymentMethodIllegalReturnTypeTestContract {

        @OnNEP17Payment
        public static int onPayment(Hash160 hash, int i, Object data) {
            return i;
        }

    }

    static class OnNep17PaymentMethodIllegalParametersTestContract {

        @OnNEP17Payment
        public static void onPayment(Hash160 hash, String s, String data) {
        }

    }

    static class MultipleOnNep17PaymentMethodsTestContract {

        @OnNEP17Payment
        public static void onPayment1(Hash160 hash, int i, Object data) {
        }

        @OnNEP17Payment
        public static void onPayment2(Hash160 hash, int i, Object data) {
        }

    }

}
