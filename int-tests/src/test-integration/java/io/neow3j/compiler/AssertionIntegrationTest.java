package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.contracts.StdLib;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.bool;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssertionIntegrationTest {

    private static final String NEOVM_FAILED_ASSERT_MESSAGE = "ASSERT is executed with false result.";

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(AssertionTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void testAssertion() throws IOException {
        InvocationResult result = ct.callInvokeFunction(testName, integer(17)).getInvocationResult();
        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertNull(result.getException());

        result = ct.callInvokeFunction(testName, integer(18)).getInvocationResult();
        assertThat(result.getState(), is(NeoVMStateType.FAULT));
        assertThat(result.getException(), is(NEOVM_FAILED_ASSERT_MESSAGE));
    }

    @Test
    public void testAssertionWithStaticVar() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(100));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), is(NEOVM_FAILED_ASSERT_MESSAGE));

        response = ct.callInvokeFunction(testName, integer(42));
        result = response.getInvocationResult();
        assertThat(result.getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testWitnessCheck() throws Throwable {
        Account a = Account.fromWIF("L4NH7MLEdnX6u8vGx1qTLnuE9Aa5ovKcrVtUQfhyksqcAwZ4Xfto");
        NeoInvokeFunction response = ct.getContract()
                .callInvokeFunction(testName, asList(hash160(a)), calledByEntry(Account.create()));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), is(NEOVM_FAILED_ASSERT_MESSAGE));

        response = ct.getContract().callInvokeFunction(testName, asList(hash160(a)), calledByEntry(a));
        result = response.getInvocationResult();
        assertThat(result.getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testAssertionWithMethod() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, string("hello, world!"));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, string("hello world"));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
        assertThat(response.getInvocationResult().getException(), containsString(NEOVM_FAILED_ASSERT_MESSAGE));
    }

    @Test
    public void testEQ() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void testNE() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testLT() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(40));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void testGT() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(44));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testLE() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(44));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void testGE() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(40));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(41));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, integer(42));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, integer(43));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testIFNOT() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));

        response = ct.callInvokeFunction(testName, bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    @Test
    public void testIF() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));

        response = ct.callInvokeFunction(testName, bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
    }

    @Test
    public void testComplexAssertion() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                string("string"), string("string"), integer(5), string("5"), bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        response = ct.callInvokeFunction(testName,
                string("string"), string("not-string"), integer(42), string("5"), bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        response = ct.callInvokeFunction(testName,
                string("hello" + ", world!"), string("not-string"), integer(1), string("5"), bool(false));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.HALT));
        response = ct.callInvokeFunction(testName,
                string("string"), string("not-string"), integer(1), string("1"), bool(true));
        assertThat(response.getInvocationResult().getState(), is(NeoVMStateType.FAULT));
    }

    static class AssertionTestContract {

        public static int VAR = 42;

        public static void testAssertion(int i) {
            assert i == 17;
        }

        public static boolean testAssertionWithStaticVar(int i) {
            assert VAR == i;
            return true;
        }

        public static void testWitnessCheck(Hash160 witness) {
            assert Runtime.checkWitness(witness);
        }

        public static void testAssertionWithMethod(String value) {
            assert value == getString();
        }

        private static String getString() {
            return "hello" + ", world!";
        }

        public static void testEQ(int i) {
            assert i == VAR;
        }

        public static void testNE(int i) {
            assert i != VAR;
        }

        public static void testLT(int i) {
            assert i < VAR;
        }

        public static void testGT(int i) {
            assert i > VAR;
        }

        public static void testLE(int i) {
            assert i <= VAR;
        }

        public static void testGE(int i) {
            assert i >= VAR;
        }

        public static void testIFNOT(boolean b) {
            assert !b;
        }

        public static void testIF(boolean b) {
            assert b;
        }

        public static void testComplexAssertion(String a, String b, Integer i, String n, boolean c) {
            assert a == b && i == new StdLib().atoi(n, 10) || i == 42 && c || a == getString();
        }

    }

}
