package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.AttributeType;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.contracts.PolicyContract;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.policyContractHash;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolicyContractIntegrationTest {

    public static final long FEE_PER_BYTE = 1000L; // GAS fractions
    public static final int DEFAULT_EXEC_FEE_FACTOR = 30;
    public static final int DEFAULT_STORAGE_PRICE = 100000;
    public static final int DEFAULT_ATTRIBUTE_FEE = 0;

    private String testName;

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(PolicyContractIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test
    public void setAndGetFeePerByte() throws IOException {
        int newFee = 1;
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(newFee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(FEE_PER_BYTE)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(newFee)));
    }

    @Test
    public void blockAndUnblockAccountAndIsBlocked() throws Throwable {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction("isBlocked",
                hash160(ct.getDefaultAccount().getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());

        // Block the account
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution("blockAccount",
                hash160(ct.getDefaultAccount().getScriptHash()));
        assertTrue(ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).getBoolean());

        // Check if it was blocked.
        response = ct.callInvokeFunction("isBlocked",
                hash160(ct.getDefaultAccount().getScriptHash()));
        assertTrue(response.getInvocationResult().getStack().get(0).getBoolean());

        // Unblock the account
        txHash = ct.invokeFunctionAndAwaitExecution("unblockAccount",
                hash160(ct.getDefaultAccount().getScriptHash()));
        assertTrue(ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog()
                .getExecutions().get(0).getStack().get(0).getBoolean());

        // Check if it was unblocked.
        response = ct.callInvokeFunction("isBlocked",
                hash160(ct.getDefaultAccount().getScriptHash()));
        assertFalse(response.getInvocationResult().getStack().get(0).getBoolean());
    }

    @Test
    public void setAndGetExecFeeFactor() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(99));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(DEFAULT_EXEC_FEE_FACTOR)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(99)));
    }

    @Test
    public void setAndGetStoragePrice() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(1000000));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(DEFAULT_STORAGE_PRICE)));
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(1000000)));
    }

    @Test
    public void setAndGetMillisecondsPerBlock() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(20000));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(1000))); // Default value
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(20000))); // New value
    }

    @Test
    public void setAndGetMaxValidUntilBlockIncrement() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(7000));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(5760))); // Default value
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(7000))); // New value
    }

    @Test
    public void setAndGetMaxTraceableBlocks() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(2000000));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).getList();
        assertThat(res.get(0).getInteger(), is(BigInteger.valueOf(2102400))); // Default value
        assertThat(res.get(1).getInteger(), is(BigInteger.valueOf(2000000))); // New value
    }

    @Test
    public void setAndGetAttributeFee() throws IOException {
        ct.signWithCommitteeAccount();
        NeoInvokeFunction response = ct.callInvokeFunction(testName, integer(1345_0000));
        List<StackItem> feeList = response.getInvocationResult().getFirstStackItem().getList();
        assertThat(feeList, hasSize(3));
        assertThat(feeList.get(0).getInteger(), is(BigInteger.valueOf(DEFAULT_ATTRIBUTE_FEE)));
        assertThat(feeList.get(1).getInteger(), is(BigInteger.valueOf(1345_0000)));
        assertThat(feeList.get(2).getInteger(), is(BigInteger.valueOf(10_0000_0000)));
    }

    @Test
    public void getHash() throws Throwable {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(policyContractHash())));
    }

    @Permission(nativeContract = NativeContract.PolicyContract, methods = "*")
    static class PolicyContractIntegrationTestContract {

        static PolicyContract policyContract = new PolicyContract();

        public static int[] setAndGetFeePerByte(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = policyContract.getFeePerByte();
            policyContract.setFeePerByte(newFee);
            sizes[1] = policyContract.getFeePerByte();
            return sizes;
        }

        public static boolean blockAccount(Hash160 scriptHash) {
            return policyContract.blockAccount(scriptHash);
        }

        public static boolean isBlocked(Hash160 scriptHash) {
            return policyContract.isBlocked(scriptHash);
        }

        public static boolean unblockAccount(Hash160 scriptHash) {
            return policyContract.unblockAccount(scriptHash);
        }

        public static Hash160 getHash() {
            return policyContract.getHash();
        }

        public static int[] setAndGetExecFeeFactor(int newFactor) {
            int[] factors = new int[2];
            factors[0] = policyContract.getExecFeeFactor();
            policyContract.setExecFeeFactor(newFactor);
            factors[1] = policyContract.getExecFeeFactor();
            return factors;
        }

        public static int[] setAndGetStoragePrice(int newPrice) {
            int[] prices = new int[2];
            prices[0] = policyContract.getStoragePrice();
            policyContract.setStoragePrice(newPrice);
            prices[1] = policyContract.getStoragePrice();
            return prices;
        }

        public static int[] setAndGetMillisecondsPerBlock(int newMilliseconds) {
            int[] values = new int[2];
            values[0] = policyContract.getMillisecondsPerBlock();
            policyContract.setMillisecondsPerBlock(newMilliseconds);
            values[1] = policyContract.getMillisecondsPerBlock();
            return values;
        }

        public static int[] setAndGetMaxValidUntilBlockIncrement(int newIncrement) {
            int[] values = new int[2];
            values[0] = policyContract.getMaxValidUntilBlockIncrement();
            policyContract.setMaxValidUntilBlockIncrement(newIncrement);
            values[1] = policyContract.getMaxValidUntilBlockIncrement();
            return values;
        }

        public static int[] setAndGetMaxTraceableBlocks(int newBlocks) {
            int[] values = new int[2];
            values[0] = policyContract.getMaxTraceableBlocks();
            policyContract.setMaxTraceableBlocks(newBlocks);
            values[1] = policyContract.getMaxTraceableBlocks();
            return values;
        }

        public static int[] setAndGetAttributeFee(int newFeeConflicts) {
            int[] fees = new int[3];
            fees[0] = policyContract.getAttributeFee(AttributeType.Conflicts);
            policyContract.setAttributeFee(AttributeType.Conflicts, newFeeConflicts);
            fees[1] = policyContract.getAttributeFee(AttributeType.Conflicts);
            policyContract.setAttributeFee(AttributeType.OracleResponse, PolicyContract.maxAttributeFee);
            fees[2] = policyContract.getAttributeFee(AttributeType.OracleResponse);
            return fees;
        }

    }

}
