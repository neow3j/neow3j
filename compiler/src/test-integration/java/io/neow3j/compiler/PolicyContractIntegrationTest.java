package io.neow3j.compiler;

import static io.neow3j.TestProperties.policyContractHash;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.Hash256;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.PolicyContract;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolicyContractIntegrationTest extends ContractTest {

    public static final long FEE_PER_BYTE = 1000L; // GAS fractions
    public static final int DEFAULT_EXEC_FEE_FACTOR = 30;
    public static final int DEFAULT_STORAGE_PRICE = 100000;

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(PolicyContractIntegrationTestContract.class.getName());
    }

    @Test
    public void setAndGetFeePerByte() throws IOException {
        int newFee = 1;
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(newFee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(FEE_PER_BYTE)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(newFee)));
    }

    @Test
    public void blockAndUnblockAccountAndIsBlocked() throws Throwable {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction("isBlocked",
                hash160(defaultAccount.getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());

        // Block the account
        Hash256 txHash = invokeFunctionAndAwaitExecution("blockAccount",
                hash160(defaultAccount.getScriptHash()));
        assertTrue(neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).getBoolean());

        // Check if it was blocked.
        response = callInvokeFunction("isBlocked", hash160(defaultAccount.getScriptHash()));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());

        // Unblock the account
        txHash = invokeFunctionAndAwaitExecution("unblockAccount",
                hash160(defaultAccount.getScriptHash()));
        assertTrue(neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).getBoolean());

        // Check if it was unblocked.
        response = callInvokeFunction("isBlocked", hash160(defaultAccount.getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void setAndGetExecFeeFactor() throws IOException {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(300));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(),
                is(BigInteger.valueOf(DEFAULT_EXEC_FEE_FACTOR)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(300)));
    }

    @Test
    public void setAndGetStoragePrice() throws IOException {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(1000000));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(),
                is(BigInteger.valueOf(DEFAULT_STORAGE_PRICE)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(1000000)));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(policyContractHash()));
    }

    static class PolicyContractIntegrationTestContract {

        public static int[] setAndGetFeePerByte(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = PolicyContract.getFeePerByte();
            PolicyContract.setFeePerByte(newFee);
            sizes[1] = PolicyContract.getFeePerByte();
            return sizes;
        }

        public static boolean blockAccount(Hash160 scriptHash) {
            return PolicyContract.blockAccount(scriptHash);
        }

        public static boolean isBlocked(Hash160 scriptHash) {
            return PolicyContract.isBlocked(scriptHash);
        }

        public static boolean unblockAccount(Hash160 scriptHash) {
            return PolicyContract.unblockAccount(scriptHash);
        }

        public static Hash160 getHash() {
            return PolicyContract.getHash();
        }

        public static int[] setAndGetExecFeeFactor(int newFactor) {
            int[] factors = new int[2];
            factors[0] = PolicyContract.getExecFeeFactor();
            PolicyContract.setExecFeeFactor(newFactor);
            factors[1] = PolicyContract.getExecFeeFactor();
            return factors;
        }

        public static int[] setAndGetStoragePrice(int newPrice) {
            int[] prices = new int[2];
            prices[0] = PolicyContract.getStoragePrice();
            PolicyContract.setStoragePrice(newPrice);
            prices[1] = PolicyContract.getStoragePrice();
            return prices;
        }
    }

}
