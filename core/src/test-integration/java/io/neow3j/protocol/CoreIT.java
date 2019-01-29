package io.neow3j.protocol;

import io.neow3j.crypto.KeyUtils;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoGetAccountState;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetTxOut;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;

import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CoreIT {

    private static int EXPOSED_INTERNAL_PORT = 30333;
    private static int EXPOSED_INTERNAL_PORT_WALLET = 30337;

    private static int BLOCK_HASH_LENGTH_WITH_PREFIX = 66;

    @ClassRule
    public static GenericContainer privateNetContainer
            = new GenericContainer("axlabs/neo-privatenet-openwallet-docker:latest")
            .withExposedPorts(EXPOSED_INTERNAL_PORT, EXPOSED_INTERNAL_PORT_WALLET)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(1L));

    private Neow3j neow3jOpenWallet;
    private Neow3j neow3jDotNet;

    @Before
    public void setup() {
        neow3jOpenWallet = Neow3j.build(new HttpService(getPrivateNetHost(EXPOSED_INTERNAL_PORT_WALLET)));
        neow3jDotNet = Neow3j.build(new HttpService(getPrivateNetHost(EXPOSED_INTERNAL_PORT)));
    }

    @Test
    public void testGetVersion() throws IOException {
        NeoGetVersion version = neow3jDotNet.getVersion().send();
        NeoGetVersion.Result versionResult = version.getVersion();
        assertNotNull(versionResult);
        assertThat(versionResult.getUserAgent(), not(isEmptyString()));
        assertThat(versionResult.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(versionResult.getPort(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetBestBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = neow3jDotNet.getBestBlockHash().send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = neow3jDotNet.getBlockHash(new BlockParameterIndex(1)).send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetConnectionCount() throws IOException {
        NeoConnectionCount getConnectionCount = neow3jDotNet.getConnectionCount().send();
        Integer connectionCount = getConnectionCount.getCount();
        assertNotNull(connectionCount);
        assertThat(connectionCount, greaterThan(1));
    }

    @Test
    public void testListAddress() throws IOException {
        NeoListAddress listAddress = neow3jDotNet.listAddress().send();
        List<NeoListAddress.Address> addresses = listAddress.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetPeers() throws IOException {
        NeoGetPeers getPeers = neow3jDotNet.getPeers().send();
        NeoGetPeers.Peers peers = getPeers.getPeers();
        assertNotNull(peers);
        assertThat(peers.getBad(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getConnected(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getUnconnected(), hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetRawMemPool() throws IOException {
        NeoGetRawMemPool getRawMemPool = neow3jDotNet.getRawMemPool().send();
        List<String> addresses = getRawMemPool.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetValidators() throws IOException {
        NeoGetValidators getValidators = neow3jDotNet.getValidators().send();
        List<NeoGetValidators.Validator> validators = getValidators.getValidators();
        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testValidateAddress() throws IOException {
        NeoValidateAddress validateAddress = neow3jDotNet.validateAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").send();
        NeoValidateAddress.Result validation = validateAddress.getValidation();
        assertNotNull(validation);
        assertThat(validation.getValid(), is(true));
        assertThat(validation.getAddress(), is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"));
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = neow3jDotNet
                .getBlock(new BlockParameterIndex(BigInteger.ONE), true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(1L));
        assertThat(block.getTransactions(), not(empty()));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetBlock_Index() throws IOException {
        NeoGetBlock neoGetBlock = neow3jDotNet
                .getBlock(new BlockParameterIndex(BigInteger.ONE), false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThan(1L));
        assertThat(block.getTransactions(), is(empty()));
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        NeoGetRawBlock neoGetRawBlock = neow3jDotNet
                .getRawBlock(new BlockParameterIndex(BigInteger.ONE))
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = neow3jDotNet
                .getBlock("0x83973124067c2814b5b979bf259f4a038cc6b5c42f10473902b9a0e262ca8d07", true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(1L));
        assertThat(block.getTransactions(), not(empty()));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetBlock_Hash() throws IOException {
        NeoGetBlock neoGetBlock = neow3jDotNet
                .getBlock("0x83973124067c2814b5b979bf259f4a038cc6b5c42f10473902b9a0e262ca8d07", false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(1L));
        assertThat(block.getTransactions(), is(empty()));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        NeoGetRawBlock neoGetRawBlock = neow3jDotNet
                .getRawBlock("0x83973124067c2814b5b979bf259f4a038cc6b5c42f10473902b9a0e262ca8d07")
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetBlockCount() throws Exception {
        NeoBlockCount neoBlockCount = neow3jDotNet.getBlockCount().send();
        BigInteger blockIndex = neoBlockCount.getBlockIndex();
        assertNotNull(blockIndex);
        assertThat(neoBlockCount.getBlockIndex(), greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetAccountState() throws IOException {
        NeoGetAccountState getAccountState = neow3jDotNet
                .getAccountState("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").send();
        NeoGetAccountState.State accountState = getAccountState.getAccountState();
        assertNotNull(accountState);
        assertThat(accountState.getVotes(), is(empty()));
        assertThat(accountState.getFrozen(), is(false));
        assertThat(accountState.getVersion(), is(0));
        assertThat(accountState.getBalances(), hasSize(2));
        assertThat(accountState.getBalances(), hasItems(
                new NeoGetAccountState.Balance(prependHexPrefix(NEOAsset.HASH_ID), "100000000"),
                new NeoGetAccountState.Balance(prependHexPrefix(GASAsset.HASH_ID), "16024")
        ));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetBlockHeader_Hash() throws IOException {
        NeoBlock block = getNeoBlockHeaderHash(getNeoBestBlockHash().getBlockHash()).getBlock();
        assertThat(block.getTransactions(), is(empty()));
        assertThat(block.getIndex(), is(notNullValue()));
        assertThat(block.getHash(), is("0x83973124067c2814b5b979bf259f4a038cc6b5c42f10473902b9a0e262ca8d07"));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetBlockHeader_Index() throws IOException {
        NeoBlock block = getNeoBlockHeaderIndex(BigInteger.valueOf(1)).getBlock();
        assertThat(block.getTransactions(), is(empty()));
        assertThat(block.getIndex(), is(1L));
        assertThat(block.getHash(), is("0x83973124067c2814b5b979bf259f4a038cc6b5c42f10473902b9a0e262ca8d07"));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetRawBlockHeader_Hash() throws IOException {

    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetRawBlockHeader_Index() throws IOException {

    }

    @Test
    public void testGetNewAddress() throws IOException {
        NeoGetNewAddress getNewAddress = neow3jDotNet.getNewAddress().send();
        String address = getNewAddress.getAddress();
        assertNotNull(address);
        assertThat(address.length(), is(KeyUtils.ADDRESS_SIZE));
    }

    @Test
    @Ignore // re-enable this once version 2.9.0 comes to neo-privatenet docker image
    public void testGetWalletHeight() throws IOException {
        NeoGetWalletHeight getWalletHeight = neow3jDotNet.getWalletHeight().send();
        BigInteger height = getWalletHeight.getHeight();
        assertNotNull(height);
        assertTrue(height.longValueExact() > 0);
    }

    @Test
    public void testGetBlockSysFee() throws IOException {
        NeoGetBlockSysFee getBlockSysFee = neow3jDotNet
                .getBlockSysFee(new BlockParameterIndex(BigInteger.ONE)).send();
        String fee = getBlockSysFee.getFee();
        assertThat(fee, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetTxOut() throws IOException {
        NeoGetTxOut getTxOut = neow3jDotNet
                .getTxOut("0xc2f7fac79531d94d406367c7feafe425f893a580fa703c7b4df9572f5944df5a", 0)
                .send();
        TransactionOutput tx = getTxOut.getTransaction();
        assertNotNull(tx);
        assertThat(tx.getIndex(), is(0));
        assertThat(tx.getAssetId(), is(prependHexPrefix(GASAsset.HASH_ID)));
        assertThat(tx.getAddress(), not(isEmptyOrNullString()));
        assertThat(tx.getValue(), not(isEmptyOrNullString()));
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction neoSendRawTransaction = neow3jDotNet
                .sendRawTransaction("80000001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b00000000295f83f83fc439f56e6e1fb062d89c6f538263d79b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500362634f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141402c797867e0a4d670876fc11c5ba09ea0690ae1d8622e16e6466fcdc7a9689aff8c1b364ab0774d19c64c4349bc2f4cc8c46e40816636f5cb24efb24e92d81b312321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
                .send();
        Boolean sendRawTransaction = neoSendRawTransaction.getSendRawTransaction();
        assertThat(sendRawTransaction, is(true));
    }

    @Test
    @Ignore("TBD")
    public void testSendToAddress() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendToAddress_Fee() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendToAddress_Fee_And_ChangeAddress() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testGetTransaction() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testGetRawTransaction() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testGetBalance() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendMany() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendMany_Empty_Transaction() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendMany_Fee() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testSendMany_Fee_And_ChangeAddress() throws IOException {

    }

    @Test
    @Ignore("TBD")
    public void testDumpPrivKey() throws IOException {

    }

    private NeoBlockHash getNeoBestBlockHash() throws IOException {
        return neow3jDotNet
                .getBestBlockHash()
                .send();
    }

    private NeoGetBlock getNeoBlockHeaderHash(String blockHash) throws IOException {
        return neow3jDotNet
                .getBlockHeader(blockHash)
                .send();
    }

    private NeoGetBlock getNeoBlockHeaderIndex(BigInteger index) throws IOException {
        return neow3jDotNet
                .getBlockHeader(new BlockParameterIndex(index))
                .send();
    }

    private String getPrivateNetHost(int port) {
        return "http://"
                + privateNetContainer.getContainerIpAddress()
                + ":"
                + privateNetContainer.getMappedPort(port);
    }

}
