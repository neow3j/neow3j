package io.neow3j.protocol;

import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.*;
import io.neow3j.transaction.WitnessScope;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

// This test class uses a static container which is reused in every test to avoid the long startup
// time of the container. Therefore only tests that perform read-only operations should be added
// here.
public class Neow3jReadOnlyIntegrationTest extends Neow3jIntegrationTest {

    @ClassRule
    public static GenericContainer privateNetContainer = new GenericContainer(NEO3_PRIVATENET_CONTAINER_IMG)
            .withClasspathResourceMapping("/node-config/config.json",
                    "/neo-cli/config.json", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/node-config/protocol.json",
                    "/neo-cli/protocol.json", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/node-config/wallet.json",
                    "/neo-cli/wallet.json", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/node-config/rpcserver.config.json",
                    "/neo-cli/Plugins/RpcServer/config.json", BindMode.READ_ONLY)
            .withExposedPorts(EXPOSED_JSONRPC_PORT)
            .waitingFor(Wait.forListeningPort());

    @Override
    protected GenericContainer getPrivateNetContainer() {
        return privateNetContainer;
    }

    // Blockchain Methods

    @Test
    public void testGetBestBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = getNeow3j().getBestBlockHash().send();
        String blockHash = getBestBlockHash.getBlockHash();

        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetBlockHash() throws IOException {
        NeoBlockHash getBlockHash = getNeow3j().getBlockHash(new BlockParameterIndex(1)).send();
        String blockHash = getBlockHash.getBlockHash();

        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetBlock_Index() throws IOException {
        NeoGetBlock getBlock = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_0_IDX), false)
                .send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertNull(block.getTransactions());
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoGetBlock getBlock = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_0_IDX), true)
                .send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertNotNull(block.getTransactions());
        assertFalse(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoGetBlock getBlock = getNeow3j().getBlock(BLOCK_0_HASH, true).send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(BLOCK_0_IDX));
        assertNotNull(block.getTransactions());
        assertFalse(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlock(new BlockParameterIndex(BLOCK_0_IDX)).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlock(BLOCK_0_HASH).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetBlockCount() throws IOException {
        NeoBlockCount blockCount = getNeow3j().getBlockCount().send();
        BigInteger blockIndex = blockCount.getBlockIndex();

        assertNotNull(blockIndex);
        assertThat(blockCount.getBlockIndex(), greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetBlockHeader_Hash() throws IOException {
        NeoGetBlock getBlock = getNeow3j().getBlockHeader(BLOCK_0_HASH).send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetBlockHeader_Index() throws IOException {
        NeoGetBlock getBlock = getNeow3j().getBlockHeader(new BlockParameterIndex(BLOCK_0_IDX)).send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlockHeader(BLOCK_0_HASH).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, is(notNullValue()));
        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlockHeader(new BlockParameterIndex(BLOCK_0_IDX)).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, is(notNullValue()));
        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetContractState() throws IOException {
        NeoGetContractState getContractState = getNeow3j().getContractState(NEO_HASH).send();
        NeoGetContractState.ContractState contractState = getContractState.getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-1));
        assertThat(contractState.getHash(), is(NEO_HASH_WITH_PREFIX));
        assertThat(contractState.getScript(), is("QUXEkoQ="));

        NeoGetContractState.ContractState.ContractManifest manifest = contractState.getManifest();
        assertThat(manifest, is(notNullValue()));
        assertThat(manifest.getGroups(), is(notNullValue()));
        assertThat(manifest.getGroups(), hasSize(0));
        assertThat(manifest.getFeatures(), is(notNullValue()));
        assertTrue(manifest.getFeatures().getStorage());
        assertFalse(manifest.getFeatures().getPayable());

        NeoGetContractState.ContractState.ContractManifest.ContractABI abi = manifest.getAbi();
        assertThat(abi, is(notNullValue()));
        assertThat(abi.getHash(), is("0x9bde8f209c88dd0e7ca3bf0af0f476cdd8207789"));

        assertThat(abi.getMethods(), is(notNullValue()));
        assertThat(abi.getMethods(), hasSize(16));
        assertThat(abi.getMethods().get(1).getName(), is("registerCandidate"));
        assertThat(abi.getMethods().get(1).getParameters().get(0).getParamName(), is("pubkey"));
        assertThat(abi.getMethods().get(1).getParameters().get(0).getParamType(),
                is(ContractParameterType.PUBLIC_KEY));
        assertThat(abi.getMethods().get(1).getReturnType(), is(ContractParameterType.BOOLEAN));

        assertThat(abi.getEvents(), is(notNullValue()));
        assertThat(abi.getEvents(), hasSize(1));
        assertThat(abi.getEvents().get(0).getName(), is("Transfer"));
        assertThat(abi.getEvents().get(0).getParameters(), hasSize(3));
        assertThat(abi.getEvents().get(0).getParameters().get(0).getParamName(), is("from"));
        assertThat(abi.getEvents().get(0).getParameters().get(0).getParamType(), is(ContractParameterType.HASH160));
        assertThat(abi.getEvents().get(0).getReturnType(), is(ContractParameterType.SIGNATURE));

        assertThat(manifest.getPermissions(), is(notNullValue()));
        assertThat(manifest.getPermissions(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getMethods().get(0), is("*"));

        assertThat(manifest.getTrusts(), is(notNullValue()));
        assertThat(manifest.getTrusts(), hasSize(0));

        assertThat(manifest.getSafeMethods(), is(notNullValue()));
        assertThat(manifest.getSafeMethods(), hasSize(11));
        assertThat(manifest.getSafeMethods(),
                containsInAnyOrder(
                        "unclaimedGas",
                        "getCandidates",
                        "getValidators",
                        "getCommittee",
                        "getNextBlockValidators",
                        "name",
                        "symbol",
                        "decimals",
                        "totalSupply",
                        "balanceOf",
                        "supportedStandards"
                ));
        assertThat(manifest.getExtra(), is(nullValue()));
    }

    @Test
    public void testGetRawMemPool() throws IOException {
        NeoGetRawMemPool getRawMemPool = getNeow3j().getRawMemPool().send();
        List<String> addresses = getRawMemPool.getAddresses();

        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetTransaction() throws IOException {
        NeoGetTransaction getTransaction = getNeow3j().getTransaction(TX_HASH).send();
        Transaction transaction = getTransaction.getTransaction();

        assertNotNull(transaction);
        assertThat(transaction.getHash(), is(TX_HASH));
        assertThat(transaction.getSize(), is(TX_SIZE));
        assertThat(transaction.getVersion(), is(TX_VERSION));
        assertThat(transaction.getNonce(), is(notNullValue()));
        assertThat(transaction.getSender(), is(ADDRESS_2));
        assertThat(transaction.getSysFee(), is(notNullValue()));
        assertThat(transaction.getNetFee(), is(notNullValue()));
        assertThat(transaction.getValidUntilBlock(), is(notNullValue()));
        assertThat(transaction.getAttributes(), is(notNullValue()));
        assertThat(transaction.getAttributes(), hasSize(1));
        assertThat(transaction.getAttributes().get(0).getAsTransactionCosigner(),
                is(new TransactionCosigner(TX_COSIGNER, WitnessScope.CALLED_BY_ENTRY)));
        assertThat(transaction.getScript(), is(TX_SCRIPT));
        assertThat(transaction.getWitnesses(), is(notNullValue()));
//        assertThat(transaction.getBlockHash().length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
        assertThat(transaction.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(transaction.getBlockTime(), greaterThanOrEqualTo(0L));
//        assertThat(transaction.getVMState(), is(VM_STATE_HALT));
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        NeoGetRawTransaction getRawTransaction = getNeow3j().getRawTransaction(TX_HASH).send();
        String rawTransaction = getRawTransaction.getRawTransaction();

        assertThat(rawTransaction.length(), is(RAW_TX_LENGTH));
    }

    @Test
    public void testGetStorage() throws IOException {
        NeoGetStorage getStorage = getNeow3j().getStorage(NEO_HASH, KEY_TO_LOOKUP_AS_HEX).send();
        String storage = getStorage.getStorage();

        assertThat(storage.length(), is(STORAGE_LENGTH));
    }

    @Test
    public void testGetStorage_with_HexParameter() throws IOException {
        NeoGetStorage getStorage = getNeow3j().getStorage(NEO_HASH_WITH_PREFIX, KEY_TO_LOOKUP_AS_HEX).send();
        String storage = getStorage.getStorage();

        assertThat(storage.length(), is(STORAGE_LENGTH));
    }

    @Test
    public void testGetValidators() throws IOException {
        NeoGetValidators getValidators = getNeow3j().getValidators().send();
        List<NeoGetValidators.Validator> validators = getValidators.getValidators();

        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetConnectionCount() throws IOException {
        NeoConnectionCount getConnectionCount = getNeow3j().getConnectionCount().send();
        Integer count = getConnectionCount.getCount();

        assertNotNull(count);
        assertThat(count, greaterThanOrEqualTo(0));
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
    public void testGetVersion() throws IOException {
        NeoGetVersion version = getNeow3j().getVersion().send();
        NeoGetVersion.Result versionResult = version.getVersion();

        assertNotNull(versionResult);
        assertThat(versionResult.getUserAgent(), not(isEmptyString()));
        assertThat(versionResult.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(versionResult.getTCPPort(), is(greaterThanOrEqualTo(0)));
        assertThat(versionResult.getWSPort(), is(greaterThanOrEqualTo(0)));
    }

    // SmartContract Methods

    @Ignore("Ignored due to ByteArray not comopletely replaced by ByteString yet.")
    @Test
    public void testInvokeFunction_empty_Params() throws IOException {
        NeoInvokeFunction invokeFunction = getNeow3j().invokeFunction(NEO_HASH, INVOKE_SYMBOL).send();
        InvocationResult invocationResult = invokeFunction.getInvocationResult();
        // TODO: 10.06.20 Michael: write test as soon as ByteArray is completely replaces by ByteString.
    }

    // Utilities Methods

    @Test
    public void testListPlugins() throws IOException {
        NeoListPlugins listPlugins = getNeow3j().listPlugins().send();
        List<NeoListPlugins.Plugin> plugins = listPlugins.getPlugins();

        assertThat(plugins, is(notNullValue()));
        assertThat(plugins, hasSize(7));
        System.out.println(plugins.get(0).getName());
    }

    @Test
    public void testValidateAddress() throws IOException {
        NeoValidateAddress validateAddress = getNeow3j().validateAddress(ADDRESS_1).send();
        NeoValidateAddress.Result validation = validateAddress.getValidation();

        assertThat(validation.getAddress(), is(ADDRESS_1));
        assertTrue(validation.getValid());
        assertTrue(validation.isValid());
    }

    @Test
    public void testCloseWallet() throws IOException {
        NeoCloseWallet closeWallet = getNeow3j().closeWallet().send();

        assertTrue(closeWallet.getCloseWallet());
    }

    @Test
    public void testOpenWallet() throws IOException {
        NeoOpenWallet openWallet = getNeow3j().openWallet("wallet.json", "neo").send();

        assertTrue(openWallet.getOpenWallet());
    }

    @Test
    public void testDumpPrivKey() throws IOException {
        NeoDumpPrivKey dumpPrivKey = getNeow3j().dumpPrivKey(ADDRESS_1).send();

        assertThat(dumpPrivKey.getDumpPrivKey(), not(isEmptyOrNullString()));
        assertThat(dumpPrivKey.getDumpPrivKey(), is(ADDR1_WIF));
    }

    @Test
    public void testGetBalance() throws IOException {
        NeoGetBalance getBalance = getNeow3j().getBalance(NEO_HASH).send();
        NeoGetBalance.Balance balance = getBalance.getBalance();

        assertNotNull(balance.getBalance());
        assertThat(Integer.parseInt(balance.getBalance()),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance.getBalance()),
                is(lessThanOrEqualTo(TOTAL_NEO_SUPPLY)));
    }

    @Test
    public void testGetBalance_with_Prefix() throws IOException {
        NeoGetBalance getBalance = getNeow3j().getBalance(NEO_HASH_WITH_PREFIX).send();
        NeoGetBalance.Balance balance = getBalance.getBalance();

        assertNotNull(balance.getBalance());
        assertThat(Integer.parseInt(balance.getBalance()),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance.getBalance()),
                is(lessThanOrEqualTo(TOTAL_NEO_SUPPLY)));
    }

    @Test
    public void testGetUnclaimedGas() throws IOException {
        NeoGetUnclaimedGas getUnclaimedGas = getNeow3j().getUnclaimedGas().send();

        assertNotNull(getUnclaimedGas.getUnclaimedGas());
    }

    @Test
    public void testListAddress() throws IOException {
        NeoListAddress listAddress = getNeow3j().listAddress().send();
        List<NeoAddress> addresses = listAddress.getAddresses();

        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetNep5Transfers() throws IOException {
        NeoGetNep5Transfers getNep5Transfers = getNeow3j().getNep5Transfers(ADDRESS_2).send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers.getNep5Transfer();

        assertNotNull(nep5TransferWrapper.getSent());
        assertThat(nep5TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep5Transfers.Nep5Transfer transfer = nep5TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(NEO_HASH_WITH_PREFIX));
        assertThat(transfer.getTransferAddress(), is(RECIPIENT_ADDRESS));
        assertThat(transfer.getAmount(), is(TX_AMOUNT));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(1L));

        assertNotNull(nep5TransferWrapper.getReceived());
        assertThat(nep5TransferWrapper.getReceived().size(), greaterThanOrEqualTo(1));
        transfer = nep5TransferWrapper.getReceived().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(GAS_HASH_WITH_PREFIX));
        assertThat(transfer.getTransferAddress(), is(TX_GAS_ADDRESS));
        assertThat(transfer.getAmount(), is(TX_GAS_AMOUNT));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(0L));
        assertThat(transfer.getTxHash().length(), is(TX_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetNep5Transfers_Date() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        NeoGetNep5Transfers getNep5Transfers = getNeow3j().getNep5Transfers(ADDRESS_2, calendar.getTime()).send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers.getNep5Transfer();

        assertNotNull(nep5TransferWrapper.getSent());
        assertThat(nep5TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep5Transfers.Nep5Transfer transfer = nep5TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep5Transfers_DateFromTo() throws IOException {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.HOUR_OF_DAY, -1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.HOUR_OF_DAY, 1);
        NeoGetNep5Transfers getNep5Transfers = getNeow3j()
                .getNep5Transfers(ADDRESS_2, from.getTime(), to.getTime())
                .send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers.getNep5Transfer();

        assertNotNull(nep5TransferWrapper.getSent());
        assertThat(nep5TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep5Transfers.Nep5Transfer transfer = nep5TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep5Balances() throws IOException {
        NeoGetNep5Balances nep5Balances = getNeow3j().getNep5Balances(ADDRESS_2).send();
        NeoGetNep5Balances.Balances balances = nep5Balances.getBalances();

        assertNotNull(balances);
        assertThat(balances.getAddress(), is(ADDRESS_2));
        assertNotNull(balances.getBalances());
        assertThat(balances.getBalances(), hasSize(2));
        assertThat(balances.getBalances().get(0).getAssetHash(), is(GAS_HASH_WITH_PREFIX));
        assertNotNull(balances.getBalances().get(0).getAmount());
        assertThat(balances.getBalances().get(0).getLastUpdatedBlock(),
                is(greaterThanOrEqualTo(new BigInteger("0"))));
        assertThat(balances.getBalances().get(1).getAssetHash(), is(NEO_HASH_WITH_PREFIX));
        assertNotNull(balances.getBalances().get(1).getAmount());
        assertNotNull(balances.getBalances().get(1).getLastUpdatedBlock());
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws IOException, InterruptedException {
        NeoGetApplicationLog getApplicationLog = getNeow3j().getApplicationLog(TX_HASH).send();
        NeoApplicationLog applicationLog = getApplicationLog.getApplicationLog();

        assertNotNull(applicationLog);
        assertThat(applicationLog.getTransactionId(), is(TX_HASH));
        assertThat(applicationLog.getTrigger(), is(APPLICATION_LOG_TRIGGER));
        assertThat(applicationLog.getState(), is(VM_STATE_HALT));
        assertThat(applicationLog.getGasConsumed(), is(TX_GAS_CONSUMED));
        assertNotNull(applicationLog.getStack());
        assertThat(applicationLog.getStack(), hasSize(0));

        assertNotNull(applicationLog.getNotifications());
        System.out.println(applicationLog.getNotifications().toString());
        assertThat(applicationLog.getNotifications(), hasSize(3));
        assertThat(applicationLog.getNotifications().get(0).getContract(), is(GAS_HASH_WITH_PREFIX));
        assertFalse(applicationLog.getNotifications().get(0).getState().asArray().isEmpty());
        assertThat(applicationLog.getNotifications().get(1).getContract(), is(GAS_HASH_WITH_PREFIX));
        assertFalse(applicationLog.getNotifications().get(1).getState().asArray().isEmpty());
        assertThat(applicationLog.getNotifications().get(2).getContract(), is(NEO_HASH_WITH_PREFIX));
        assertFalse(applicationLog.getNotifications().get(2).getState().asArray().isEmpty());
    }
}
