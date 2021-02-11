package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.constants.NeoConstants;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.contracts.LedgerContract;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;
import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class LedgerContractIntegrationTest extends ContractTest {

    private static NeoBlock blockOfDeployTx;

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(LedgerContractIntegrationTestContract.class.getName());
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
    public void getTransactionHeightOfNonExistentTransaction() throws IOException {
        NeoInvokeFunction response = callInvokeFunction("getTransactionHeight",
                hash256("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(
                response.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
                is(-1));
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
    public void getNonExistentTransaction() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                hash256("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(response.getInvocationResult().getStack().get(0)
                .asInteger().getValue().intValue(), is(1));
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
    public void currentIndex() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        IntegerStackItem height = response.getInvocationResult().getStack().get(0).asInteger();
        assertThat(height.getValue().intValue(), greaterThanOrEqualTo(0));
    }

    @Test
    public void currentHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        ByteStringStackItem hash = response.getInvocationResult().getStack().get(0).asByteString();
        assertThat(hash.getValue().length, is(32));
    }

    @Ignore("Waiting for the implementation of the LedgerContract in the contract module.")
    @Test
    public void getHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        // TODO: Get the script hash of the LedgerContract form the contract module.
//      assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
//              is(io.neow3j.contract.LedgerContract.SCRIPT_HASH.toString()));
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

        public static Object getBlockWithBlockHash(Hash256 blockHash) {
            return LedgerContract.getBlock(blockHash);
        }

        public static Object getBlockWithBlockNumber(int blockNr) {
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
