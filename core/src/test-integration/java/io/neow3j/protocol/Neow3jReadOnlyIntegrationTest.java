package io.neow3j.protocol;

import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_WIF;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_2_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_TOTAL_SUPPLY;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.protocol.IntegrationTestHelper.VM_STATE_HALT;
import static io.neow3j.protocol.IntegrationTestHelper.getNodeUrl;
import static io.neow3j.protocol.IntegrationTestHelper.setupPrivateNetContainer;
import static org.hamcrest.Matchers.empty;
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
import static org.hamcrest.Matchers.nullValue;
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
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoAddress;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetCommittee;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers.Nep17TransferWrapper;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Await;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
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

    private static final String NEO_TOKEN_HASH = "0xf61eebf573ea36593fd43aa150c055ad7906ab83";

    // Information about the transaction that is sent after starting the node.
    private static String txHashNeoTransfer;
    private static String txHashGasTransfer;
    private static final String TX_GAS_CONSUMED = "0.0999954";
    private static final long TX_BLOCK_IDX = 2L;
    private static final int TX_HASH_LENGTH_WITH_PREFIX = 66;
    private static final int TX_VERSION = 0;
    private static final String TX_SCRIPT_NEO_TRANSFER =
            "CwHECQwUbazId3tftsqZsILCa3hs63feRfAMFHr9IDJVyylyvQpqgn5044ftMivsFMAfDAh0cmFuc2ZlcgwUg6sGea1VwFChOtQ/WTbqc/XrHvZBYn1bUjk=";
    private static final String TX_SCRIPT_GAS_TRANSFER =
            "CwMA6HZIFwAAAAwUbazId3tftsqZsILCa3hs63feRfAMFHr9IDJVyylyvQpqgn5044ftMivsFMAfDAh0cmFuc2ZlcgwUKLOtq3Jp+cIYHbPLdB6/VRkw4nBBYn1bUjk=";
    private static final String TX_AMOUNT_NEO = "2500";
    private static final String TX_AMOUNT_GAS = "1000";
    // wif KzQMj6by8e8RaL6W2oaqbn2XMKnM7gueSEVUF4Fwg9LmDWuojqKb
    private static final String TX_RECIPIENT_1 = "NVuspqtyaV92cDo7SQdiYDCMvPUEZ3Ys3f";
    private static final int TX_LENGTH = 508;

    private static final String CALC_NETWORK_FEE_TX =
            "005815ca1700c0030000000000ebc403000000000037170000017afd203255cb2972bd0a6a827e74e387ed322bec0100560c00120c14dc84704b8283397326095c0b4e9662282c3a73190c147afd203255cb2972bd0a6a827e74e387ed322bec14c00c087472616e736665720c14b6720fef7e7eb73f25afb470f587997ce3e2460a41627d5b5201420c40a969322ebce6b9a5746005453e4c657c175403399a8ce23a1e550c64997ca23b65297ea68242e3675dc7aceec135e9f0d0e80b3d2d40e1db6b7946c1f7c86c602b110c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60110b41138defaf";

    protected static final String APPLICATION_LOG_TRIGGER = "Application";

    // Invoke function variables
    protected static final String INVOKE_SYMBOL = "symbol";
    protected static final String INVOKE_BALANCE = "balanceOf";

    protected static int BLOCK_HASH_LENGTH_WITH_PREFIX = 66;
    protected static final String UNCLAIMED_GAS = "0.999975";

    // The address that is imported to the wallet.
    protected static final String IMPORT_ADDRESS_WIF = "L3ijcgFEaNvR5nYYHuMNLtCc8e5Qwerj9qe6VUHNkF74GkUZtiD8";
    protected static final String IMPORT_ADDRESS = "NcVYTbDRzThKUFxEvjA4nPDn1nVpBK5CVH";
    // The address from which account 2 receives GAS when sending NEO to the recipient address.
    protected static final String TX_GAS_ADDRESS = "NKuyBkoGdZZSLyPbJEetheRhMjeznFZszf";
    protected static final String TX_GAS_AMOUNT_EXPECTED = "1";

    protected static long BLOCK_0_IDX = 0;
    protected static String BLOCK_0_HASH =
            "0xfd502bea5e3badbdfcedde3bf7e59330440fe1ba8b079dd1b18aaa9257848c59";
    protected static String BLOCK_0_HEADER_RAW_STRING =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVWHgUp8gG/opUbVeYsix9tm7/OLPI0Hiue25Q3blOFCI6hnvVQEAAAAAAAB6/SAyVcspcr0KaoJ+dOOH7TIr7AEAAREA";
    protected static String BLOCK_0_RAW_STRING =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVWHgUp8gG/opUbVeYsix9tm7/OLPI0Hiue25Q3blOFCI6hnvVQEAAAAAAAB6/SAyVcspcr0KaoJ+dOOH7TIr7AEAAREBAB2sK3wAAAAA";

    // wif KxwrYazXiCdK33JEddpwHbXTpAYyhXC1YyC4SXTVF6GLRPBuVBFb
    private static final String RECIPIENT = "NhixBNjEBvgyk18RzuXJt1T3BpqgAwANSA";

    protected static Neow3j neow3j;

    @ClassRule
    public static GenericContainer<?> privateNetContainer = setupPrivateNetContainer();

    private static final String NEXT_VALIDATORS_PREFIX = "0e";

    @BeforeClass
    public static void setUp() throws Exception {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(privateNetContainer)));
        // open the wallet for JSON-RPC calls
        neow3j.openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(
                "1", new ScriptHash(NEO_TOKEN_HASH), neow3j);
        // make a transaction that can be used for the tests
        txHashNeoTransfer = transferNeo(TX_RECIPIENT_1, TX_AMOUNT_NEO);
        txHashGasTransfer = transferGas(TX_RECIPIENT_1, TX_AMOUNT_GAS);
    }

    private static String transferNeo(String toAddress, String amount) throws IOException {
        NeoSendToAddress send = neow3j.sendToAddress(NEO_HASH, toAddress, amount).send();
        String txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        return txHash;
    }

    private static String transferGas(String toAddress, String amount) throws IOException {
        NeoSendToAddress send = neow3j.sendToAddress(GAS_HASH, toAddress, amount).send();
        String txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
        return txHash;
    }

    private static Neow3j getNeow3j() {
        return neow3j;
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
        assertTrue(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoGetBlock getBlock = getNeow3j().getBlock(BLOCK_0_HASH, true).send();
        NeoBlock block = getBlock.getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(BLOCK_0_IDX));
        assertNotNull(block.getTransactions());
        assertTrue(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlock(new BlockParameterIndex(BLOCK_0_IDX))
                .send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        NeoGetRawBlock getRawBlock = getNeow3j().getRawBlock(BLOCK_0_HASH).send();
        String rawBlock = getRawBlock.getRawBlock();

        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
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
        assertThat(contractState.getId(), is(-3));
        assertThat(contractState.getHash(), is("0x" + NEO_HASH));
        assertThat(contractState.getNef(), is(notNullValue()));
        assertThat(contractState.getNef().getMagic(), is(860243278L));
        assertThat(contractState.getNef().getCompiler(), is("neo-core-v3.0"));
        assertThat(contractState.getNef().getTokens(), is(empty()));
        assertThat(contractState.getNef().getScript(), is("AP1BGvd7Zw=="));
        assertThat(contractState.getNef().getChecksum(), is(3921333105L));

        ContractManifest manifest = contractState.getManifest();
        assertNotNull(manifest);
        assertNotNull(manifest.getGroups());
        assertThat(manifest.getGroups(), hasSize(0));

        ContractManifest.ContractABI abi = manifest.getAbi();
        assertNotNull(abi);

        assertNotNull(abi.getMethods());
        assertThat(abi.getMethods(), hasSize(14));
        ContractMethod method = abi.getMethods().get(6);
        assertThat(method.getName(), is("registerCandidate"));
        assertThat(method.getParameters().get(0).getParamName(), is("pubkey"));
        assertThat(method.getParameters().get(0).getParamType(),
                is(ContractParameterType.BYTE_ARRAY));
        assertThat(method.getOffset(), is(0));
        assertThat(method.getReturnType(), is(ContractParameterType.BOOLEAN));
        assertFalse(method.isSafe());

        assertNotNull(abi.getEvents());
        assertThat(abi.getEvents(), hasSize(1));
        ContractEvent event = abi.getEvents().get(0);
        assertThat(event.getName(), is("Transfer"));
        assertThat(event.getParameters(), hasSize(3));
        assertThat(event.getParameters().get(0).getParamName(), is("from"));
        assertThat(event.getParameters().get(0).getParamType(),
                is(ContractParameterType.HASH160));

        assertNotNull(manifest.getPermissions());
        assertThat(manifest.getPermissions(), hasSize(1));
        ContractPermission permission = manifest.getPermissions().get(0);
        assertThat(permission.getContract(), is("*"));
        assertThat(permission.getMethods(), hasSize(1));
        assertThat(permission.getMethods().get(0), is("*"));

        assertNotNull(manifest.getTrusts());
        assertThat(manifest.getTrusts(), hasSize(0));

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
    public void testGetTransaction_NeoToken() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        NeoGetTransaction getTransaction = getNeow3j().getTransaction(txHashNeoTransfer).send();
        Transaction transaction = getTransaction.getTransaction();

        assertNotNull(transaction);
        assertThat(transaction.getHash(), is(txHashNeoTransfer));
        assertThat(transaction.getVersion(), is(TX_VERSION));
        assertNotNull(transaction.getNonce());
        assertNotNull(transaction.getSender());
        assertNotNull(transaction.getSysFee());
        assertNotNull(transaction.getNetFee());
        assertNotNull(transaction.getValidUntilBlock());
        assertNotNull(transaction.getAttributes());
        assertThat(transaction.getAttributes(), hasSize(0));
        assertThat(transaction.getScript(), is(TX_SCRIPT_NEO_TRANSFER));
        assertNotNull(transaction.getWitnesses());
        assertNotNull(transaction.getBlockHash());
        assertThat(transaction.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(transaction.getBlockTime(), greaterThanOrEqualTo(0L));
        // the VMState should be null to the Neo transfer
        assertThat(transaction.getVMState(), is(nullValue()));
    }

    @Test
    public void testGetTransaction_GasToken() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashGasTransfer, neow3j);

        NeoGetTransaction getTransaction = getNeow3j().getTransaction(txHashGasTransfer).send();
        Transaction transaction = getTransaction.getTransaction();

        assertNotNull(transaction);
        assertThat(transaction.getHash(), is(txHashGasTransfer));
        assertThat(transaction.getVersion(), is(TX_VERSION));
        assertNotNull(transaction.getNonce());
        assertNotNull(transaction.getSender());
        assertNotNull(transaction.getSysFee());
        assertNotNull(transaction.getNetFee());
        assertNotNull(transaction.getValidUntilBlock());
        assertNotNull(transaction.getAttributes());
        assertThat(transaction.getAttributes(), hasSize(0));
        assertThat(transaction.getScript(), is(TX_SCRIPT_GAS_TRANSFER));
        assertNotNull(transaction.getWitnesses());
        assertNotNull(transaction.getBlockHash());
        assertThat(transaction.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(transaction.getBlockTime(), greaterThanOrEqualTo(0L));
        // the VMState should be null to the Gas transfer
        assertThat(transaction.getVMState(), is(nullValue()));
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);
        NeoGetRawTransaction getRawTransaction = getNeow3j().getRawTransaction(txHashNeoTransfer).send();
        String rawTransaction = getRawTransaction.getRawTransaction();
        assertThat(rawTransaction.length(), is(TX_LENGTH));
    }

    @Test
    public void testGetStorage() throws IOException {
        NeoGetStorage getStorage = getNeow3j().getStorage(NEO_HASH, NEXT_VALIDATORS_PREFIX).send();
        String storage = getStorage.getStorage();
        assertThat(storage, is("QAFBAighAhY5RqEz49Lg2Yf7kMsBsGDtF4DxcY4too7fE7ll/StgIQA="));
    }

    @Test
    public void testGetTransactionHeight() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        NeoGetTransactionHeight getTransactionHeight = getNeow3j().getTransactionHeight(
                txHashNeoTransfer)
                .send();
        BigInteger height = getTransactionHeight.getHeight();

        assertThat(height.intValue(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetNextBlockValidators() throws IOException {
        NeoGetNextBlockValidators getNextBlockValidators = getNeow3j().getNextBlockValidators().send();
        List<NeoGetNextBlockValidators.Validator> validators = getNextBlockValidators.getNextBlockValidators();

        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetCommittee() throws IOException {
        NeoGetCommittee getCommittee = getNeow3j().getCommittee().send();
        List<String> committee = getCommittee.getCommittee();

        assertThat(committee, hasSize(1));
        assertThat(committee.get(0), is("02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"));
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
        assertThat(invocationResult.getStack().get(0).asByteString().getAsString(), is("NEO"));
    }

    @Test
    public void testInvokeFunctionWithBalanceOf() throws IOException {
        List<ContractParameter> parameters = Collections.singletonList(
                ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_1_ADDRESS)));
        ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_1_ADDRESS));
        NeoInvokeFunction invokeFunction = getNeow3j()
                .invokeFunction("0x" + NEO_HASH, INVOKE_BALANCE, parameters)
                .send();

        assertNotNull(invokeFunction.getInvocationResult());
    }

    @Test
    public void testInvokeFunctionWithTransfer() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_2_ADDRESS)),
                ContractParameter.hash160(ScriptHash.fromAddress(RECIPIENT)),
                ContractParameter.integer(1),
                ContractParameter.any(null));
        Signer signer = new Signer.Builder()
                .account(ScriptHash.fromAddress(ACCOUNT_2_ADDRESS))
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(new ScriptHash(NEO_HASH))
                .build();
        InvocationResult invoc = getNeow3j()
                .invokeFunction(NEO_HASH, "transfer", params, signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
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
        assertThat(plugins, hasSize(8));
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
                .importPrivKey(IMPORT_ADDRESS_WIF).send();
        NeoAddress privKey = importPrivKey.getAddresses();

        assertThat(privKey.getAddress(), is(IMPORT_ADDRESS));
        assertTrue(privKey.getHasKey());
        assertNull(privKey.getLabel());
        assertFalse(privKey.getWatchOnly());
    }

    @Test
    public void testCalculateNetworkFee() throws IOException {
        NeoCalculateNetworkFee calcNetworkFee = getNeow3j().calculateNetworkFee(CALC_NETWORK_FEE_TX).send();
        NeoNetworkFee networkFee = calcNetworkFee.getNetworkFee();

        assertThat(networkFee.getNetworkFee(), is(new BigInteger("1230610")));
    }

    @Test
    public void testListAddress() throws IOException {
        NeoListAddress listAddress = getNeow3j().listAddress().send();
        List<NeoAddress> addresses = listAddress.getAddresses();

        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetNep17Transfers() throws IOException {
        NeoGetNep17Transfers getNep17Transfers = getNeow3j().getNep17Transfers(ACCOUNT_1_ADDRESS)
                .send();
        Nep17TransferWrapper nep17TransferWrapper = getNep17Transfers
                .getNep17Transfer();

        assertNotNull(nep17TransferWrapper.getSent());
        assertThat(nep17TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is("0x" + NEO_HASH));
        assertThat(transfer.getTransferAddress(), is(TX_RECIPIENT_1));
        assertThat(transfer.getAmount(), is(TX_AMOUNT_NEO));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(1L));

        assertNotNull(nep17TransferWrapper.getReceived());
        assertThat(nep17TransferWrapper.getReceived().size(), greaterThanOrEqualTo(1));
        transfer = nep17TransferWrapper.getReceived().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is("0x" + GAS_HASH));
        assertNull(transfer.getTransferAddress());
        assertThat(transfer.getAmount(), is(TX_GAS_AMOUNT_EXPECTED));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(0L));
        assertThat(transfer.getTxHash().length(), is(TX_HASH_LENGTH_WITH_PREFIX));
    }

    @Test
    public void testGetNep17Transfers_Date() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        NeoGetNep17Transfers getNep17Transfers = getNeow3j().getNep17Transfers(ACCOUNT_1_ADDRESS,
                calendar.getTime()).send();
        NeoGetNep17Transfers.Nep17TransferWrapper nep17TransferWrapper = getNep17Transfers
                .getNep17Transfer();

        assertNotNull(nep17TransferWrapper.getSent());
        assertThat(nep17TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep17Transfers_DateFromTo() throws IOException {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.HOUR_OF_DAY, -1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.HOUR_OF_DAY, 1);
        NeoGetNep17Transfers getNep17Transfers = getNeow3j()
                .getNep17Transfers(ACCOUNT_1_ADDRESS, from.getTime(), to.getTime())
                .send();
        NeoGetNep17Transfers.Nep17TransferWrapper nep17TransferWrapper = getNep17Transfers
                .getNep17Transfer();

        assertNotNull(nep17TransferWrapper.getSent());
        assertThat(nep17TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep17Balances() throws IOException {
        NeoGetNep17Balances nep17Balances = getNeow3j().getNep17Balances(ACCOUNT_1_ADDRESS).send();
        NeoGetNep17Balances.Balances balances = nep17Balances.getBalances();

        assertNotNull(balances);
        assertThat(balances.getAddress(), is(ACCOUNT_1_ADDRESS));
        assertNotNull(balances.getBalances());
        assertThat(balances.getBalances(), hasSize(2));
        assertThat(balances.getBalances().get(0).getAssetHash(), is("0x" + GAS_HASH));
        assertNotNull(balances.getBalances().get(0).getAmount());
        assertThat(balances.getBalances().get(0).getLastUpdatedBlock(),
                is(greaterThanOrEqualTo(new BigInteger("0"))));
        assertThat(balances.getBalances().get(1).getAssetHash(), is("0x" + NEO_HASH));
        assertNotNull(balances.getBalances().get(1).getAmount());
        assertNotNull(balances.getBalances().get(1).getLastUpdatedBlock());
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        NeoGetApplicationLog getApplicationLog = getNeow3j().getApplicationLog(txHashNeoTransfer).send();
        NeoApplicationLog applicationLog = getApplicationLog.getApplicationLog();

        assertNotNull(applicationLog);
        assertThat(applicationLog.getTransactionId(), is(txHashNeoTransfer));
        assertThat(applicationLog.getExecutions(), hasSize(1));

        NeoApplicationLog.Execution execution = applicationLog.getExecutions().get(0);
        assertThat(execution.getTrigger(), is(APPLICATION_LOG_TRIGGER));
        assertThat(execution.getState(), is(VM_STATE_HALT));
        assertNull(execution.getException());
        assertThat(execution.getGasConsumed(), is(TX_GAS_CONSUMED));
        assertNotNull(execution.getStack());
        assertThat(execution.getStack(), hasSize(0));

        assertNotNull(execution.getNotifications());
        assertThat(execution.getNotifications(), hasSize(greaterThanOrEqualTo(1)));
        assertThat(execution.getNotifications().get(0).getContract(),
                isOneOf("0x" + NEO_HASH, "0x" + GAS_HASH));
        assertThat(execution.getNotifications().get(0).getEventName(), is("Transfer"));

        StackItem state = execution.getNotifications().get(0).getState();
        assertThat(state, is(notNullValue()));
        assertThat(state.getType(), is(StackItemType.ARRAY));
        assertThat(state.asArray().getValue(), hasSize(3));
        assertThat(state.asArray().getValue().get(0).getType(), is(StackItemType.ANY));
        assertThat(state.asArray().getValue().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(state.asArray().getValue().get(1).asByteString().getAsAddress(), is(
                ACCOUNT_1_ADDRESS));
        assertThat(state.asArray().getValue().get(2).getType(), is(StackItemType.INTEGER));
        assertThat(state.asArray().getValue().get(2).asInteger().getValue(),
                is(new BigInteger("100000000")));
    }

    @Test
    public void testInvokeScript() throws IOException {
        // Script that transfers 100 NEO from NX8GreRFGFK5wpGMWetpX93HmtrezGogzk to
        // NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj.
        String script = "CwBkDBSTrRVypLNcS5JUg84XAbeHQtxGDwwUev0gMlXLKXK9CmqCfnTjh+0yK+wUwB8MCHRyYW5zZmVyDBSDqwZ5rVXAUKE61D9ZNupz9ese9kFifVtSOQ==";
        Signer signer = Signer.calledByEntry(ScriptHash.fromAddress(ACCOUNT_1_ADDRESS));
        NeoInvokeScript invokeScript = getNeow3j().invokeScript(script, signer).send();
        InvocationResult invoc = invokeScript.getInvocationResult();

        assertNotNull(invoc);
        assertThat(invoc.getScript(), is(script));
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
    }


}
