package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.contracts.PolicyContract;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolicyContractTest extends ContractTest {

    public static final long MAX_BLOCK_SYSTEM_FEE = 9000L * 100_000_000L; // GAS fractions
    public static final long MAX_BLOCK_SIZE = 1024 * 256;
    public static final long MAX_TRANSACTIONS_PER_BLOCK = 512;
    public static final long FEE_PER_BYTE = 1000L; // GAS fractions

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(PolicyContractTestContract.class.getName());
    }

    @Test
    public void setAndGetFeePerByte() throws IOException {
        int newFee = 1;
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(newFee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(FEE_PER_BYTE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(newFee)));
    }

    @Test
    public void setAndGetMaxBlockSystemFee() throws Throwable {
        int fee = 4007601;
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(fee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(MAX_BLOCK_SYSTEM_FEE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(fee)));
    }

    @Test
    public void setAndGetMaxBlockSize() throws IOException {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(1024));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(MAX_BLOCK_SIZE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(1024)));
    }

    @Test
    public void setAndGetMaxTransactionsPerBlock() throws IOException {
        BigInteger newTxPerBlock = BigInteger.ONE;
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction(integer(newTxPerBlock));

        StackItem array = response.getInvocationResult().getStack().get(0);
        StackItem elem1 = array.asArray().get(0);
        assertThat(elem1.asInteger().getValue(),
                is(BigInteger.valueOf(MAX_TRANSACTIONS_PER_BLOCK)));
        StackItem elem2 = array.asArray().get(1);
        assertThat(elem2.asInteger().getValue(), is(newTxPerBlock));
    }

    @Test
    public void blockAndUnblockAccountAndIsBlocked() throws Throwable {
        signAsCommittee();
        NeoInvokeFunction response = callInvokeFunction("isBlocked",
                hash160(defaultAccount.getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).asBoolean().getValue());

        // Block the account
        String txHash = invokeFunctionAndAwaitExecution("blockAccount",
                hash160(defaultAccount.getScriptHash()));
        assertTrue(neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).asBoolean().getValue());

        // Check if it was blocked.
        response = callInvokeFunction("isBlocked", hash160(defaultAccount.getScriptHash()));
        assertTrue(response.getInvocationResult().getStack().get(0).asBoolean().getValue());

        // Unblock the account
        txHash = invokeFunctionAndAwaitExecution("unblockAccount",
                hash160(defaultAccount.getScriptHash()));
        assertTrue(neow3j.getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).asBoolean().getValue());

        // Check if it was unblocked.
        response = callInvokeFunction("isBlocked", hash160(defaultAccount.getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is(io.neow3j.contract.PolicyContract.SCRIPT_HASH.toString()));
    }

    static class PolicyContractTestContract {

        public static int[] setAndGetFeePerByte(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = PolicyContract.getFeePerByte();
            PolicyContract.setFeePerByte(newFee);
            sizes[1] = PolicyContract.getFeePerByte();
            return sizes;
        }

        public static int[] setAndGetMaxBlockSystemFee(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = PolicyContract.getMaxBlockSystemFee();
            if (PolicyContract.setMaxBlockSystemFee(newFee)) {
                sizes[1] = PolicyContract.getMaxBlockSystemFee();
            }
            return sizes;
        }

        public static int[] setAndGetMaxBlockSize(int newSize) {
            int[] sizes = new int[2];
            sizes[0] = PolicyContract.getMaxBlockSize();
            PolicyContract.setMaxBlockSize(newSize);
            sizes[1] = PolicyContract.getMaxBlockSize();
            return sizes;
        }

        public static int[] setAndGetMaxTransactionsPerBlock(int size) {
            int[] sizes = new int[2];
            sizes[0] = PolicyContract.getMaxTransactionsPerBlock();
            PolicyContract.setMaxTransactionsPerBlock(size);
            sizes[1] = PolicyContract.getMaxTransactionsPerBlock();
            return sizes;
        }

        public static boolean blockAccount(byte[] scriptHash) {
            return PolicyContract.blockAccount(scriptHash);
        }

        public static boolean isBlocked(byte[] scriptHash) {
            return PolicyContract.isBlocked(scriptHash);
        }

        public static boolean unblockAccount(byte[] scriptHash) {
            return PolicyContract.unblockAccount(scriptHash);
        }

        public static byte[] getHash() {
            return PolicyContract.getHash();
        }

    }

}


