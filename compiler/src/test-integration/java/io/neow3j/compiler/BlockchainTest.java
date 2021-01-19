package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.constants.NeoConstants;
import io.neow3j.devpack.neo.Blockchain;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockchainTest extends ContractTest {

    private static NeoBlock blockOfDeployTx;

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(BlockchainTestContract.class.getName());
        blockOfDeployTx = neow3j.getBlock(blockHashOfDeployTx, true).send().getBlock();
    }

    @Test
    public void getTransactionHeight() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(deployTxHash));
        assertThat(
                response.getInvocationResult().getStack().get(0).asInteger().getValue().longValue(),
                is(blockOfDeployTx.getIndex()));
    }

    @Test
    public void getTransactionFromBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                integer(BigInteger.valueOf(blockOfDeployTx.getIndex())), integer(0));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(deployTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(),
                is(committee.getAddress())); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT));
        assertThat(tx.get(7).asByteString().getAsHexString().length(),
                greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getTransactionFromBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(blockHashOfDeployTx), integer(0));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(deployTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(),
                is(committee.getAddress())); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT));
        assertThat(tx.get(7).asByteString().getAsHexString().length(),
                greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getTransaction() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(deployTxHash));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(deployTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().longValue(), greaterThanOrEqualTo(1L)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(),
                is(committee.getAddress())); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(1)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(),
                greaterThanOrEqualTo(NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT));
        assertThat(tx.get(7).asByteString().getAsHexString().length(),
                greaterThanOrEqualTo(1)); // script
    }

    @Test
    public void getBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(blockHashOfDeployTx));

        ArrayStackItem block = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(block.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockHashOfDeployTx)));
        assertThat(block.get(1).asInteger().getValue().intValue(),
                is(blockOfDeployTx.getVersion()));
        assertThat(block.get(2).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getPrevBlockHash())));
        assertThat(block.get(3).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getMerkleRootHash())));
        assertThat(block.get(4).asInteger().getValue().longValue(),
                is(blockOfDeployTx.getTime()));
        assertThat(block.get(5).asInteger().getValue().longValue(),
                is(blockOfDeployTx.getIndex()));
        assertThat(block.get(6).asByteString().getAsAddress(),
                is(blockOfDeployTx.getNextConsensus()));
        assertThat(block.get(7).asInteger().getValue().intValue(),
                is(blockOfDeployTx.getTransactions().size()));
    }

    @Test
    public void getBlockWithBlockNumber() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                integer(BigInteger.valueOf(blockOfDeployTx.getIndex())));

        ArrayStackItem block = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(block.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockHashOfDeployTx)));
        assertThat(block.get(1).asInteger().getValue().intValue(),
                is(blockOfDeployTx.getVersion()));
        assertThat(block.get(2).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getPrevBlockHash())));
        assertThat(block.get(3).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(blockOfDeployTx.getMerkleRootHash())));
        assertThat(block.get(4).asInteger().getValue().longValue(),
                is(blockOfDeployTx.getTime()));
        assertThat(block.get(5).asInteger().getValue().longValue(),
                is(blockOfDeployTx.getIndex()));
        assertThat(block.get(6).asByteString().getAsAddress(),
                is(blockOfDeployTx.getNextConsensus()));
        assertThat(block.get(7).asInteger().getValue().intValue(),
                is(blockOfDeployTx.getTransactions().size()));
    }

    @Test
    public void getHeight() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        IntegerStackItem height = response.getInvocationResult().getStack().get(0).asInteger();
        assertThat(height.getValue().intValue(), greaterThanOrEqualTo(0));
    }

    static class BlockchainTestContract {

        public static int getTransactionHeight(byte[] blockHash) {
            return Blockchain.getTransactionHeight(blockHash);
        }

        public static Object getTransactionFromBlock(int blockNr, int txNr) {
            return Blockchain.getTransactionFromBlock(blockNr, txNr);
        }

        public static Object getTransactionFromBlockWithBlockHash(byte[] blockHash, int txNr) {
            return Blockchain.getTransactionFromBlock(blockHash, txNr);
        }

        public static Object getTransaction(byte[] txHash) {
            return Blockchain.getTransaction(txHash);
        }

        public static Object getBlockWithBlockHash(byte[] blockHash) {
            return Blockchain.getBlock(blockHash);
        }

        public static Object getBlockWithBlockNumber(int blockNr) {
            return Blockchain.getBlock(blockNr);
        }

        public static int getHeight() {
            return Blockchain.getHeight();
        }
    }
}
