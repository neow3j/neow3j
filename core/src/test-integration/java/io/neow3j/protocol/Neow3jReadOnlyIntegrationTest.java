package io.neow3j.protocol;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_WIF;
import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH_STRING;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH_STRING;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_TOTAL_SUPPLY;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.protocol.IntegrationTestHelper.VM_STATE_HALT;
import static io.neow3j.protocol.IntegrationTestHelper.getNodeUrl;
import static io.neow3j.protocol.IntegrationTestHelper.setupPrivateNetContainer;
import static java.util.Collections.singletonList;
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
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import io.neow3j.protocol.core.methods.response.ContractNef;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoAddress;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool.MemPoolDetails;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances.Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers.Nep17TransferWrapper;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators.Validator;
import io.neow3j.protocol.core.methods.response.NeoGetPeers.Peers;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas.GetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoListPlugins.Plugin;
import io.neow3j.protocol.core.methods.response.NeoNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Await;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

// This test class uses a static container which is started once for the whole class and reused in
// every test. Therefore only tests that don't need a new and clean blockchain should be added here.
public class Neow3jReadOnlyIntegrationTest {

    private static final String NEO_TOKEN_HASH = "0xf61eebf573ea36593fd43aa150c055ad7906ab83";
    private static final String GAS_TOKEN_NAME = "GasToken";

    // Information about the transaction that is sent after starting the node.
    private static Hash256 txHashNeoTransfer;
    private static String txHashNeoTransferAsString;
    private static Hash256 txHashGasTransfer;

    private static final String TX_GAS_CONSUMED = "9999540";
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

    protected static final String UNCLAIMED_GAS = "99997500";

    // The address that is imported to the wallet.
    protected static final String IMPORT_ADDRESS_WIF =
            "L3ijcgFEaNvR5nYYHuMNLtCc8e5Qwerj9qe6VUHNkF74GkUZtiD8";
    protected static final String IMPORT_ADDRESS = "NcVYTbDRzThKUFxEvjA4nPDn1nVpBK5CVH";
    // The address from which account 2 receives GAS when sending NEO to the recipient address.
    protected static final String TX_GAS_ADDRESS = "NKuyBkoGdZZSLyPbJEetheRhMjeznFZszf";
    protected static final String TX_GAS_AMOUNT_EXPECTED = "100000000";

    protected static final long BLOCK_0_IDX = 0;
    protected static final String BLOCK_0_HASH_STRING =
            "0xfd502bea5e3badbdfcedde3bf7e59330440fe1ba8b079dd1b18aaa9257848c59";
    private static final Hash256 BLOCK_0_HASH = new Hash256(BLOCK_0_HASH_STRING);
    protected static final String BLOCK_0_HEADER_RAW_STRING =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVWHgUp8gG/opUbVeYsix9tm7/OLPI0Hiue25Q3blOFCI6hnvVQEAAAAAAAB6/SAyVcspcr0KaoJ+dOOH7TIr7AEAAREA";
    protected static final String BLOCK_0_RAW_STRING =
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
        getNeow3j().openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(
                "1", new Hash160(NEO_TOKEN_HASH), getNeow3j());
        // make a transaction that can be used for the tests
        txHashNeoTransfer = transferNeo(TX_RECIPIENT_1, TX_AMOUNT_NEO);
        txHashNeoTransferAsString = txHashNeoTransfer.toString();
        txHashGasTransfer = transferGas(TX_RECIPIENT_1, TX_AMOUNT_GAS);
    }

    private static Hash256 transferNeo(String toAddress, String amount) throws IOException {
        NeoSendToAddress send = getNeow3j()
                .sendToAddress(NEO_HASH_STRING, toAddress, amount)
                .send();
        Hash256 txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return txHash;
    }

    private static Hash256 transferGas(String toAddress, String amount) throws IOException {
        NeoSendToAddress send = getNeow3j()
                .sendToAddress(GAS_HASH_STRING, toAddress, amount)
                .send();
        Hash256 txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        Await.waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return txHash;
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    @Test
    public void testGetBestBlockHash() throws IOException {
        Hash256 blockHash = getNeow3j()
                .getBestBlockHash()
                .send()
                .getBlockHash();

        assertNotNull(blockHash);
        assertThat(blockHash, instanceOf(Hash256.class));
    }

    @Test
    public void testGetBlock_Index() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_0_IDX), false)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertNull(block.getTransactions());
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlock(new BlockParameterIndex(BLOCK_0_IDX), true)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertNotNull(block.getTransactions());
        assertThat(block.getTransactions(), hasSize(0));
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlock(BLOCK_0_HASH_STRING, true)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(BLOCK_0_IDX));
        assertNotNull(block.getTransactions());
        assertTrue(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlock(new BlockParameterIndex(BLOCK_0_IDX))
                .send()
                .getRawBlock();

        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlock(BLOCK_0_HASH_STRING)
                .send()
                .getRawBlock();

        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetBlockHeaderCount() throws IOException {
        BigInteger count = getNeow3j()
                .getBlockHeaderCount()
                .send()
                .getCount();

        assertNotNull(count);
        assertThat(count, greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetBlockCount() throws IOException {
        BigInteger blockIndex = getNeow3j()
                .getBlockCount()
                .send()
                .getBlockIndex();

        assertNotNull(blockIndex);
        assertThat(blockIndex, greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetBlockHash() throws IOException {
        Hash256 blockHash = getNeow3j()
                .getBlockHash(new BlockParameterIndex(1))
                .send()
                .getBlockHash();

        assertNotNull(blockHash);
        assertThat(blockHash, instanceOf(Hash256.class));
    }

    @Test
    public void testGetBlockHeader_Hash() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlockHeader(BLOCK_0_HASH)
                .send()
                .getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetBlockHeader_Hash_fromStringBlockHash() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlockHeader(BLOCK_0_HASH_STRING)
                .send()
                .getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetBlockHeader_Index() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlockHeader(new BlockParameterIndex(BLOCK_0_IDX))
                .send()
                .getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlockHeader(BLOCK_0_HASH_STRING)
                .send()
                .getRawBlock();

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlockHeader(new BlockParameterIndex(BLOCK_0_IDX))
                .send()
                .getRawBlock();

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetNativeContracts() throws IOException {
        List<ContractState> nativeContracts = getNeow3j()
                .getNativeContracts()
                .send()
                .getNativeContracts();

        assertThat(nativeContracts, hasSize(8));
        ContractState contractState1 = nativeContracts.get(0);
        assertThat(contractState1.getId(), is(-1));
        assertNull(contractState1.getUpdateCounter());
        assertThat(contractState1.getHash(),
                is(new Hash160("0xa501d7d7d10983673b61b7a2d3a813b36f9f0e43")));

        ContractNef nef1 = contractState1.getNef();
        assertThat(nef1.getMagic(), is(860243278L));
        assertThat(nef1.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef1.getTokens(), hasSize(0));
        assertThat(nef1.getScript(), is("D0Ea93tn"));
        assertThat(nef1.getChecksum(), is(3516775561L));

        ContractManifest manifest1 = contractState1.getManifest();
        assertThat(manifest1.getName(), is("ContractManagement"));
        assertThat(manifest1.getGroups(), hasSize(0));
        assertThat(manifest1.getSupportedStandards(), hasSize(0));

        ContractABI abi1 = manifest1.getAbi();
        assertThat(abi1.getMethods(), hasSize(8));
        assertThat(abi1.getMethods().get(7).getName(), is("update"));
        assertThat(abi1.getMethods().get(7).getParameters(),
                hasSize(3));
        assertThat(abi1.getMethods().get(7).getReturnType(), is(ContractParameterType.VOID));
        assertThat(abi1.getMethods().get(7).getOffset(), is(0));
        assertFalse(abi1.getMethods().get(7).isSafe());
        assertThat(abi1.getEvents(), hasSize(3));
        assertThat(abi1.getEvents().get(1).getName(), is("Update"));
        assertThat(abi1.getEvents().get(1).getParameters(), hasSize(1));
        assertThat(abi1.getEvents().get(1).getParameters().get(0).getParamName(), is("Hash"));
        assertThat(abi1.getEvents().get(1).getParameters().get(0).getParamType(),
                is(ContractParameterType.HASH160));

        assertThat(manifest1.getPermissions(), hasSize(1));
        assertThat(manifest1.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest1.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest1.getPermissions().get(0).getMethods().get(0), is("*"));
        assertThat(manifest1.getTrusts(), hasSize(0));
        assertNull(manifest1.getExtra());
        assertThat(contractState1.getActiveBlockIndex(), is(0));

        ContractState contractState8 = nativeContracts.get(7);
        assertThat(contractState8.getId(), is(-8));
        assertNull(contractState8.getUpdateCounter());
        assertThat(contractState8.getHash(),
                is(new Hash160("0xa2b524b68dfe43a9d56af84f443c6b9843b8028c")));

        ContractNef nef8 = contractState8.getNef();
        assertThat(nef8.getMagic(), is(860243278L));
        assertThat(nef8.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef8.getTokens(), hasSize(0));
        assertThat(nef8.getScript(), is("APhBGvd7Zw=="));
        assertThat(nef8.getChecksum(), is(3740064217L));

        ContractManifest manifest8 = contractState8.getManifest();
        assertThat(manifest8.getName(), is("NameService"));
        assertThat(manifest8.getGroups(), hasSize(0));
        assertThat(manifest8.getSupportedStandards(), hasSize(0));

        ContractABI abi8 = manifest8.getAbi();
        assertThat(abi8.getMethods(), hasSize(20));
        assertThat(abi8.getMethods().get(19).getName(), is("transfer"));
        assertThat(abi8.getMethods().get(19).getParameters(), hasSize(2));
        assertThat(abi8.getMethods().get(19).getReturnType(), is(ContractParameterType.BOOLEAN));
        assertThat(abi8.getMethods().get(19).getOffset(), is(0));
        assertFalse(abi8.getMethods().get(19).isSafe());
        assertThat(abi8.getEvents(), hasSize(1));
        assertThat(abi8.getEvents().get(0).getName(), is("Transfer"));
        assertThat(abi8.getEvents().get(0).getParameters(), hasSize(4));
        assertThat(abi8.getEvents().get(0).getParameters().get(3).getParamName(), is("tokenId"));
        assertThat(abi8.getEvents().get(0).getParameters().get(3).getParamType(),
                is(ContractParameterType.BYTE_ARRAY));

        assertThat(manifest8.getPermissions(), hasSize(1));
        assertThat(manifest8.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest8.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest8.getPermissions().get(0).getMethods().get(0), is("*"));
        assertThat(manifest8.getTrusts(), hasSize(0));
        assertNull(manifest8.getExtra());
        assertThat(contractState8.getActiveBlockIndex(), is(0));
    }

    @Test
    public void testGetContractState() throws IOException {
        ContractState contractState = getNeow3j()
                .getContractState(new Hash160(NEO_HASH_STRING))
                .send()
                .getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-3));
        assertThat(contractState.getHash(), is(NEO_HASH));
        ContractNef nef = contractState.getNef();
        assertThat(nef, is(notNullValue()));
        assertThat(nef.getMagic(), is(860243278L));
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getTokens(), is(empty()));
        assertThat(nef.getScript(), is("AP1BGvd7Zw=="));
        assertThat(nef.getChecksum(), is(3921333105L));

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
        assertThat(event.getParameters().get(0).getParamType(), is(ContractParameterType.HASH160));

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
    public void testGetContractState_byName() throws IOException {
        ContractState contractState = getNeow3j()
                .getContractState(GAS_TOKEN_NAME)
                .send()
                .getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-4));
        assertThat(contractState.getHash(), is(GAS_HASH));
        ContractNef nef = contractState.getNef();
        assertNotNull(nef);
        assertThat(nef.getMagic(), is(860243278L));
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getTokens(), is(empty()));
        assertThat(nef.getScript(), is("APxBGvd7Zw=="));
        assertThat(nef.getChecksum(), is(3155977747L));

        ContractManifest manifest = contractState.getManifest();
        assertNotNull(manifest);
        assertThat(manifest.getName(), is(GAS_TOKEN_NAME));

        assertNotNull(manifest.getGroups());
        assertThat(manifest.getGroups(), hasSize(0));

        assertThat(manifest.getSupportedStandards(), hasSize(1));
        assertThat(manifest.getSupportedStandards().get(0), is("NEP-17"));

        ContractManifest.ContractABI abi = manifest.getAbi();
        assertNotNull(abi);
        assertNotNull(abi.getMethods());
        assertThat(abi.getMethods(), hasSize(5));
        ContractMethod method = abi.getMethods().get(0);
        assertThat(method.getName(), is("balanceOf"));
        assertThat(method.getParameters(), hasSize(1));
        assertThat(method.getParameters().get(0).getParamName(), is("account"));
        assertThat(method.getParameters().get(0).getParamType(), is(ContractParameterType.HASH160));
        assertThat(method.getOffset(), is(0));
        assertThat(method.getReturnType(), is(ContractParameterType.INTEGER));
        assertTrue(method.isSafe());

        assertNotNull(abi.getEvents());
        assertThat(abi.getEvents(), hasSize(1));
        ContractEvent event = abi.getEvents().get(0);
        assertThat(event.getName(), is("Transfer"));
        assertThat(event.getParameters(), hasSize(3));
        assertThat(event.getParameters().get(2).getParamName(), is("amount"));
        assertThat(event.getParameters().get(2).getParamType(), is(ContractParameterType.INTEGER));

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
    public void testGetMemPool() throws IOException {
        MemPoolDetails details = getNeow3j()
                .getMemPool()
                .send()
                .getMemPoolDetails();

        assertThat(details.getHeight(), greaterThanOrEqualTo(1L));
        assertThat(details.getVerified(), hasSize(0));
        assertThat(details.getUnverified(), hasSize(0));
    }

    @Test
    public void testGetRawMemPool() throws IOException {
        List<Hash256> addresses = getNeow3j()
                .getRawMemPool()
                .send()
                .getAddresses();

        assertNotNull(addresses);
        assertThat(addresses, hasSize(0));
    }

    @Test
    public void testGetTransaction_NeoToken() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        Transaction tx = getNeow3j()
                .getTransaction(txHashNeoTransfer)
                .send()
                .getTransaction();

        assertNotNull(tx);
        assertThat(tx.getHash(), is(txHashNeoTransfer));
        assertThat(tx.getVersion(), is(TX_VERSION));
        assertNotNull(tx.getNonce());
        assertNotNull(tx.getSender());
        assertNotNull(tx.getSysFee());
        assertNotNull(tx.getNetFee());
        assertNotNull(tx.getValidUntilBlock());
        assertNotNull(tx.getAttributes());
        assertThat(tx.getAttributes(), hasSize(0));
        assertThat(tx.getScript(), is(TX_SCRIPT_NEO_TRANSFER));
        assertNotNull(tx.getWitnesses());
        assertNotNull(tx.getBlockHash());
        assertThat(tx.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(tx.getBlockTime(), greaterThanOrEqualTo(0L));
        // the VMState should be null to the Neo transfer
        assertThat(tx.getVMState(), is(nullValue()));
    }

    @Test
    public void testGetTransaction_NeoToken_fromStringTxId() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        Transaction tx = getNeow3j()
                .getTransaction(txHashNeoTransferAsString)
                .send()
                .getTransaction();

        assertNotNull(tx);
        assertThat(tx.getHash(), is(txHashNeoTransfer));
        assertThat(tx.getVersion(), is(TX_VERSION));
        assertNotNull(tx.getNonce());
        assertNotNull(tx.getSender());
        assertNotNull(tx.getSysFee());
        assertNotNull(tx.getNetFee());
        assertNotNull(tx.getValidUntilBlock());
        assertNotNull(tx.getAttributes());
        assertThat(tx.getAttributes(), hasSize(0));
        assertThat(tx.getScript(), is(TX_SCRIPT_NEO_TRANSFER));
        assertNotNull(tx.getWitnesses());
        assertNotNull(tx.getBlockHash());
        assertThat(tx.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(tx.getBlockTime(), greaterThanOrEqualTo(0L));
        // the VMState should be null to the Neo transfer
        assertThat(tx.getVMState(), is(nullValue()));
    }

    @Test
    public void testGetTransaction_GasToken() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashGasTransfer, neow3j);

        Transaction tx = getNeow3j()
                .getTransaction(txHashGasTransfer)
                .send()
                .getTransaction();

        assertNotNull(tx);
        assertThat(tx.getHash(), is(txHashGasTransfer));
        assertThat(tx.getVersion(), is(TX_VERSION));
        assertNotNull(tx.getNonce());
        assertNotNull(tx.getSender());
        assertNotNull(tx.getSysFee());
        assertNotNull(tx.getNetFee());
        assertNotNull(tx.getValidUntilBlock());
        assertNotNull(tx.getAttributes());
        assertThat(tx.getAttributes(), hasSize(0));
        assertThat(tx.getScript(), is(TX_SCRIPT_GAS_TRANSFER));
        assertNotNull(tx.getWitnesses());
        assertNotNull(tx.getBlockHash());
        assertThat(tx.getConfirmations(), greaterThanOrEqualTo(0));
        assertThat(tx.getBlockTime(), greaterThanOrEqualTo(0L));
        // the VMState should be null to the Gas transfer
        assertThat(tx.getVMState(), is(nullValue()));
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        String rawTransaction = getNeow3j()
                .getRawTransaction(txHashNeoTransfer)
                .send()
                .getRawTransaction();

        assertThat(rawTransaction.length(), is(TX_LENGTH));
    }

    @Test
    public void testGetRawTransaction_fromStringTxId() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        String rawTransaction = getNeow3j()
                .getRawTransaction(txHashNeoTransferAsString)
                .send()
                .getRawTransaction();

        assertThat(rawTransaction.length(), is(TX_LENGTH));
    }

    @Test
    public void testGetStorage() throws IOException {
        String storage = getNeow3j()
                .getStorage(NEO_HASH, NEXT_VALIDATORS_PREFIX)
                .send()
                .getStorage();

        assertThat(storage, is("QAFBAighAhY5RqEz49Lg2Yf7kMsBsGDtF4DxcY4too7fE7ll/StgIQA="));
    }

    @Test
    public void testGetStorage_fromStringTxId() throws IOException {
        String storage = getNeow3j()
                .getStorage(NEO_HASH_STRING, NEXT_VALIDATORS_PREFIX)
                .send()
                .getStorage();

        assertThat(storage, is("QAFBAighAhY5RqEz49Lg2Yf7kMsBsGDtF4DxcY4too7fE7ll/StgIQA="));
    }

    @Test
    public void testGetTransactionHeight() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        BigInteger height = getNeow3j()
                .getTransactionHeight(txHashNeoTransfer)
                .send()
                .getHeight();

        assertThat(height.intValue(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetTransactionHeight_fromStringTxId() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        String txId = txHashNeoTransfer.toString();
        BigInteger height = getNeow3j()
                .getTransactionHeight(txId)
                .send()
                .getHeight();

        assertThat(height.intValue(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetNextBlockValidators() throws IOException {
        List<Validator> validators = getNeow3j()
                .getNextBlockValidators()
                .send()
                .getNextBlockValidators();

        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetCommittee() throws IOException {
        List<String> committee = getNeow3j()
                .getCommittee()
                .send()
                .getCommittee();

        assertThat(committee, hasSize(1));
        assertThat(committee.get(0),
                is("02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"));
    }

    // Node Methods

    @Test
    public void testGetConnectionCount() throws IOException {
        Integer connectionCount = getNeow3j()
                .getConnectionCount()
                .send()
                .getCount();

        assertNotNull(connectionCount);
        assertThat(connectionCount, greaterThanOrEqualTo(0));
    }

    @Test
    public void testGetPeers() throws IOException {
        Peers peers = getNeow3j()
                .getPeers()
                .send()
                .getPeers();

        assertNotNull(peers);
        assertThat(peers.getBad(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getConnected(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getUnconnected(), hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetVersion() throws IOException {
        NeoGetVersion.Result versionResult = getNeow3j()
                .getVersion()
                .send()
                .getVersion();

        assertNotNull(versionResult);
        assertThat(versionResult.getUserAgent(), not(isEmptyString()));
        assertThat(versionResult.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(versionResult.getTCPPort(), is(greaterThanOrEqualTo(0)));
        assertThat(versionResult.getWSPort(), is(greaterThanOrEqualTo(0)));
        assertThat(versionResult.getMagic(), is(greaterThanOrEqualTo(0)));
    }

    // SmartContract Methods

    @Test
    public void testInvokeFunction_empty_Params() throws IOException {
        InvocationResult result = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_SYMBOL)
                .send()
                .getInvocationResult();
        assertThat(result.getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void testInvokeFunction_empty_Params_fromString() throws IOException {
        InvocationResult result = getNeow3j()
                .invokeFunction(NEO_HASH_STRING, INVOKE_SYMBOL)
                .send()
                .getInvocationResult();

        assertThat(result.getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void testInvokeFunctionWithBalanceOf() throws IOException {
        List<ContractParameter> parameters =
                singletonList(hash160(Hash160.fromAddress(ACCOUNT_1_ADDRESS)));

        InvocationResult invocResult = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_BALANCE, parameters)
                .send()
                .getInvocationResult();

        assertNotNull(invocResult);
    }

    @Test
    public void testInvokeFunctionWithBalanceOf_fromString() throws IOException {
        List<ContractParameter> parameters =
                singletonList(hash160(Hash160.fromAddress(ACCOUNT_1_ADDRESS)));

        InvocationResult invocResult = getNeow3j()
                .invokeFunction(NEO_HASH_STRING, INVOKE_BALANCE, parameters)
                .send()
                .getInvocationResult();

        assertNotNull(invocResult);
    }

    @Test
    public void testInvokeFunctionWithTransfer() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                hash160(Hash160.fromAddress(ACCOUNT_1_ADDRESS)),
                hash160(Hash160.fromAddress(RECIPIENT)),
                integer(1),
                integer(1));
        Signer signer = new Signer.Builder()
                .account(Hash160.fromAddress(ACCOUNT_1_ADDRESS))
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(NEO_HASH)
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
    public void testInvokeFunctionWithTransfer_fromString() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                hash160(Hash160.fromAddress(ACCOUNT_1_ADDRESS)),
                hash160(Hash160.fromAddress(RECIPIENT)),
                integer(1),
                integer(1));
        Signer signer = new Signer.Builder()
                .account(Hash160.fromAddress(ACCOUNT_1_ADDRESS))
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(NEO_HASH)
                .build();

        InvocationResult invoc = getNeow3j()
                .invokeFunction(NEO_HASH_STRING, "transfer", params, signer)
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
        GetUnclaimedGas unclaimedGas = getNeow3j()
                .getUnclaimedGas(ACCOUNT_1_ADDRESS)
                .send()
                .getUnclaimedGas();

        assertThat(unclaimedGas, is(notNullValue()));
        assertThat(unclaimedGas.getUnclaimed(), is(UNCLAIMED_GAS));
        assertThat(unclaimedGas.getAddress(), is(ACCOUNT_1_ADDRESS));
    }

    // Utilities Methods

    @Test
    public void testListPlugins() throws IOException {
        List<Plugin> plugins = getNeow3j()
                .listPlugins()
                .send()
                .getPlugins();

        assertNotNull(plugins);
        assertThat(plugins, hasSize(10));
    }

    @Test
    public void testValidateAddress() throws IOException {
        NeoValidateAddress.Result validation = getNeow3j()
                .validateAddress(ACCOUNT_1_ADDRESS)
                .send()
                .getValidation();

        assertThat(validation.getAddress(), is(ACCOUNT_1_ADDRESS));
        assertTrue(validation.getValid());
        assertTrue(validation.isValid());
    }

    @Test
    public void testCloseWallet() throws IOException {
        Boolean closeWallet = getNeow3j()
                .closeWallet()
                .send()
                .getCloseWallet();

        assertTrue(closeWallet);
    }

    @Test
    public void testOpenWallet() throws IOException {
        Boolean openWallet = getNeow3j()
                .openWallet("wallet.json", "neo")
                .send()
                .getOpenWallet();

        assertTrue(openWallet);
    }

    @Test
    public void testDumpPrivKey() throws IOException {
        String dumpPrivKey = getNeow3j()
                .dumpPrivKey(ACCOUNT_1_ADDRESS)
                .send()
                .getDumpPrivKey();

        assertThat(dumpPrivKey, not(isEmptyOrNullString()));
        assertThat(dumpPrivKey, is(ACCOUNT_1_WIF));
    }

    @Test
    public void testGetBalance() throws IOException {
        String balance = getNeow3j()
                .getWalletBalance(NEO_HASH)
                .send()
                .getWalletBalance()
                .getBalance();

        assertNotNull(balance);
        assertThat(Integer.parseInt(balance), is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance), is(lessThanOrEqualTo(NEO_TOTAL_SUPPLY)));
    }

    @Test
    public void testGetBalance_string() throws IOException {
        String balance = getNeow3j()
                .getWalletBalance(NEO_HASH_STRING)
                .send()
                .getWalletBalance()
                .getBalance();

        assertNotNull(balance);
        assertThat(Integer.parseInt(balance), is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance), is(lessThanOrEqualTo(NEO_TOTAL_SUPPLY)));
    }

    @Test
    public void testGetBalance_string_withPrefix() throws IOException {
        String balance = getNeow3j()
                .getWalletBalance("0x" + NEO_HASH_STRING)
                .send()
                .getWalletBalance()
                .getBalance();

        assertNotNull(balance);
        assertThat(Integer.parseInt(balance), is(greaterThanOrEqualTo(0)));
        assertThat(Integer.parseInt(balance), is(lessThanOrEqualTo(NEO_TOTAL_SUPPLY)));
    }

    @Test
    public void testGetNewAddress() throws IOException {
        String address = getNeow3j()
                .getNewAddress()
                .send()
                .getAddress();

        Hash160 hashOfNewAddress = Hash160.fromAddress(address);

        assertThat(hashOfNewAddress, instanceOf(Hash160.class));
    }

    @Test
    public void testGetWalletUnclaimedGas() throws IOException {
        String unclaimedGas = getNeow3j()
                .getWalletUnclaimedGas()
                .send()
                .getWalletUnclaimedGas();

        assertNotNull(unclaimedGas);
    }

    @Test
    public void testImportPrivKey() throws IOException {
        NeoAddress privKey = getNeow3j()
                .importPrivKey(IMPORT_ADDRESS_WIF)
                .send()
                .getAddresses();

        assertThat(privKey.getAddress(), is(IMPORT_ADDRESS));
        assertTrue(privKey.getHasKey());
        assertNull(privKey.getLabel());
        assertFalse(privKey.getWatchOnly());
    }

    @Test
    public void testCalculateNetworkFee() throws IOException {
        NeoNetworkFee networkFee = getNeow3j()
                .calculateNetworkFee(CALC_NETWORK_FEE_TX)
                .send()
                .getNetworkFee();

        assertThat(networkFee.getNetworkFee(), is(new BigInteger("1230610")));
    }

    @Test
    public void testListAddress() throws IOException {
        List<NeoAddress> addresses = getNeow3j()
                .listAddress()
                .send()
                .getAddresses();

        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetNep17Transfers() throws IOException {
        Nep17TransferWrapper nep17TransferWrapper = getNeow3j()
                .getNep17Transfers(ACCOUNT_1_ADDRESS)
                .send()
                .getNep17Transfer();

        assertNotNull(nep17TransferWrapper.getSent());
        assertThat(nep17TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(NEO_HASH));
        assertThat(transfer.getTransferAddress(), is(TX_RECIPIENT_1));
        assertThat(transfer.getAmount(), is(TX_AMOUNT_NEO));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(1L));

        assertNotNull(nep17TransferWrapper.getReceived());
        assertThat(nep17TransferWrapper.getReceived().size(), greaterThanOrEqualTo(1));
        transfer = nep17TransferWrapper.getReceived().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(GAS_HASH));
        assertNull(transfer.getTransferAddress());
        assertThat(transfer.getAmount(), is(TX_GAS_AMOUNT_EXPECTED));
        assertThat(transfer.getBlockIndex(), is(TX_BLOCK_IDX));
        assertThat(transfer.getTransferNotifyIndex(), is(0L));
        assertThat(transfer.getTxHash(), instanceOf(Hash256.class));
    }

    @Test
    public void testGetNep17Transfers_Date() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        Nep17TransferWrapper nep17TransferWrapper = getNeow3j()
                .getNep17Transfers(ACCOUNT_1_ADDRESS, calendar.getTime())
                .send()
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

        Nep17TransferWrapper nep17TransferWrapper = getNeow3j()
                .getNep17Transfers(ACCOUNT_1_ADDRESS, from.getTime(), to.getTime())
                .send()
                .getNep17Transfer();

        assertNotNull(nep17TransferWrapper.getSent());
        assertThat(nep17TransferWrapper.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17TransferWrapper.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep17Balances() throws IOException {
        Balances balances = getNeow3j()
                .getNep17Balances(ACCOUNT_1_ADDRESS)
                .send()
                .getBalances();

        assertNotNull(balances);
        assertThat(balances.getAddress(), is(ACCOUNT_1_ADDRESS));
        assertNotNull(balances.getBalances());
        assertThat(balances.getBalances(), hasSize(2));
        assertThat(balances.getBalances().get(0).getAssetHash(), is(GAS_HASH));
        assertNotNull(balances.getBalances().get(0).getAmount());
        assertThat(balances.getBalances().get(0).getLastUpdatedBlock(),
                is(greaterThanOrEqualTo(new BigInteger("0"))));
        assertThat(balances.getBalances().get(1).getAssetHash(), is(NEO_HASH));
        assertNotNull(balances.getBalances().get(1).getAmount());
        assertNotNull(balances.getBalances().get(1).getLastUpdatedBlock());
    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        NeoApplicationLog applicationLog = getNeow3j()
                .getApplicationLog(txHashNeoTransfer)
                .send()
                .getApplicationLog();

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
                isOneOf(NEO_HASH, GAS_HASH));
        assertThat(execution.getNotifications().get(0).getEventName(), is("Transfer"));

        StackItem state = execution.getNotifications().get(0).getState();
        assertThat(state, is(notNullValue()));
        assertThat(state.getType(), is(StackItemType.ARRAY));
        assertThat(state.getList(), hasSize(3));
        assertThat(state.getList().get(0).getType(), is(StackItemType.ANY));
        assertThat(state.getList().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(state.getList().get(1).getAddress(), is(ACCOUNT_1_ADDRESS));
        assertThat(state.getList().get(2).getType(), is(StackItemType.INTEGER));
        assertThat(state.getList().get(2).getInteger(), is(new BigInteger("100000000")));
    }

    @Test
    public void testGetApplicationLog_fromStringTxId() throws IOException {
        Await.waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        String txId = txHashNeoTransfer.toString();
        NeoApplicationLog applicationLog = getNeow3j()
                .getApplicationLog(txId)
                .send()
                .getApplicationLog();

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
                isOneOf(NEO_HASH, GAS_HASH));
        assertThat(execution.getNotifications().get(0).getEventName(), is("Transfer"));

        StackItem state = execution.getNotifications().get(0).getState();
        assertThat(state, is(notNullValue()));
        assertThat(state.getType(), is(StackItemType.ARRAY));
        List<StackItem> array = state.getList();
        assertThat(array.size(), is(3));
        assertThat(array.get(0).getType(), is(StackItemType.ANY));
        assertThat(array.get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(array.get(1).getAddress(), is(ACCOUNT_1_ADDRESS));
        assertThat(array.get(2).getType(), is(StackItemType.INTEGER));
        assertThat(array.get(2).getInteger(), is(new BigInteger("100000000")));
    }

    @Test
    public void testInvokeScript() throws IOException {
        // Script that transfers 100 NEO from NX8GreRFGFK5wpGMWetpX93HmtrezGogzk to
        // NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj.
        String script = "CwBkDBSTrRVypLNcS5JUg84XAbeHQtxGDwwUev0gMlXLKXK9CmqCfnTjh+0yK+wUwB8MCHRyYW5zZmVyDBSDqwZ5rVXAUKE61D9ZNupz9ese9kFifVtSOQ==";
        Signer signer = Signer.calledByEntry(Hash160.fromAddress(ACCOUNT_1_ADDRESS));
        InvocationResult invoc = getNeow3j()
                .invokeScript(script, signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertThat(invoc.getScript(), is(script));
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
    }

}
