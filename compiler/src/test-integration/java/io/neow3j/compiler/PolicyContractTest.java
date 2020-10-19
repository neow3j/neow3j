package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.neo.Policy;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolicyContractTest extends CompilerTest {

    public static final long MAX_BLOCK_SYSTEM_FEE = 9000L * 100_000_000L; // GAS fractions
    public static final long MAX_BLOCK_SIZE = 1024 * 256;
    public static final long MAX_TRANSACTIONS_PER_BLOCK = 512;
    public static final long FEE_PER_BYTE = 1000L; // GAS fractions

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(PolicyContractUser.class.getName());
    }

    @Test
    public void getName() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("Policy"));
    }

    @Test
    public void getFeePerByte() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(FEE_PER_BYTE)));
    }

    @Test
    public void getMaxBlockSystemFee() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(MAX_BLOCK_SYSTEM_FEE)));
    }

    @Test
    public void getMaxBlockSize() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(MAX_BLOCK_SIZE)));
    }

    @Test
    public void getMaxTransactionsPerBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asInteger().getValue(),
                is(BigInteger.valueOf(MAX_TRANSACTIONS_PER_BLOCK)));
    }

    @Test
    public void setAndGetMaxTransactionsPerBlock() throws IOException {
        BigInteger newTxPerBlock = BigInteger.ONE;

        NeoInvokeFunction response = callInvokeFunction(
                Signer.calledByEntry(multiSigAcc.getScriptHash()),
                ContractParameter.integer(newTxPerBlock));

        StackItem array = response.getInvocationResult().getStack().get(0);
        assertThat(array.getType(), is(StackItemType.ARRAY));
        StackItem elem1 = array.asArray().get(0);
        assertThat(elem1.getType(), is(StackItemType.INTEGER));
        assertThat(elem1.asInteger().getValue(),
                is(BigInteger.valueOf(MAX_TRANSACTIONS_PER_BLOCK)));
        StackItem elem2 = array.asArray().get(1);
        assertThat(elem2.getType(), is(StackItemType.INTEGER));
        assertThat(elem2.asInteger().getValue(), is(newTxPerBlock));
    }

    @Test
    public void setAndGetBlockedAccounts() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                Signer.calledByEntry(multiSigAcc.getScriptHash()),
                ContractParameter.hash160(account.getScriptHash()));

        StackItem arrayItem = response.getInvocationResult().getStack().get(0);
        assertThat(arrayItem.getType(), is(StackItemType.ARRAY));
        String blockedAccScriptHash = arrayItem.asArray().get(0).asByteString().getAsHexString();
        assertThat(blockedAccScriptHash,
                is(Numeric.toHexStringNoPrefix(account.getScriptHash().toArray())));
    }

    static class PolicyContractUser {

        public static String getName() {
            return Policy.name();
        }

        public static int getFeePerByte() {
            return Policy.getFeePerByte();
        }

        public static int getMaxBlockSystemFee() {
            return Policy.getMaxBlockSystemFee();
        }

        public static int getMaxBlockSize() {
            return Policy.getMaxBlockSize();
        }

        public static int getMaxTransactionsPerBlock() {
            return Policy.getMaxTransactionsPerBlock();
        }

        public static int[] setAndGetMaxTransactionsPerBlock(int size) {
            int[] sizes = new int[2];
            sizes[0] = Policy.getMaxTransactionsPerBlock();
            Policy.setMaxTransactionsPerBlock(size);
            sizes[1] = Policy.getMaxTransactionsPerBlock();
            return sizes;
        }

        public static String[] setAndGetBlockedAccounts(byte[] scriptHash) {
            Policy.blockAccount(scriptHash);
            return Policy.getBlockedAccounts();
        }
    }

}


