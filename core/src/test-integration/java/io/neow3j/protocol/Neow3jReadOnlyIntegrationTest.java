package io.neow3j.protocol;

import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_WIF;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_2_PUBKEY;
import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_TOTAL_SUPPLY;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.protocol.IntegrationTestHelper.VM_STATE_HALT;
import static io.neow3j.protocol.IntegrationTestHelper.getNodeUrl;
import static io.neow3j.protocol.IntegrationTestHelper.setupPrivateNetContainer;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.HexParameter;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoAddress;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

// This test class uses a static container which is started once for the whole class and reused in
// every test. Therefore only tests that don't need a new and clean blockchain should be added here.
public class Neow3jReadOnlyIntegrationTest {

    // Information about the transaction that is sent after starting the node.
    private static String txHash;
    private static final String TX_GAS_CONSUMED = "9007990";
    private static final long TX_BLOCK_IDX = 2L;
    private static final int TX_HASH_LENGTH_WITH_PREFIX = 66;
    private static final int TX_VERSION = 0;
    private static final String TX_SCRIPT = "AcQJDBTXhdxFuBA/Rv+5MO5//k7/XYa79wwUCJjqIZc3j2I6dnCXR"
            + "FREhXbQrq8TwAwIdHJhbnNmZXIMFCUFnstIeNOodfkcUc7e0zDUV1/eQWJ9W1I4";
    private static final String TX_AMOUNT = "2500";
    private static final String TX_RECIPIENT_1 = "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2";
    private static final int TX_LENGTH = 496;

    protected static final String APPLICATION_LOG_TRIGGER = "Application";

    // Invoke function variables
    protected static final String INVOKE_SYMBOL = "symbol";
    protected static final String INVOKE_BALANCE = "balanceOf";

    protected static int BLOCK_HASH_LENGTH_WITH_PREFIX = 66;
    protected static final String UNCLAIMED_GAS = "599985000";

    // The address that is imported to the wallet.
    protected static final String IMPORT_ADDRESS = "AaN1ksPFARY9dH73brS8AGoUBt86zqRDcQ";
    // The address from which account 2 receives GAS when sending NEO to the recipient address.
    protected static final String TX_GAS_ADDRESS = "AFmseVrdL9f9oyCzZefL9tG6UbvhPbdYzM";
    protected static final String TX_GAS_AMOUNT = "1200000000";

    protected static long BLOCK_0_IDX = 0;
    protected static String BLOCK_0_HASH =
            "0x78acf25f201970d882ed8e29480a8879b4379ea6b4ffc0c9797f971352377891";
    protected static String BLOCK_0_HEADER_RAW_STRING =
            "0000000000000000000000000000000000000000000000000000000000000000000000002e0b8a0698b8fc0740928ba5b8bf7cd5a504b8ed7dcc21a4bcedac6ba19ba1e588ea19ef55010000000000000898ea2197378f623a7670974454448576d0aeaf0100011100";

    protected static Neow3jTestWrapper neow3jWrapper;

    @ClassRule
    public static GenericContainer privateNetContainer = setupPrivateNetContainer();

    private static final String NEXT_VALIDATORS_PREFIX = "0e";

    @BeforeClass
    public static void setUp() throws Exception {
        neow3jWrapper = new Neow3jTestWrapper(new HttpService(getNodeUrl(privateNetContainer)));
        // open the wallet for JSON-RPC calls
        neow3jWrapper.openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        neow3jWrapper.waitUntilWalletHasBalanceGreaterThanOrEqualToOne();
        // make a transaction that can be used for the tests
        txHash = neow3jWrapper.performNeoTransfer(TX_RECIPIENT_1, TX_AMOUNT);
    }

    private static Neow3j getNeow3j() {
        return neow3jWrapper;
    }

    @Test
    public void testGetBestBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = getNeow3j().getBestBlockHash().send();
        String blockHash = getBestBlockHash.getBlockHash();

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
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlock(new BlockParameterIndex(BLOCK_0_IDX))
                .send();
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
    public void testGetBlockHash() throws IOException {
        NeoBlockHash getBlockHash = getNeow3j().getBlockHash(new BlockParameterIndex(1)).send();
        String blockHash = getBlockHash.getBlockHash();

        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
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
        NeoGetBlock getBlock = getNeow3j().getBlockHeader(new BlockParameterIndex(BLOCK_0_IDX))
                .send();
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

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlockHeader(
                new BlockParameterIndex(BLOCK_0_IDX)).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetContractState() throws IOException {
        NeoGetContractState getContractState = getNeow3j().getContractState(NEO_HASH).send();
        NeoGetContractState.ContractState contractState = getContractState.getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-1));
        assertThat(contractState.getHash(), is("0x" + NEO_HASH));
        assertThat(contractState.getScript(), is("DANORU9Ba2d4Cw=="));

        NeoGetContractState.ContractState.ContractManifest manifest = contractState.getManifest();
        assertNotNull(manifest);
        assertNotNull(manifest.getGroups());
        assertThat(manifest.getGroups(), hasSize(0));
        assertNotNull(manifest.getFeatures());
        assertTrue(manifest.getFeatures().getStorage());
        assertFalse(manifest.getFeatures().getPayable());

        NeoGetContractState.ContractState.ContractManifest.ContractABI abi = manifest.getAbi();
        assertNotNull(abi);
        assertThat(abi.getHash(), is("0x" + NEO_HASH));

        assertNotNull(abi.getMethods());
        assertThat(abi.getMethods(), hasSize(15));
        assertThat(abi.getMethods().get(3).getName(), is("registerCandidate"));
        assertThat(abi.getMethods().get(3).getParameters().get(0).getParamName(), is("pubkey"));
        assertThat(abi.getMethods().get(3).getParameters().get(0).getParamType(),
                is(ContractParameterType.BYTE_ARRAY));
        assertThat(abi.getMethods().get(3).getOffset(), is(0));
        assertThat(abi.getMethods().get(3).getReturnType(), is(ContractParameterType.BOOLEAN));

        assertNotNull(abi.getEvents());
        assertThat(abi.getEvents(), hasSize(1));
        assertThat(abi.getEvents().get(0).getName(), is("Transfer"));
        assertThat(abi.getEvents().get(0).getParameters(), hasSize(3));
        assertThat(abi.getEvents().get(0).getParameters().get(0).getParamName(), is("from"));
        assertThat(abi.getEvents().get(0).getParameters().get(0).getParamType(),
                is(ContractParameterType.HASH160));

        assertNotNull(manifest.getPermissions());
        assertThat(manifest.getPermissions(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest.getPermissions().get(0).getMethods().get(0), is("*"));

        assertNotNull(manifest.getTrusts());
        assertThat(manifest.getTrusts(), hasSize(0));

        assertNotNull(manifest.getSafeMethods());
        assertThat(manifest.getSafeMethods(), hasSize(10));
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
                        "balanceOf"
                ));
        assertNull(manifest.getExtra());
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
        neow3jWrapper.waitUntilTxHash(txHash);

        NeoGetTransaction getTransaction = getNeow3j().getTransaction(txHash).send();
        Transaction transaction = getTransaction.getTransaction();

        assertNotNull(transaction);
        assertThat(transaction.getHash(), is(txHash));
        assertThat(transaction.getVersion(), is(TX_VERSION));
        assertNotNull(transaction.getNonce());
        assertNotNull(transaction.getSender());
        assertNotNull(transaction.getSysFee());
        assertNotNull(transaction.getNetFee());
        assertNotNull(transaction.getValidUntilBlock());
        assertNotNull(transaction.getAttributes());
        assertThat(transaction.getAttributes(), hasSize(0));
        assertThat(transaction.getScript(), is(TX_SCRIPT));
        assertNotNull(transaction.getWitnesses());
        assertNotNull(transaction.getBlockHash());
        assertThat(transaction.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(transaction.getBlockTime(), greaterThanOrEqualTo(0L));
        assertThat(transaction.getVMState(), is(VM_STATE_HALT));
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        neow3jWrapper.waitUntilTxHash(txHash);
        NeoGetRawTransaction getRawTransaction = getNeow3j().getRawTransaction(txHash).send();
        String rawTransaction = getRawTransaction.getRawTransaction();
        assertThat(rawTransaction.length(), is(TX_LENGTH));
    }

    @Test
    public void testGetStorage() throws IOException {
        NeoGetStorage getStorage = getNeow3j().getStorage(NEO_HASH, NEXT_VALIDATORS_PREFIX).send();
        String storage = getStorage.getStorage();
        assertThat(storage, is("01" + Numeric.toHexStringNoPrefix(ACCOUNT_2_PUBKEY.toArray())));
    }

    @Test
    public void testGetStorage_with_HexParameter() throws IOException {
        NeoGetStorage getStorage = getNeow3j().getStorage(NEO_HASH,
                HexParameter.valueOf(new BigInteger(NEXT_VALIDATORS_PREFIX, 16)))
                .send();
        String storage = getStorage.getStorage();
        assertThat(storage, is("01" + Numeric.toHexStringNoPrefix(ACCOUNT_2_PUBKEY.toArray())));
    }

    @Test
    public void testGetTransactionHeight() throws IOException {
        neow3jWrapper.waitUntilTxHash(txHash);

        NeoGetTransactionHeight getTransactionHeight = getNeow3j().getTransactionHeight(txHash)
                .send();
        BigInteger height = getTransactionHeight.getHeight();

        assertThat(height.intValue(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetValidators() throws IOException {
        NeoGetValidators getValidators = getNeow3j().getValidators().send();
        List<NeoGetValidators.Validator> validators = getValidators.getValidators();

        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    // Node Methods

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

    @Test
    public void testInvokeFunction_empty_Params() throws IOException {
        NeoInvokeFunction invokeFunction = getNeow3j().invokeFunction(NEO_HASH, INVOKE_SYMBOL)
                .send();
        InvocationResult invocationResult = invokeFunction.getInvocationResult();
        assertThat(invocationResult.getStack().get(0).asByteString().getAsString(), is("neo"));
    }

    @Test
    public void testInvokeFunction() throws IOException {
        List<ContractParameter> parameters = Collections.singletonList(
                ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_1_ADDRESS)));
        ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_1_ADDRESS));
        NeoInvokeFunction invokeFunction = getNeow3j()
                .invokeFunction("0x" + NEO_HASH, INVOKE_BALANCE, parameters)
                .send();

        assertNotNull(invokeFunction.getInvocationResult());
    }

    @Test
    public void testGetUnclaimedGas() throws IOException {
        NeoGetUnclaimedGas getUnclaimedGas = getNeow3j().getUnclaimedGas(ACCOUNT_1_ADDRESS).send();

        assertThat(getUnclaimedGas.getUnclaimedGas(), is(notNullValue()));
        assertThat(getUnclaimedGas.getUnclaimedGas().getUnclaimed(), is(UNCLAIMED_GAS));
        assertThat(getUnclaimedGas.getUnclaimedGas().getAddress(), is(ACCOUNT_1_ADDRESS));
    }

    // Utilities Methods

    @Test
    public void testListPlugins() throws IOException {
        NeoListPlugins listPlugins = getNeow3j().listPlugins().send();
        List<NeoListPlugins.Plugin> plugins = listPlugins.getPlugins();

        assertNotNull(plugins);
        assertThat(plugins, hasSize(7));
    }

    @Test
    public void testValidateAddress() throws IOException {
        NeoValidateAddress validateAddress = getNeow3j().validateAddress(ACCOUNT_1_ADDRESS).send();
        NeoValidateAddress.Result validation = validateAddress.getValidation();

        assertThat(validation.getAddress(), is(ACCOUNT_1_ADDRESS));
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
        NeoDumpPrivKey dumpPrivKey = getNeow3j().dumpPrivKey(ACCOUNT_1_ADDRESS).send();

        assertThat(dumpPrivKey.getDumpPrivKey(), not(isEmptyOrNullString()));
        assertThat(dumpPrivKey.getDumpPrivKey(), is(ACCOUNT_1_WIF));
    }

    @Test
    public void testGetBalance() throws IOException {
        NeoGetWalletBalance getWalletBalance = getNeow3j().getWalletBalance(NEO_HASH).send();
        String balance = getWalletBalance.getWalletBalance().getBalance();

        assertNotNull(balance);
        assertThat(Integer.parseInt(balance),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance),
                is(lessThanOrEqualTo(NEO_TOTAL_SUPPLY)));
    }

    @Test
    public void testGetBalance_with_Prefix() throws IOException {
        NeoGetWalletBalance getWalletBalance = getNeow3j().getWalletBalance("0x" + NEO_HASH)
                .send();
        String balance = getWalletBalance.getWalletBalance().getBalance();

        assertNotNull(balance);
        assertThat(Integer.parseInt(balance),
                is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance),
                is(lessThanOrEqualTo(NEO_TOTAL_SUPPLY)));
    }

    @Test
    public void testGetNewAddress() throws IOException {
        NeoGetNewAddress getNewAddress = getNeow3j().getNewAddress().send();

        assertThat(ScriptHash.fromAddress(getNewAddress.getAddress()),
                instanceOf(ScriptHash.class));
    }

    @Test
    public void testGetWalletUnclaimedGas() throws IOException {
        Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas = getNeow3j()
                .getWalletUnclaimedGas();
        NeoGetWalletUnclaimedGas send = getWalletUnclaimedGas.send();

        assertNotNull(send.getWalletUnclaimedGas());
    }

    @Test
    public void testImportPrivKey() throws IOException {
        NeoImportPrivKey importPrivKey = getNeow3j()
                .importPrivKey("KwYRSjqmEhK4nPuUZZz1LEUSxvSzSRCv3SVePoe67hjcdPGLRJY5").send();
        NeoAddress privKey = importPrivKey.getAddresses();

        assertThat(privKey.getAddress(), is(IMPORT_ADDRESS));
        assertTrue(privKey.getHasKey());
        assertNull(privKey.getLabel());
        assertFalse(privKey.getWatchOnly());
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
        NeoGetNep5Transfers getNep5Transfers = getNeow3j().getNep5Transfers(ACCOUNT_1_ADDRESS)
                .send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers
                .getNep5Transfer();

        assertNotNull(nep5TransferWrapper.getSent());
        assertThat(nep5TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep5Transfers.Nep5Transfer transfer = nep5TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is("0x" + NEO_HASH));
        assertThat(transfer.getTransferAddress(), is(TX_RECIPIENT_1));
        assertThat(transfer.getAmount(), is(TX_AMOUNT));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(1L));

        assertNotNull(nep5TransferWrapper.getReceived());
        assertThat(nep5TransferWrapper.getReceived().size(), greaterThanOrEqualTo(1));
        transfer = nep5TransferWrapper.getReceived().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is("0x" + GAS_HASH));
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
        NeoGetNep5Transfers getNep5Transfers = getNeow3j().getNep5Transfers(ACCOUNT_1_ADDRESS,
                calendar.getTime()).send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers
                .getNep5Transfer();

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
                .getNep5Transfers(ACCOUNT_1_ADDRESS, from.getTime(), to.getTime())
                .send();
        NeoGetNep5Transfers.Nep5TransferWrapper nep5TransferWrapper = getNep5Transfers
                .getNep5Transfer();

        assertNotNull(nep5TransferWrapper.getSent());
        assertThat(nep5TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep5Transfers.Nep5Transfer transfer = nep5TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep5Balances() throws IOException {
        NeoGetNep5Balances nep5Balances = getNeow3j().getNep5Balances(ACCOUNT_1_ADDRESS).send();
        NeoGetNep5Balances.Balances balances = nep5Balances.getBalances();

        assertNotNull(balances);
        assertThat(balances.getAddress(), is(ACCOUNT_1_ADDRESS));
        assertNotNull(balances.getBalances());
        assertThat(balances.getBalances(), hasSize(2));
        assertThat(balances.getBalances().get(0).getAssetHash(), is("0x" + NEO_HASH));
        assertNotNull(balances.getBalances().get(0).getAmount());
        assertThat(balances.getBalances().get(0).getLastUpdatedBlock(),
                is(greaterThanOrEqualTo(new BigInteger("0"))));
        assertThat(balances.getBalances().get(1).getAssetHash(), is("0x" + GAS_HASH));
        assertNotNull(balances.getBalances().get(1).getAmount());
        assertNotNull(balances.getBalances().get(1).getLastUpdatedBlock());
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws IOException {
        neow3jWrapper.waitUntilTxHash(txHash);

        NeoGetApplicationLog getApplicationLog = getNeow3j().getApplicationLog(txHash).send();
        NeoApplicationLog applicationLog = getApplicationLog.getApplicationLog();

        assertNotNull(applicationLog);
        assertThat(applicationLog.getTransactionId(), is(txHash));
        assertThat(applicationLog.getTrigger(), is(APPLICATION_LOG_TRIGGER));
        assertThat(applicationLog.getState(), is(VM_STATE_HALT));
        assertThat(applicationLog.getGasConsumed(), is(TX_GAS_CONSUMED));
        assertNotNull(applicationLog.getStack());
        assertThat(applicationLog.getStack(), hasSize(0));

        assertNotNull(applicationLog.getNotifications());
        assertThat(applicationLog.getNotifications(), hasSize(greaterThanOrEqualTo(1)));
        assertThat(applicationLog.getNotifications().get(0).getContract(),
                isOneOf("0x" + NEO_HASH, "0x" + GAS_HASH));
        assertThat(applicationLog.getNotifications().get(0).getEventName(), is("Transfer"));

        StackItem state = applicationLog.getNotifications().get(0).getState();
        assertThat(state, is(notNullValue()));
        assertThat(state.getType(), is(StackItemType.ARRAY));
        assertThat(state.asArray().getValue(), hasSize(3));
        assertThat(state.asArray().getValue().get(0).getType(), is(StackItemType.ANY));
        assertThat(state.asArray().getValue().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(state.asArray().getValue().get(1).asByteString().getAsAddress(), is(
                ACCOUNT_1_ADDRESS));
        assertThat(state.asArray().getValue().get(2).getType(), is(StackItemType.INTEGER));
        assertThat(state.asArray().getValue().get(2).asInteger().getValue(),
                is(new BigInteger("1200000000")));
    }

}
