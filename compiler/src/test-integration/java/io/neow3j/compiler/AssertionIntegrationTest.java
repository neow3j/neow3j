package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.wallet.Account;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AssertionIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(AssertionTestContract.class.getName());

    @Test
    public void testAssertion() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(11));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), containsString("assertion failed"));

        response = ct.callInvokeFunction(testName, integer(17));
        result = response.getInvocationResult();
        assertFalse(result.hasStateFault());
    }

    @Test
    public void testAssertionWithStaticVar() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(100));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), containsString("neoowww"));

        response = ct.callInvokeFunction(testName, integer(42));
        result = response.getInvocationResult();
        assertFalse(result.hasStateFault());
        assertTrue(result.getStack().get(0).getBoolean());
    }

    @Test
    public void testWitnessCheck() throws Throwable {
        Account a = Account.fromWIF("L4NH7MLEdnX6u8vGx1qTLnuE9Aa5ovKcrVtUQfhyksqcAwZ4Xfto");
        NeoInvokeFunction response = ct.getContract()
                .callInvokeFunction(testName.getMethodName(), asList(hash160(a)),
                        calledByEntry(Account.create()));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), containsString("No authorization!"));

        response = ct.getContract()
                .callInvokeFunction(testName.getMethodName(), asList(hash160(a)), calledByEntry(a));
        result = response.getInvocationResult();
        assertFalse(result.hasStateFault());
    }

    @Test
    public void testWitnessCheckWithErrorMessageFromMethod() throws Throwable {
        Account a = Account.fromWIF("L4NH7MLEdnX6u8vGx1qTLnuE9Aa5ovKcrVtUQfhyksqcAwZ4Xfto");
        NeoInvokeFunction response = ct.getContract()
                .callInvokeFunction(testName.getMethodName(), asList(hash160(a)),
                        calledByEntry(Account.create()));
        InvocationResult result = response.getInvocationResult();
        assertTrue(result.hasStateFault());
        assertThat(result.getException(), containsString("hello, world!"));

        response = ct.getContract()
                .callInvokeFunction(testName.getMethodName(), asList(hash160(a)), calledByEntry(a));
        result = response.getInvocationResult();
        assertFalse(result.hasStateFault());
    }

    static class AssertionTestContract {

        public static int VAR = 42;

        public static void testAssertion(int i) {
            assert i == 17;
        }

        public static boolean testAssertionWithStaticVar(int i) {
            assert VAR == i : "neoowww";
            return true;
        }

        public static void testWitnessCheck(Hash160 witness) {
            assert Runtime.checkWitness(witness) : "No authorization!";
        }

        public static void testWitnessCheckWithErrorMessageFromMethod(Hash160 witness) {
            assert Runtime.checkWitness(witness) : getString();
        }

        private static String getString() {
            return "hello" + ", world!";
        }
    }

}
