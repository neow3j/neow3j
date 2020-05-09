package io.neow3j.protocol;

import static io.neow3j.protocol.TestHelper.NEO_HASH;
import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.NeoWitness;
import io.neow3j.protocol.core.methods.response.TransactionAttribute;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

// This test class uses a static container which is reused in every test to avoid the long startup
// time of the container. Therefore only tests that perform read-only operations should be added
// here.
public class Neow3jReadOnlyIntegrationTest extends Neow3jIntegrationTest {

    @ClassRule
    public static GenericContainer privateNetContainer = new GenericContainer(PRIVNET_CONTAINER)
            .withExposedPorts(EXPOSED_INTERNAL_PORT_NEO_DOTNET)
            .waitingFor(Wait.forListeningPort());

    @Override
    protected GenericContainer getPrivateNetContainer() {
        return privateNetContainer;
    }

    @Test
    public void testGetVersion() throws IOException {
        NeoGetVersion version = getNeow3j().getVersion().send();
        NeoGetVersion.Result versionResult = version.getVersion();
        assertNotNull(versionResult);
        assertThat(versionResult.getUserAgent(), not(isEmptyString()));
        assertThat(versionResult.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(versionResult.getPort(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetBestBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = getNeow3j().getBestBlockHash().send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = getNeow3j().getBlockHash(new BlockParameterIndex(1)).send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetConnectionCount() throws IOException {
        NeoConnectionCount getConnectionCount = getNeow3j().getConnectionCount().send();
        Integer connectionCount = getConnectionCount.getCount();
        assertNotNull(connectionCount);
        assertThat(connectionCount, greaterThanOrEqualTo(0));
    }

    @Test
    public void testListAddress() throws IOException {
        NeoListAddress listAddress = getNeow3j().listAddress().send();
        List<Address> addresses = listAddress.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetPeers() throws IOException {
        NeoGetPeers getPeers = getNeow3j().getPeers().send();
        NeoGetPeers.Peers peers = getPeers.getPeers();
        assertNotNull(peers);
        assertThat(peers.getBad(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getConnected(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getUnconnected(), hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetRawMemPool() throws IOException {
        NeoGetRawMemPool getRawMemPool = getNeow3j().getRawMemPool().send();
        List<String> addresses = getRawMemPool.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetValidators() throws IOException {
        NeoGetValidators getValidators = getNeow3j().getValidators().send();
        List<NeoGetValidators.Validator> validators = getValidators.getValidators();
        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testValidateAddress() throws IOException {
        NeoValidateAddress validateAddress = getNeow3j().validateAddress(ADDRESS_1).send();
        NeoValidateAddress.Result validation = validateAddress.getValidation();
        assertNotNull(validation);
        assertThat(validation.getValid(), is(true));
        assertThat(validation.getAddress(), is(ADDRESS_1));
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_2001_IDX), true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_2001_IDX));
        assertThat(block.getTransactions(), not(empty()));
    }

    @Test
    public void testGetBlock_Index() throws IOException {
        NeoGetBlock neoGetBlock = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_2001_IDX), false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), equalTo(BLOCK_2001_IDX));
        assertThat(block.getTransactions(), is(nullValue()));
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        NeoGetRawBlock neoGetRawBlock = getNeow3j()
                .getRawBlock(new BlockParameterIndex(BLOCK_2001_IDX))
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = getNeow3j()
                .getBlock(BLOCK_2001_HASH, true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), equalTo(BLOCK_2001_IDX));
        assertThat(block.getTransactions(), not(empty()));
    }

    @Test
    public void testGetBlock_Hash() throws IOException {
        NeoGetBlock neoGetBlock = getNeow3j()
                .getBlock(BLOCK_2001_HASH, false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(BLOCK_2001_IDX));
        assertThat(block.getTransactions(), is(nullValue()));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        NeoGetRawBlock neoGetRawBlock = getNeow3j()
                .getRawBlock(BLOCK_2001_HASH)
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetBlockCount() throws Exception {
        NeoBlockCount neoBlockCount = getNeow3j().getBlockCount().send();
        BigInteger blockIndex = neoBlockCount.getBlockIndex();
        assertNotNull(blockIndex);
        assertThat(neoBlockCount.getBlockIndex(), greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetBlockHeader_Hash() throws IOException {
        NeoBlock block = getNeow3j().getBlockHeader(BLOCK_2001_HASH).send().getBlock();
        assertThat(block, not(nullValue()));
        assertThat(block.getTransactions(), is(nullValue()));
        assertThat(block.getIndex(), is(notNullValue()));
        assertThat(block.getHash(), is(BLOCK_2001_HASH));
    }

    @Test
    public void testGetBlockHeader_Index() throws IOException {
        NeoBlock block = getNeow3j().getBlockHeader(new BlockParameterIndex(BLOCK_2001_IDX)).send()
                .getBlock();
        assertThat(block.getTransactions(), is(nullValue()));
        assertThat(block.getIndex(), is(BLOCK_2001_IDX));
        assertThat(block.getHash(), is(BLOCK_2001_HASH));
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws IOException {
        NeoGetRawBlock getRawBlockHeader = getNeow3j().getRawBlockHeader(BLOCK_2001_HASH).send();
        assertThat(getRawBlockHeader.getRawBlock(), is(notNullValue()));
        assertThat(getRawBlockHeader.getRawBlock(), is(BLOCK_2001_RAW_STRING));
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        NeoGetRawBlock getRawBlockHeader = getNeow3j()
                .getRawBlockHeader(new BlockParameterIndex(BLOCK_2001_IDX)).send();
        assertThat(getRawBlockHeader.getRawBlock(), is(notNullValue()));
        assertThat(getRawBlockHeader.getRawBlock(), is(BLOCK_2001_RAW_STRING));
    }

    @Test
    public void testGetWalletHeight() throws IOException {
        NeoGetWalletHeight getWalletHeight = getNeow3j().getWalletHeight().send();
        BigInteger height = getWalletHeight.getHeight();
        assertNotNull(height);
        assertTrue(height.longValueExact() > 0);
    }

    @Test
    public void testGetBlockSysFee() throws IOException {
        NeoGetBlockSysFee getBlockSysFee = getNeow3j()
                .getBlockSysFee(new BlockParameterIndex(BigInteger.ONE)).send();
        String fee = getBlockSysFee.getFee();
        assertThat(fee, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetTransaction() throws IOException {
        NeoGetTransaction getTransaction = getNeow3j().getTransaction(UTXO_TX_HASH).send();
        assertThat(getTransaction.getTransaction(), is(notNullValue()));
        assertThat(
                getTransaction.getTransaction().getTransactionId(),
                is(UTXO_TX_HASH)
        );
        assertThat(
                getTransaction.getTransaction().getSize(),
                is(223L)
        );
        assertThat(
                getTransaction.getTransaction().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                getTransaction.getTransaction().getVersion(),
                is(0)
        );
        assertThat(
                getTransaction.getTransaction().getAttributes(),
                hasItem(
                        new TransactionAttribute(TransactionAttributeUsageType.SCRIPT,
                                ADDR1_SCRIPT_HASH)
                )
        );
        assertThat(
                getTransaction.getTransaction().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEO_HASH.toString()),
                                ADDR1_INIT_NEO_BALANCE, ADDRESS_1)
                )
        );
        assertThat(
                getTransaction.getTransaction().getInputs(),
                hasItem(
                        new TransactionInput(
                                "0x83df8bd085fcb60b2789f7d0a9f876e5f3908567f7877fcba835e899b9dea0b5",
                                0)
                )
        );
        assertThat(
                getTransaction.getTransaction().getSysFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getNetFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getScripts(),
                hasItems(
                        new NeoWitness(
                                "40a3799c78dec17823fde75233793a7039bf2b1dbca4383a6eef1ac829460ba14c1e6a50ab1f2174e689bebfc0bb7accc965a6fe3e46d517b317bba1325b7fdaca",
                                "21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
                )
        );
        assertThat(
                getTransaction.getTransaction().getBlockHash(),
                is(BLOCK_2008_HASH)
        );
        assertThat(
                getTransaction.getTransaction().getConfirmations(),
                greaterThanOrEqualTo(1L)
        );
        assertThat(
                getTransaction.getTransaction().getBlockTime(),
                greaterThanOrEqualTo(1547956859L)
        );
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        NeoGetRawTransaction getRawTransaction = getNeow3j().getRawTransaction(UTXO_TX_HASH).send();
        assertThat(getRawTransaction.getRawTransaction(),
                is("8000012023ba2703c53263e8d6e522dc32203339dcd8eee901b5a0deb999e835a8cb7f87f7678590f3e576f8a9d0f789270bb6fc85d08bdf830000019b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50000c16ff286230023ba2703c53263e8d6e522dc32203339dcd8eee9014140a3799c78dec17823fde75233793a7039bf2b1dbca4383a6eef1ac829460ba14c1e6a50ab1f2174e689bebfc0bb7accc965a6fe3e46d517b317bba1325b7fdaca2321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac"));
    }

    @Test
    public void testGetBalance() throws IOException {
        NeoGetBalance getBalance = getNeow3j().getBalance(NEO_HASH.toString()).send();
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(Integer.parseInt(getBalance.getBalance().getConfirmed()),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(getBalance.getBalance().getConfirmed()),
                is(lessThanOrEqualTo(TOTAL_NEO_SUPPLY)));
        assertThat(Integer.parseInt(getBalance.getBalance().getBalance()),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(getBalance.getBalance().getBalance()),
                is(lessThanOrEqualTo(TOTAL_NEO_SUPPLY)));
    }

    @Test
    public void testDumpPrivKey() throws IOException {
        NeoDumpPrivKey neoDumpPrivKey = getNeow3j().dumpPrivKey(ADDRESS_1).send();
        String privKey = neoDumpPrivKey.getDumpPrivKey();
        assertThat(privKey, not(isEmptyOrNullString()));
        assertThat(privKey, is(ADDR1_WIF));
    }

    @Test
    public void testGetStorage() throws IOException {
        // TODO: 2019-02-28 Guil:
        // to be implemented
    }

    @Test
    public void testGetContractState() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    @Test
    public void testGetNep5Balances() throws IOException {
        // TODO 2019-08-08 claude:
        // Implement
    }

    @Test
    public void testGetClaimable() throws IOException {
        // TODO: 2019-05-31 Claude:
        // Implement
    }

    @Test
    public void testListInputs() throws IOException {
        // TODO: 2019-06-12 Claude:
        // Implement
    }

}
