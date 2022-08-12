package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.types.ContractParameterType;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
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

public class VerificationMethodTest {

    private static final String VERIFY_METHOD_NAME = "verify";

    @Test
    public void whenUsingTheOnVerificationAnnotationTheVerifyMethodShouldBeInTheContractManifest()
            throws IOException {

        CompilationUnit unit = new Compiler()
                .compile(VerificationMethodTestContract.class.getName());
        List<ContractMethod> methods = unit.getManifest().getAbi().getMethods().stream()
                .filter(m -> m.getName().equals(VERIFY_METHOD_NAME))
                .collect(Collectors.toList());
        assertThat(methods, hasSize(1));
        assertThat(methods.get(0).getReturnType(), is(ContractParameterType.BOOLEAN));
    }

    @Test
    public void throwExceptionWhenVerifyMethodHasIllegalSignature() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(VerificationMethodIllegalSignatureTestContract.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(asList(
                "doVerify", "required to have return type", "boolean")));
    }

    @Test
    public void throwExceptionWhenMultipleVerifyMethodsAreUsed() {
        CompilerException thrown = assertThrows(CompilerException.class,
                () -> new Compiler().compile(MultipleVerificationMethodsTestContract.class.getName())
        );
        assertThat(thrown.getMessage(), stringContainsInOrder(
                asList("multiple methods", VERIFY_METHOD_NAME)));
    }

    static class VerificationMethodTestContract {

        static Hash160 ownerScriptHash = StringLiteralHelper.addressToScriptHash(
                "NiNmXL8FjEUEs1nfX9uHFBNaenxDHJtmuB");

        @OnVerification
        public static boolean doVerify() {
            return Runtime.checkWitness(ownerScriptHash);
        }
    }

    static class VerificationMethodIllegalSignatureTestContract {

        @OnVerification
        public static int doVerify(int i) {
            return i;
        }

    }

    static class MultipleVerificationMethodsTestContract {

        @OnVerification
        public static boolean doVerify1() {
            return true;
        }

        @OnVerification
        public static boolean doVerify2() {
            return true;
        }

    }

}
