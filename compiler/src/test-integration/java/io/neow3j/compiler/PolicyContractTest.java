package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.devpack.neo.Policy;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.utils.Numeric;
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
        setUp(PolicyContractUser.class.getName());
    }

    @Test
    public void getName() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();

        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("Policy"));
    }

    @Test
    public void setAndGetFeePerByte() throws IOException {
        int newFee = 1;
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(integer(newFee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(FEE_PER_BYTE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(newFee)));
    }

    @Test
    public void setAndGetMaxBlockSystemFee() throws IOException {
        int fee = 4007601;
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(integer(fee));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(MAX_BLOCK_SYSTEM_FEE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(fee)));
    }

    @Test
    public void setAndGetMaxBlockSize() throws IOException {
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(integer(1024));

        List<StackItem> res = response.getInvocationResult().getStack().get(0).asArray().getValue();
        assertThat(res.get(0).asInteger().getValue(), is(BigInteger.valueOf(MAX_BLOCK_SIZE)));
        assertThat(res.get(1).asInteger().getValue(), is(BigInteger.valueOf(1024)));
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
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(integer(newTxPerBlock));

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
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(hash160(defaultAccount.getScriptHash()));

        StackItem arrayItem = response.getInvocationResult().getStack().get(0);
        assertThat(arrayItem.getType(), is(StackItemType.ARRAY));
        String blockedAccScriptHash = arrayItem.asArray().get(0).asByteString().getAsHexString();
        assertThat(blockedAccScriptHash,
                is(Numeric.toHexStringNoPrefix(defaultAccount.getScriptHash().toArray())));
    }

    @Test
    public void unblockAccount() throws IOException {
        signWithCommitteeMember();
        NeoInvokeFunction response = callInvokeFunction(hash160(defaultAccount.getScriptHash()));
        assertTrue(response.getInvocationResult().getStack().get(0).asBoolean().getValue());
    }

    static class PolicyContractUser {

        public static String getName() {
            return Policy.name();
        }

        public static int[] setAndGetFeePerByte(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = Policy.getFeePerByte();
            Policy.setFeePerByte(newFee);
            sizes[1] = Policy.getFeePerByte();
            return sizes;
        }

        public static int[] setAndGetMaxBlockSystemFee(int newFee) {
            int[] sizes = new int[2];
            sizes[0] = Policy.getMaxBlockSystemFee();
            Policy.setMaxBlockSystemFee(newFee);
            sizes[1] = Policy.getMaxBlockSystemFee();
            return sizes;
        }

        public static int[] setAndGetMaxBlockSize(int newSize) {
            int[] sizes = new int[2];
            sizes[0] = Policy.getMaxBlockSize();
            Policy.setMaxBlockSize(newSize);
            sizes[1] = Policy.getMaxBlockSize();
            return sizes;
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

        public static boolean unblockAccount(byte[] scriptHash) {
            Policy.blockAccount(scriptHash);
            return Policy.unblockAccount(scriptHash);
        }
    }

}


