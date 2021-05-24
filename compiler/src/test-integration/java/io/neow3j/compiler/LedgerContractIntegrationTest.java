package io.neow3j.compiler;

import io.neow3j.devpack.Block;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.contracts.LedgerContract;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.utils.Numeric;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.test.TestProperties.ledgerContractHash;
import static io.neow3j.types.ContractParameter.hash256;
import static io.neow3j.types.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LedgerContractIntegrationTest {

    private static NeoBlock blockOfDeployTx;

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            LedgerContractIntegrationTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        blockOfDeployTx = ct.getNeow3j().getBlock(ct.getBlockHashOfDeployTx(), true)
                .send().getBlock();
    }

    @Test
    public void getTransactionHeight() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(ct.getDeployTxHash()));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().longValue(),
                is(blockOfDeployTx.getIndex()));
    }

    @Test
    public void getTransactionHeightOfNonExistentTransaction() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction("getTransactionHeight",
                hash256("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(
                response.getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(-1));
    }

    @Test
    public void getTransactionFromBlock() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                integer(BigInteger.valueOf(blockOfDeployTx.getIndex())), integer(0));
        List<StackItem> tx = response.getInvocationResult().getStack().get(0).getList();
        assertThat(tx.get(0).getHexString(),
                is(Numeric.reverseHexString(ct.getDeployTxHash().toString())));
        assertThat(tx.get(1).getInteger().intValue(), is(0)); // version
        assertThat(tx.get(2).getInteger().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).getAddress(), is(ct.getCommittee().getAddress())); // sender
        assertThat(tx.get(4).getInteger().intValue(), greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).getInteger().intValue(), greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).getInteger().longValue(),
                greaterThanOrEqualTo(new Neow3jConfig().getMaxValidUntilBlockIncrement()));
        assertThat(tx.get(7).getHexString().length(), greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getTransactionFromBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash256(ct.getBlockHashOfDeployTx()), integer(0));
        List<StackItem> tx = response.getInvocationResult().getStack().get(0).getList();
        assertThat(tx.get(0).getHexString(),
                is(Numeric.reverseHexString(ct.getDeployTxHash().toString())));
        assertThat(tx.get(1).getInteger().intValue(), is(0)); // version
        assertThat(tx.get(2).getInteger().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).getAddress(), is(ct.getCommittee().getAddress())); // sender
        assertThat(tx.get(4).getInteger().intValue(), greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).getInteger().intValue(), greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).getInteger().longValue(),
                greaterThanOrEqualTo(new Neow3jConfig().getMaxValidUntilBlockIncrement()));
        assertThat(tx.get(7).getHexString().length(), greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getTransaction() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(ct.getDeployTxHash()));
        List<StackItem> tx = response.getInvocationResult().getStack().get(0).getList();
        assertThat(tx.get(0).getHexString(),
                is(Numeric.reverseHexString(ct.getDeployTxHash().toString())));
        assertThat(tx.get(1).getInteger().intValue(), is(0)); // version
        assertThat(tx.get(2).getInteger().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).getAddress(), is(ct.getCommittee().getAddress())); // sender
        assertThat(tx.get(4).getInteger().intValue(), greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).getInteger().intValue(), greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).getInteger().longValue(),
                greaterThanOrEqualTo(new Neow3jConfig().getMaxValidUntilBlockIncrement()));
        assertThat(tx.get(7).getHexString().length(), greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getNonExistentTransaction() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, hash256(
                "0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(response.getInvocationResult().getStack().get(0).getInteger().intValue(), is(1));
    }

    @Test
    public void getBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                hash256(ct.getBlockHashOfDeployTx()));

        List<StackItem> block = response.getInvocationResult().getStack().get(0).getList();
        assertThat(block.get(0).getHexString(),
                is(Numeric.reverseHexString(ct.getBlockHashOfDeployTx().toString())));
        assertThat(block.get(1).getInteger().intValue(), is(blockOfDeployTx.getVersion()));
        assertThat(block.get(2).getHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getPrevBlockHash().toString())));
        assertThat(block.get(3).getHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getMerkleRootHash().toString())));
        assertThat(block.get(4).getInteger().longValue(), is(blockOfDeployTx.getTime()));
        assertThat(block.get(5).getInteger().longValue(), is(blockOfDeployTx.getIndex()));
        assertThat(block.get(6).getInteger().intValue(), is(blockOfDeployTx.getPrimary()));
        assertThat(block.get(7).getAddress(), is(blockOfDeployTx.getNextConsensus()));
        assertThat(block.get(8).getInteger().intValue(),
                is(blockOfDeployTx.getTransactions().size()));
    }

    @Test
    public void getBlockWithBlockNumber() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName,
                integer(BigInteger.valueOf(blockOfDeployTx.getIndex())));

        List<StackItem> block = response.getInvocationResult().getStack().get(0).getList();
        assertThat(block.get(0).getHexString(),
                is(Numeric.reverseHexString(ct.getBlockHashOfDeployTx().toString())));
        assertThat(block.get(1).getInteger().intValue(), is(blockOfDeployTx.getVersion()));
        assertThat(block.get(2).getHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getPrevBlockHash().toString())));
        assertThat(block.get(3).getHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getMerkleRootHash().toString())));
        assertThat(block.get(4).getInteger().longValue(), is(blockOfDeployTx.getTime()));
        assertThat(block.get(5).getInteger().longValue(), is(blockOfDeployTx.getIndex()));
        assertThat(block.get(6).getInteger().intValue(), is(blockOfDeployTx.getPrimary()));
        assertThat(block.get(7).getAddress(), is(blockOfDeployTx.getNextConsensus()));
        assertThat(block.get(8).getInteger().intValue(),
                is(blockOfDeployTx.getTransactions().size()));
    }

    @Test
    public void currentIndex() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        BigInteger height = response.getInvocationResult().getStack().get(0).getInteger();
        assertThat(height.intValue(), greaterThanOrEqualTo(0));
    }

    @Test
    public void currentHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        byte[] hash = response.getInvocationResult().getStack().get(0).getByteArray();
        assertThat(hash.length, is(32));
    }

    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(ledgerContractHash())));
    }

    static class LedgerContractIntegrationTestContract {

        public static int getTransactionHeight(Hash256 blockHash) {
            return LedgerContract.getTransactionHeight(blockHash);
        }

        public static Object getTransactionFromBlock(int blockNr, int txNr) {
            return LedgerContract.getTransactionFromBlock(blockNr, txNr);
        }

        public static Object getTransactionFromBlockWithBlockHash(Hash256 blockHash, int txNr) {
            return LedgerContract.getTransactionFromBlock(blockHash, txNr);
        }

        public static Object getTransaction(Hash256 txHash) {
            return LedgerContract.getTransaction(txHash);
        }

        public static boolean getNonExistentTransaction(Hash256 txHash) {
            if (LedgerContract.getTransaction(txHash) == null) {
                return true;
            }
            return false;
        }

        public static Block getBlockWithBlockHash(Hash256 blockHash) {
            return LedgerContract.getBlock(blockHash);
        }

        public static Block getBlockWithBlockNumber(int blockNr) {
            return LedgerContract.getBlock(blockNr);
        }

        public static int currentIndex() {
            return LedgerContract.currentIndex();
        }

        public static Hash256 currentHash() {
            return LedgerContract.currentHash();
        }

        public static Hash160 getHash() {
            return LedgerContract.getHash();
        }
    }

}
