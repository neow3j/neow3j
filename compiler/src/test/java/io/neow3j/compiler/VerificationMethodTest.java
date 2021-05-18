package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VerificationMethodTest {

    private static final String VERIFY_METHOD_NAME = "verify";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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
    public void throwExceptionWhenVerifyMethodHasIllegalSignature() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(asList(
                "doVerify", "required to have return type", "boolean")));
        new Compiler().compile(VerificationMethodIllegalSignatureTestContract.class.getName());
    }

    @Test
    public void throwExceptionWhenMultipleVerifyMethodsAreUsed() throws IOException {
        exceptionRule.expect(CompilerException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(
                asList("multiple methods", VERIFY_METHOD_NAME)));
        new Compiler().compile(MultipleVerificationMethodsTestContract.class.getName());
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
