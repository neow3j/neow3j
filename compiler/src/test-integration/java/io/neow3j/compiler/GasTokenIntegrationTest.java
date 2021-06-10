package io.neow3j.compiler;

import io.neow3j.devpack.annotations.Permission;
import io.neow3j.types.Hash256;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Helper;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.Transaction;
import io.neow3j.devpack.contracts.GasToken;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.gasTokenHash;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GasTokenIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            GasTokenIntegrationTestContract.class.getName());

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(gasTokenHash())));
    }

    @Test
    public void refuel() throws Throwable {
        // Provide the default account with enough GAS.
        Hash256 txHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(), BigInteger.TEN.pow(10));
        Await.waitUntilTransactionIsExecuted(txHash, ct.getNeow3j());

        ct.signWithDefaultAccount();
        InvocationResult res = ct.callInvokeFunction(testName).getInvocationResult();
        List<StackItem> amounts = res.getStack().get(0).getList();
        BigInteger before = amounts.get(0).getInteger();
        BigInteger after = amounts.get(1).getInteger();
        assertTrue(after.compareTo(before) > 0);
    }

    @Permission(contract = "d2a4cff31913016155e38e474a2c06d08be276cf")
    static class GasTokenIntegrationTestContract {

        public static Hash160 getHash() {
            return GasToken.getHash();
        }

        public static int[] refuel() {
            Transaction tx = (Transaction) Runtime.getScriptContainer();
            int[] gasAmounts = new int[2];
            gasAmounts[0] = Runtime.getGasLeft();
            // refuel with 100 GAS
            GasToken.refuel(tx.sender, Helper.pow(10, 10));
            gasAmounts[1] = Runtime.getGasLeft();
            return gasAmounts;
        }
    }

}


