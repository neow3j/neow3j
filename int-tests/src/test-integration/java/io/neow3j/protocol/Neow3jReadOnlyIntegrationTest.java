package io.neow3j.protocol;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.response.ContractManifest.ContractPermission;
import io.neow3j.protocol.core.response.ContractNef;
import io.neow3j.protocol.core.response.ContractState;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NativeContractState;
import io.neow3j.protocol.core.response.NeoAddress;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoFindStates;
import io.neow3j.protocol.core.response.NeoGetMemPool.MemPoolDetails;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.response.NeoGetNep17Balances.Nep17Balances;
import io.neow3j.protocol.core.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.response.NeoGetNextBlockValidators.Validator;
import io.neow3j.protocol.core.response.NeoGetPeers.Peers;
import io.neow3j.protocol.core.response.NeoGetProof;
import io.neow3j.protocol.core.response.NeoGetStateHeight.StateHeight;
import io.neow3j.protocol.core.response.NeoGetStateRoot.StateRoot;
import io.neow3j.protocol.core.response.NeoGetUnclaimedGas.GetUnclaimedGas;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.core.response.NeoListPlugins.Plugin;
import io.neow3j.protocol.core.response.NeoNetworkFee;
import io.neow3j.protocol.core.response.NeoSendToAddress;
import io.neow3j.protocol.core.response.NeoValidateAddress;
import io.neow3j.protocol.core.response.Transaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.NeoTestContainer;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Numeric;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static io.neow3j.protocol.IntegrationTestHelper.COMMITTEE_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.GAS_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.test.TestProperties.committeeAccountAddress;
import static io.neow3j.test.TestProperties.committeeAccountScriptHash;
import static io.neow3j.test.TestProperties.contractManagementHash;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountScriptHash;
import static io.neow3j.test.TestProperties.defaultAccountWIF;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.gasTokenName;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.test.TestProperties.oracleContractHash;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// This test class uses a static container which is started once for the whole class and reused in every test.
// Therefore, only tests that don't need a new and clean blockchain should be added here.
@Testcontainers
public class Neow3jReadOnlyIntegrationTest {

    // Hashes of the transactions that are sent before all tests.
    private static Hash256 txHashNeoTransfer;

    private static final int TX_VERSION = 0;
    private static final String TX_SCRIPT_NEO_TRANSFER =
            "CwHECQwURVEu00/nfUbhURye" +
                    "/qtSvc8LywMMFH9l1DQ2JwiyVfDgaFa9y1zpnYUFFMAfDAh0cmFuc2ZlcgwU9WPqQLwoPU0OBcSOowWz8qBzQO9BYn1bUjk=";
    private static final BigInteger TX_AMOUNT_NEO = new BigInteger("2500");
    private static final BigInteger TX_AMOUNT_GAS = new BigInteger("1000");
    // wif KzQMj6by8e8RaL6W2oaqbn2XMKnM7gueSEVUF4Fwg9LmDWuojqKb
    private static final Hash160 TX_RECIPIENT_1 =
            Hash160.fromAddress("NSEV4gPHGUs5SAR2sqvCEi47XXhKuAh1J9");
    private static final int TX_LENGTH = 504;

    private static final String CALC_NETWORK_FEE_TX =
            "005815ca1700c0030000000000ebc403000000000037170000017afd203255cb2972bd0a6a827e74e387ed322bec0100560c00120c14dc84704b8283397326095c0b4e9662282c3a73190c147afd203255cb2972bd0a6a827e74e387ed322bec14c00c087472616e736665720c14b6720fef7e7eb73f25afb470f587997ce3e2460a41627d5b5201420c40a969322ebce6b9a5746005453e4c657c175403399a8ce23a1e550c64997ca23b65297ea68242e3675dc7aceec135e9f0d0e80b3d2d40e1db6b7946c1f7c86c602b110c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60110b41138defaf";

    protected static final String APPLICATION_LOG_TRIGGER = "Application";

    // Invoke function variables
    private static final String INVOKE_SYMBOL = "symbol";
    private static final String INVOKE_BALANCE = "balanceOf";

    private static final BigInteger BLOCK_0_IDX = BigInteger.ZERO;
    private static final Hash256 BLOCK_0_HASH =
            new Hash256("442050ddb914d41b80481a03938e63b1bb88a28f2acb8e636492205392e9f014");
    private static final String BLOCK_0_HEADER_RAW_STRING =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACI6hnvVQEAAB2sK3wAAAAAAAAAAAB/ZdQ0NicIslXw4GhWvctc6Z2FBQEAARE=";
    private static final String BLOCK_0_RAW_STRING =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACI6hnvVQEAAB2sK3wAAAAAAAAAAAB/ZdQ0NicIslXw4GhWvctc6Z2FBQEAAREA";

    // Total supply of NEO tokens.
    private static final int NEO_TOTAL_SUPPLY = 100000000;

    private static final String NEXT_VALIDATORS_PREFIX = "0e";

    protected static Neow3j neow3j;

    @Container
    public static NeoTestContainer neoTestContainer = new NeoTestContainer();

    @BeforeAll
    public static void setUp() throws Exception {
        neow3j = Neow3j.build(new HttpService(neoTestContainer.getNodeUrl(), true));
        // open the wallet for JSON-RPC calls
        getNeow3j().openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", NEO_HASH, getNeow3j());
        // make a transaction that can be used for the tests
        txHashNeoTransfer = transferNeo(TX_RECIPIENT_1, TX_AMOUNT_NEO);
        transferGas(TX_RECIPIENT_1, TX_AMOUNT_GAS);
    }

    private static Hash256 transferNeo(Hash160 to, BigInteger amount) throws IOException {
        NeoSendToAddress send = getNeow3j()
                .sendToAddress(NEO_HASH, to, amount)
                .send();
        Hash256 txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
        return txHash;
    }

    private static Hash256 transferGas(Hash160 to, BigInteger amount) throws IOException {
        NeoSendToAddress send = getNeow3j()
                .sendToAddress(GAS_HASH, to, amount)
                .send();
        Hash256 txHash = send.getSendToAddress().getHash();
        // ensure that the transaction is sent
        waitUntilTransactionIsExecuted(txHash, getNeow3j());
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
                .getBlock(BLOCK_0_IDX, false)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX.longValue()));
        assertNull(block.getTransactions());
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlock(BLOCK_0_IDX, true)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), is(BLOCK_0_IDX.longValue()));
        assertNotNull(block.getTransactions());
        assertThat(block.getTransactions(), hasSize(0));
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlock(BLOCK_0_HASH, true)
                .send()
                .getBlock();

        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(BLOCK_0_IDX.longValue()));
        assertNotNull(block.getTransactions());
        assertTrue(block.getTransactions().isEmpty());
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlock(BLOCK_0_IDX)
                .send()
                .getRawBlock();

        assertThat(rawBlock, is(BLOCK_0_RAW_STRING));
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlock(BLOCK_0_HASH)
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
                .getBlockCount();

        assertNotNull(blockIndex);
        assertThat(blockIndex, greaterThan(BigInteger.valueOf(0)));
    }

    @Test
    public void testGetBlockHash() throws IOException {
        Hash256 blockHash = getNeow3j()
                .getBlockHash(BigInteger.ONE)
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
        assertThat(block.getIndex(), is(BLOCK_0_IDX.longValue()));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetBlockHeader_Index() throws IOException {
        NeoBlock block = getNeow3j()
                .getBlockHeader(BLOCK_0_IDX)
                .send()
                .getBlock();

        assertNotNull(block);
        assertNull(block.getTransactions());
        assertThat(block.getIndex(), is(BLOCK_0_IDX.longValue()));
        assertThat(block.getHash(), is(BLOCK_0_HASH));
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlockHeader(BLOCK_0_HASH)
                .send()
                .getRawBlock();

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        String rawBlock = getNeow3j()
                .getRawBlockHeader(BLOCK_0_IDX)
                .send()
                .getRawBlock();

        assertNotNull(rawBlock);
        assertThat(rawBlock, is(BLOCK_0_HEADER_RAW_STRING));
    }

    @Test
    public void testGetNativeContracts() throws IOException {
        List<NativeContractState> nativeContracts = getNeow3j()
                .getNativeContracts()
                .send()
                .getNativeContracts();

        assertThat(nativeContracts, hasSize(9));
        NativeContractState contractState1 = nativeContracts.get(0);
        assertThat(contractState1.getId(), is(-1));
        assertThat(contractState1.getHash(), is(new Hash160(contractManagementHash())));

        ContractNef nef1 = contractState1.getNef();
        assertThat(nef1.getMagic(), is(860243278L));
        assertThat(nef1.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef1.getSource(), is(""));
        assertThat(nef1.getTokens(), hasSize(0));
        assertThat(nef1.getScript(), is(
                "EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A="));
        assertThat(nef1.getChecksum(), is(1094259016L));

        ContractManifest manifest1 = contractState1.getManifest();
        assertThat(manifest1.getName(), is("ContractManagement"));
        assertThat(manifest1.getGroups(), hasSize(0));
        assertThat(manifest1.getSupportedStandards(), hasSize(0));

        ContractABI abi1 = manifest1.getAbi();
        assertThat(abi1.getMethods(), hasSize(11));
        assertThat(abi1.getMethods().get(10).getName(), is("update"));
        assertThat(abi1.getMethods().get(10).getParameters(), hasSize(3));
        assertThat(abi1.getMethods().get(10).getReturnType(), is(ContractParameterType.VOID));
        assertThat(abi1.getMethods().get(10).getOffset(), is(70));
        assertFalse(abi1.getMethods().get(10).isSafe());
        assertThat(abi1.getEvents(), hasSize(3));
        assertThat(abi1.getEvents().get(1).getName(), is("Update"));
        assertThat(abi1.getEvents().get(1).getParameters(), hasSize(1));
        assertThat(abi1.getEvents().get(1).getParameters().get(0).getName(), is("Hash"));
        assertThat(abi1.getEvents().get(1).getParameters().get(0).getType(), is(ContractParameterType.HASH160));

        assertThat(manifest1.getPermissions(), hasSize(1));
        assertThat(manifest1.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest1.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest1.getPermissions().get(0).getMethods().get(0), is("*"));
        assertThat(manifest1.getTrusts(), hasSize(0));
        assertNull(manifest1.getExtra());

        assertThat(contractState1.getUpdateHistory(), hasSize(1));
        assertThat(contractState1.getUpdateHistory().get(0), is(0));

        NativeContractState contractState8 = nativeContracts.get(8);
        assertThat(contractState8.getId(), is(-9));
        assertThat(contractState8.getHash(), is(new Hash160(oracleContractHash())));

        ContractNef nef8 = contractState8.getNef();
        assertThat(nef8.getMagic(), is(860243278L));
        assertThat(nef8.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef8.getSource(), is(""));
        assertThat(nef8.getTokens(), hasSize(0));
        assertThat(nef8.getScript(), is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A="));
        assertThat(nef8.getChecksum(), is(2663858513L));

        ContractManifest manifest8 = contractState8.getManifest();
        assertThat(manifest8.getName(), is("OracleContract"));
        assertThat(manifest8.getGroups(), hasSize(0));
        assertTrue(manifest8.getFeatures().isEmpty());
        assertThat(manifest8.getSupportedStandards(), hasSize(0));

        ContractABI abi8 = manifest8.getAbi();
        assertThat(abi8.getMethods(), hasSize(5));
        ContractMethod method = abi8.getMethods().get(3);
        assertThat(method.getName(), is("setPrice"));
        assertThat(method.getParameters(), hasSize(1));
        assertThat(method.getReturnType(), is(ContractParameterType.VOID));
        assertThat(method.getOffset(), is(21));
        assertFalse(method.isSafe());
        assertThat(abi8.getEvents(), hasSize(2));
        ContractEvent event = abi8.getEvents().get(0);
        assertThat(event.getName(), is("OracleRequest"));
        assertThat(event.getParameters(), hasSize(4));
        assertThat(event.getParameters().get(3).getName(), is("Filter"));
        assertThat(event.getParameters().get(3).getType(), is(ContractParameterType.STRING));

        assertThat(manifest8.getPermissions(), hasSize(1));
        assertThat(manifest8.getPermissions().get(0).getContract(), is("*"));
        assertThat(manifest8.getPermissions().get(0).getMethods(), hasSize(1));
        assertThat(manifest8.getPermissions().get(0).getMethods().get(0), is("*"));
        assertThat(manifest8.getTrusts(), hasSize(0));
        assertNull(manifest8.getExtra());

        assertThat(contractState8.getUpdateHistory(), hasSize(1));
        assertThat(contractState8.getUpdateHistory().get(0), is(0));
    }

    @Test
    public void testGetContractState() throws IOException {
        ContractState contractState = getNeow3j()
                .getContractState(new Hash160(neoTokenHash()))
                .send()
                .getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-5));
        assertThat(contractState.getHash(), is(NEO_HASH));
        ContractNef nef = contractState.getNef();
        assertThat(nef, is(notNullValue()));
        assertThat(nef.getMagic(), is(860243278L));
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getSource(), is(""));
        assertThat(nef.getTokens(), is(empty()));
        assertThat(nef.getScript(),
                is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQA=="));
        assertThat(nef.getChecksum(), is(65467259L));

        ContractManifest manifest = contractState.getManifest();
        assertNotNull(manifest);
        assertNotNull(manifest.getGroups());
        assertThat(manifest.getGroups(), hasSize(0));

        ContractABI abi = manifest.getAbi();
        assertNotNull(abi);

        assertNotNull(abi.getMethods());
        assertThat(abi.getMethods(), hasSize(19));
        ContractMethod method = abi.getMethods().get(10);
        assertThat(method.getName(), is("registerCandidate"));
        assertThat(method.getParameters().get(0).getName(), is("pubkey"));
        assertThat(method.getParameters().get(0).getType(),
                is(ContractParameterType.PUBLIC_KEY));
        assertThat(method.getOffset(), is(70));
        assertThat(method.getReturnType(), is(ContractParameterType.BOOLEAN));
        assertFalse(method.isSafe());

        assertNotNull(abi.getEvents());
        assertThat(abi.getEvents(), hasSize(3));
        ContractEvent event = abi.getEvents().get(0);
        assertThat(event.getName(), is("Transfer"));
        assertThat(event.getParameters(), hasSize(3));
        assertThat(event.getParameters().get(0).getName(), is("from"));
        assertThat(event.getParameters().get(0).getType(), is(ContractParameterType.HASH160));

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
                .getNativeContractState(gasTokenName())
                .send()
                .getContractState();

        assertNotNull(contractState);
        assertThat(contractState.getId(), is(-6));
        assertThat(contractState.getHash(), is(GAS_HASH));
        ContractNef nef = contractState.getNef();
        assertNotNull(nef);
        assertThat(nef.getMagic(), is(860243278L));
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getSource(), is(""));
        assertThat(nef.getTokens(), is(empty()));
        assertThat(nef.getScript(), is("EEEa93tnQBBBGvd7Z0AQQRr3e2dAEEEa93tnQBBBGvd7Z0A="));
        assertThat(nef.getChecksum(), is(2663858513L));

        ContractManifest manifest = contractState.getManifest();
        assertNotNull(manifest);
        assertThat(manifest.getName(), is(gasTokenName()));

        assertNotNull(manifest.getGroups());
        assertThat(manifest.getGroups(), hasSize(0));

        assertThat(manifest.getSupportedStandards(), hasSize(1));
        assertThat(manifest.getSupportedStandards().get(0), is("NEP-17"));

        ContractABI abi = manifest.getAbi();
        assertNotNull(abi);
        assertNotNull(abi.getMethods());
        assertThat(abi.getMethods(), hasSize(5));
        ContractMethod method = abi.getMethods().get(0);
        assertThat(method.getName(), is("balanceOf"));
        assertThat(method.getParameters(), hasSize(1));
        assertThat(method.getParameters().get(0).getName(), is("account"));
        assertThat(method.getParameters().get(0).getType(), is(ContractParameterType.HASH160));
        assertThat(method.getOffset(), is(0));
        assertThat(method.getReturnType(), is(ContractParameterType.INTEGER));
        assertTrue(method.isSafe());

        assertNotNull(abi.getEvents());
        assertThat(abi.getEvents(), hasSize(1));
        ContractEvent event = abi.getEvents().get(0);
        assertThat(event.getName(), is("Transfer"));
        assertThat(event.getParameters(), hasSize(3));
        assertThat(event.getParameters().get(2).getName(), is("amount"));
        assertThat(event.getParameters().get(2).getType(), is(ContractParameterType.INTEGER));

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
        waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

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
    public void testGetRawTransaction() throws IOException {
        waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        String rawTransaction = getNeow3j()
                .getRawTransaction(txHashNeoTransfer)
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

        assertThat(storage, is("QAFBAighAzpNBRsEt/wCMNKxqu39WoS+J5pTYac1jbZlrXhXeH8bIQA="));
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
                is("033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b"));
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
        NeoGetVersion.NeoVersion neoVersion = getNeow3j()
                .getVersion()
                .send()
                .getVersion();

        assertNotNull(neoVersion);
        assertThat(neoVersion.getUserAgent(), not(isEmptyString()));
        assertThat(neoVersion.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(neoVersion.getTCPPort(), is(greaterThanOrEqualTo(0)));
        assertThat(neoVersion.getWSPort(), is(greaterThanOrEqualTo(0)));

        NeoGetVersion.NeoVersion.Protocol protocol = neoVersion.getProtocol();
        assertThat(protocol.getValidatorsCount(), is(1));
        assertThat(protocol.getMilliSecondsPerBlock(), is(1_000L));
        assertThat(protocol.getMaxValidUntilBlockIncrement(), is(86_400L));
        assertThat(protocol.getMaxTraceableBlocks(), is(2_102_400L));
        assertThat(protocol.getAddressVersion(), is(53));
        assertThat(protocol.getMaxTransactionsPerBlock(), is(512L));
        assertThat(protocol.getMemoryPoolMaxTransactions(), is(50_000));
        assertThat(protocol.getInitialGasDistribution(),
                is(BigInteger.valueOf(5_200_000_000_000_000L)));
    }

    // SmartContract Methods

    @Test
    public void testGetUnclaimedGas() throws IOException {
        GetUnclaimedGas unclaimedGas = getNeow3j()
                .getUnclaimedGas(COMMITTEE_HASH)
                .send()
                .getUnclaimedGas();

        assertThat(unclaimedGas, is(notNullValue()));
        assertThat(unclaimedGas.getUnclaimed(), greaterThanOrEqualTo("100000000"));
        assertThat(unclaimedGas.getAddress(), is(committeeAccountAddress()));
    }

    @Test
    public void testInvokeFunction_empty_Params() throws IOException {
        InvocationResult result = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_SYMBOL)
                .send()
                .getInvocationResult();

        assertThat(result.getStack().get(0).getString(), is("NEO"));
    }

    @Test
    public void testInvokeFunctionWithBalanceOf() throws IOException {
        List<ContractParameter> parameters =
                singletonList(hash160(new Hash160(committeeAccountScriptHash())));

        InvocationResult invocResult = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_BALANCE, parameters)
                .send()
                .getInvocationResult();

        assertNotNull(invocResult);
        assertTrue(invocResult.getNotifications().isEmpty());
    }

    @Test
    public void testInvokeFunctionWithTransfer() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                hash160(new Hash160(committeeAccountScriptHash())),
                hash160(new Hash160(defaultAccountScriptHash())),
                integer(1),
                integer(1));
        Signer signer = AccountSigner.calledByEntry(new Hash160(committeeAccountScriptHash()))
                .setAllowedContracts(NEO_HASH);

        InvocationResult invoc = getNeow3j()
                .invokeFunction(NEO_HASH, "transfer", params, signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(NeoVMStateType.HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertThat(invoc.getNotifications(), hasSize(2));
        assertNull(invoc.getDiagnostics());
        assertTrue(invoc.getStack().get(0).getBoolean());
        assertNotNull(invoc.getTx());
    }

    @Test
    public void testInvokeFunctionWithDiagnostics() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                hash160(new Hash160(committeeAccountScriptHash())),
                hash160(new Hash160(defaultAccountScriptHash())),
                integer(1),
                integer(1));
        Signer signer = AccountSigner.calledByEntry(new Hash160(committeeAccountScriptHash()))
                .setAllowedContracts(NEO_HASH);

        InvocationResult invoc = getNeow3j()
                .invokeFunctionDiagnostics(NEO_HASH, "transfer", params, signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(NeoVMStateType.HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertThat(invoc.getNotifications(), hasSize(2));
        assertNotNull(invoc.getDiagnostics());
        assertTrue(invoc.getStack().get(0).getBoolean());
        assertNotNull(invoc.getTx());
    }

    @Test
    public void testInvokeScript() throws IOException {
        List<ContractParameter> params = asList(
                hash160(new Hash160(committeeAccountScriptHash())),
                hash160(new Hash160(defaultAccountScriptHash())),
                integer(10),
                any(null));
        byte[] script = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "transfer", params)
                .toArray();

        AccountSigner signer = calledByEntry(new Hash160(committeeAccountScriptHash()));
        InvocationResult invoc = getNeow3j()
                .invokeScript(Numeric.toHexString(script), signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertThat(invoc.getScript(), is(Base64.encode(script)));
        assertThat(invoc.getState(), is(NeoVMStateType.HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertNotNull(invoc.getNotifications());
        assertNull(invoc.getDiagnostics());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
    }

    @Test
    public void testInvokeScriptDiagnostics() throws IOException {
        List<ContractParameter> params = asList(
                hash160(new Hash160(committeeAccountScriptHash())),
                hash160(new Hash160(defaultAccountScriptHash())),
                integer(10),
                any(null));
        byte[] script = new ScriptBuilder()
                .contractCall(new Hash160(neoTokenHash()), "transfer", params)
                .toArray();

        AccountSigner signer = calledByEntry(new Hash160(committeeAccountScriptHash()));
        InvocationResult invoc = getNeow3j()
                .invokeScriptDiagnostics(Numeric.toHexString(script), signer)
                .send()
                .getInvocationResult();

        assertNotNull(invoc);
        assertThat(invoc.getScript(), is(Base64.encode(script)));
        assertThat(invoc.getState(), is(NeoVMStateType.HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
        assertNotNull(invoc.getNotifications());
        assertNotNull(invoc.getDiagnostics());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
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
                .validateAddress(committeeAccountAddress())
                .send()
                .getValidation();

        assertThat(validation.getAddress(), is(committeeAccountAddress()));
        assertTrue(validation.getValid());
        assertTrue(validation.isValid());
    }

    // Wallet

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
                .dumpPrivKey(COMMITTEE_HASH)
                .send()
                .getDumpPrivKey();

        assertThat(dumpPrivKey, not(isEmptyOrNullString()));
        assertThat(dumpPrivKey, is(defaultAccountWIF()));
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
                .importPrivKey(defaultAccountWIF())
                .send()
                .getAddresses();

        assertThat(privKey.getAddress(), is(defaultAccountAddress()));
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

        assertThat(networkFee.getNetworkFee(), is(new BigInteger("136000")));
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

    // TokenTracker: Nep17

    @Test
    public void testGetNep17Transfers() throws IOException {
        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
                .getNep17Transfers(COMMITTEE_HASH)
                .send()
                .getNep17Transfers();

        assertNotNull(nep17Transfers.getSent());
        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(NEO_HASH));
        assertThat(transfer.getTransferAddress(), is(TX_RECIPIENT_1.toAddress()));
        assertThat(transfer.getAmount(), is(TX_AMOUNT_NEO));
        assertThat(transfer.getBlockIndex(), greaterThanOrEqualTo(1L));
        assertThat(transfer.getTransferNotifyIndex(), is(0L));

        assertNotNull(nep17Transfers.getReceived());
        assertThat(nep17Transfers.getReceived().size(), greaterThanOrEqualTo(1));
        transfer = nep17Transfers.getReceived().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
        assertThat(transfer.getAssetHash(), is(GAS_HASH));
        assertNull(transfer.getTransferAddress());
        assertThat(transfer.getAmount(), greaterThanOrEqualTo(new BigInteger("50000000")));
        assertThat(transfer.getBlockIndex(), greaterThanOrEqualTo(1L));
        assertThat(transfer.getTransferNotifyIndex(), is(1L));
        assertThat(transfer.getTxHash(), instanceOf(Hash256.class));
    }

    @Test
    public void testGetNep17Transfers_Date() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
                .getNep17Transfers(COMMITTEE_HASH, calendar.getTime())
                .send()
                .getNep17Transfers();

        assertNotNull(nep17Transfers.getSent());
        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep17Transfers_DateFromTo() throws IOException {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.HOUR_OF_DAY, -1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.HOUR_OF_DAY, 1);

        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
                .getNep17Transfers(COMMITTEE_HASH, from.getTime(), to.getTime())
                .send()
                .getNep17Transfers();

        assertNotNull(nep17Transfers.getSent());
        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    public void testGetNep17Balances() throws IOException {
        Nep17Balances nep17Balances = getNeow3j()
                .getNep17Balances(COMMITTEE_HASH)
                .send()
                .getBalances();

        assertNotNull(nep17Balances);
        assertThat(nep17Balances.getAddress(), is(committeeAccountAddress()));

        List<NeoGetNep17Balances.Nep17Balance> balanceList = nep17Balances.getBalances();
        assertNotNull(balanceList);
        assertThat(balanceList, hasSize(2));

        assertThat(balanceList.get(0).getAssetHash(), is(GAS_HASH));
        assertThat(balanceList.get(0).getName(), is("GasToken"));
        assertThat(balanceList.get(0).getSymbol(), is("GAS"));
        assertThat(balanceList.get(0).getDecimals(), is("8"));
        assertNotNull(balanceList.get(0).getAmount());
        assertThat(balanceList.get(0).getLastUpdatedBlock(), is(greaterThanOrEqualTo(BigInteger.ZERO)));

        assertThat(balanceList.get(1).getAssetHash(), is(NEO_HASH));
        assertThat(balanceList.get(1).getName(), is("NeoToken"));
        assertThat(balanceList.get(1).getSymbol(), is("NEO"));
        assertThat(balanceList.get(1).getDecimals(), is("0"));
        assertNotNull(balanceList.get(1).getAmount());
        assertThat(balanceList.get(1).getLastUpdatedBlock(), is(greaterThanOrEqualTo(BigInteger.ZERO)));
    }

    // TokenTracker: Nep11

    @Test
    // TODO: 04.12.21 see https://github.com/neow3j/neow3j/issues/690
    @Disabled("See: https://github.com/neow3j/neow3j/issues/690")
    public void testGetNep11Transfers() throws IOException {
//        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
//                .getNep17Transfers(COMMITTEE_HASH)
//                .send()
//                .getNep17Transfers();
//
//        assertNotNull(nep17Transfers.getSent());
//        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
//        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
//        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
//        assertThat(transfer.getAssetHash(), is(NEO_HASH));
//        assertThat(transfer.getTransferAddress(), is(TX_RECIPIENT_1.toAddress()));
//        assertThat(transfer.getAmount(), is(TX_AMOUNT_NEO));
//        assertThat(transfer.getBlockIndex(), greaterThanOrEqualTo(1L));
//        assertThat(transfer.getTransferNotifyIndex(), is(1L));
//
//        assertNotNull(nep17Transfers.getReceived());
//        assertThat(nep17Transfers.getReceived().size(), greaterThanOrEqualTo(1));
//        transfer = nep17Transfers.getReceived().get(0);
//        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
//        assertThat(transfer.getAssetHash(), is(GAS_HASH));
//        assertNull(transfer.getTransferAddress());
//        assertThat(transfer.getAmount(), greaterThanOrEqualTo(new BigInteger("50000000")));
//        assertThat(transfer.getBlockIndex(), greaterThanOrEqualTo(1L));
//        assertThat(transfer.getTransferNotifyIndex(), is(0L));
//        assertThat(transfer.getTxHash(), instanceOf(Hash256.class));
    }

    @Test
    // TODO: 04.12.21 see https://github.com/neow3j/neow3j/issues/690
    @Disabled("See: https://github.com/neow3j/neow3j/issues/690")
    public void testGetNep11Transfers_Date() throws IOException {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, -1);
//
//        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
//                .getNep17Transfers(COMMITTEE_HASH, calendar.getTime())
//                .send()
//                .getNep17Transfers();
//
//        assertNotNull(nep17Transfers.getSent());
//        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
//        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
//        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    // TODO: 04.12.21 see https://github.com/neow3j/neow3j/issues/690
    @Disabled("See: https://github.com/neow3j/neow3j/issues/690")
    public void testGetNep11Transfers_DateFromTo() throws IOException {
//        Calendar from = Calendar.getInstance();
//        from.add(Calendar.HOUR_OF_DAY, -1);
//        Calendar to = Calendar.getInstance();
//        to.add(Calendar.HOUR_OF_DAY, 1);
//
//        NeoGetNep17Transfers.Nep17Transfers nep17Transfers = getNeow3j()
//                .getNep17Transfers(COMMITTEE_HASH, from.getTime(), to.getTime())
//                .send()
//                .getNep17Transfers();
//
//        assertNotNull(nep17Transfers.getSent());
//        assertThat(nep17Transfers.getSent().size(), greaterThanOrEqualTo(1));
//        NeoGetNep17Transfers.Nep17Transfer transfer = nep17Transfers.getSent().get(0);
//        assertThat(transfer.getTimestamp(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    // TODO: 04.12.21 see https://github.com/neow3j/neow3j/issues/690
    @Disabled("See: https://github.com/neow3j/neow3j/issues/690")
    public void testGetNep11Balances() throws IOException {
//        Nep17Balances balances = getNeow3j()
//                .getNep17Balances(COMMITTEE_HASH)
//                .send()
//                .getBalances();
//
//        assertNotNull(balances);
//        assertThat(balances.getAddress(), is(committeeAccountAddress()));
//        assertNotNull(balances.getBalances());
//        assertThat(balances.getBalances(), hasSize(2));
//        assertThat(balances.getBalances().get(0).getAssetHash(), is(GAS_HASH));
//        assertNotNull(balances.getBalances().get(0).getAmount());
//        assertThat(balances.getBalances().get(0).getLastUpdatedBlock(),
//                is(greaterThanOrEqualTo(new BigInteger("0"))));
//        assertThat(balances.getBalances().get(1).getAssetHash(), is(NEO_HASH));
//        assertNotNull(balances.getBalances().get(1).getAmount());
//        assertNotNull(balances.getBalances().get(1).getLastUpdatedBlock());
    }

    @Test
    // TODO: 04.12.21 see https://github.com/neow3j/neow3j/issues/690
    @Disabled("See: https://github.com/neow3j/neow3j/issues/690")
    public void testGetNep11Attributes() throws IOException {

    }

    // ApplicationLogs

    @Test
    public void testGetApplicationLog() throws IOException {
        waitUntilTransactionIsExecuted(txHashNeoTransfer, neow3j);

        NeoApplicationLog applicationLog = getNeow3j()
                .getApplicationLog(txHashNeoTransfer)
                .send()
                .getApplicationLog();

        assertNotNull(applicationLog);
        assertThat(applicationLog.getTransactionId(), is(txHashNeoTransfer));
        assertThat(applicationLog.getExecutions(), hasSize(1));

        NeoApplicationLog.Execution execution = applicationLog.getExecutions().get(0);
        assertThat(execution.getTrigger(), is(APPLICATION_LOG_TRIGGER));
        assertThat(execution.getState(), is(NeoVMStateType.HALT));
        assertNull(execution.getException());
        assertThat(execution.getGasConsumed(), is("9977780"));
        assertNotNull(execution.getStack());
        assertThat(execution.getStack(), hasSize(0));

        assertNotNull(execution.getNotifications());
        assertThat(execution.getNotifications(), hasSize(greaterThanOrEqualTo(1)));
        assertThat(execution.getNotifications().get(0).getContract(),
                isOneOf(NEO_HASH, GAS_HASH));
        assertThat(execution.getNotifications().get(0).getEventName(), is("Transfer"));

        StackItem state = execution.getNotifications().get(1).getState();
        assertThat(state, is(notNullValue()));
        assertThat(state.getType(), is(StackItemType.ARRAY));
        assertThat(state.getList(), hasSize(3));
        assertThat(state.getList().get(0).getType(), is(StackItemType.ANY));
        assertThat(state.getList().get(1).getType(), is(StackItemType.BYTE_STRING));
        assertThat(state.getList().get(1).getAddress(), is(committeeAccountAddress()));
        assertThat(state.getList().get(2).getType(), is(StackItemType.INTEGER));
        assertThat(state.getList().get(2).getInteger(), greaterThanOrEqualTo(new BigInteger("50000000")));
    }

    // StateService

    @Test
    public void testGetStateRoot() throws IOException {
        StateRoot stateRoot = getNeow3j()
                .getStateRoot(0L)
                .send()
                .getStateRoot();

        assertThat(stateRoot.getVersion(), is(0));
        assertThat(stateRoot.getIndex(), is(0L));
        assertThat(stateRoot.getRootHash(), instanceOf(Hash256.class));
        assertThat(stateRoot.getRootHash(), not(Hash256.ZERO));
        assertThat(stateRoot.getWitnesses(), hasSize(0));
    }

    @Test
    public void testGetProof() throws IOException {
        long localRootIndex = 2L;
        String proof = null;
        while (localRootIndex < 5) {
            Hash256 rootHash = getNeow3j()
                    .getStateRoot(localRootIndex)
                    .send()
                    .getStateRoot()
                    .getRootHash();

            NeoGetProof response = getNeow3j().getProof(rootHash, NEO_HASH, NEXT_VALIDATORS_PREFIX).send();
            if (!response.hasError()) {
                proof = response.getProof();
                break;
            }
            localRootIndex++;
        }
        assertNotNull(proof);
    }

    @Test
    public void testVerifyProof() throws IOException {
        String storage = getNeow3j()
                .verifyProof(
                        new Hash256(
                                "0x1cd846c74d5f78565c7c50957330147e5c0ee7e298f1dabd43a5651e00c41fac"),
                        "05fdffffff0e062401010f03e20708edd777e5afb3ddbaaed0a6cd78793086975c43897f4ec5361c36ef8930d200040404040404040403b97ce0716d448e55d75b39f2f03ef02679180ac52e419460b3bdf112da31a6260393049f1d2ea67548e395d3a6850346fc2b94f0f9f6e5b2f5a22b752e13d03d9c040366ddb2164ca79a31da0a1500e323be8c4d21658eaa326844fbba64ec3691d8fa03cbaa546787bdeaba7529da84f04b9206595fd633e1bd02b5a95f554d7e37e11b03fadfbaa3addaca4f81a78b8d665c20a21afbbbe5605bf977c2821eb5da5726ee040342eefd9168b302445d5780ba52ae2d762f62302fd65c8f3e4b662681e671ca36042901060f0f0f0f0f0f03b30f4847ae191de2be4bfe41aca92bdab8f54ca6558fd1c4d92d7bb73adf4cf45200037b51dba93dcf3e1b8d547b52c05f34a1d4f840ac205d70d73dfe28aae2471d17032509026b74fac8e7415ae27831390c22ef239f554ef5c19b30ad7151ea9bb0b404040404040404040404040404040472000403bf8b4ccf825a4db3c123889eedf9dcabf4640aaa4f2e74dfd425295f3e9f61db04040404040404040403e0f5608876eb84737d1e70b991a63da5884450400409171a91ec7e9759611db6040403281489452008c08da47651d740cb3ff679e2100cd5bddd8058cf5f1633b22bd504042d022b2940014102282102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60210000")
                .send()
                .verifyProof();

        assertThat(storage, is("KUABQQIoIQIWOUahM+PS4NmH+5DLAbBg7ReA8XGOLaKO3xO5Zf0rYCEAAA=="));
    }

    @Test
    public void testGetStateHeight() throws IOException {
        StateHeight stateHeight = getNeow3j()
                .getStateHeight()
                .send()
                .getStateHeight();

        assertThat(stateHeight.getLocalRootIndex(), greaterThanOrEqualTo(0L));
        assertThat(stateHeight.getValidatedRootIndex(), greaterThanOrEqualTo(0L));
    }

    @Test
    public void testGetState() throws IOException {
        long stateHeight = getNeow3j().getStateHeight().send()
                .getStateHeight().getLocalRootIndex();
        Hash256 rootHash = getNeow3j().getStateRoot(stateHeight).send()
                .getStateRoot().getRootHash();
        String state = getNeow3j().getState(rootHash, new Hash160(gasTokenHash()),
                "140d165c9899c38bbf5991c5e47b04937258caec69").send().getState();

        assertNotNull(state);
    }

    @Test
    public void testFindStates() throws IOException {
        String keyPrefix = "14";
        Hash160 contractHash = new Hash160(gasTokenHash());

        long stateHeight = getNeow3j().getStateHeight().send().getStateHeight().getLocalRootIndex();
        Hash256 rootHash = getNeow3j().getStateRoot(stateHeight).send().getStateRoot().getRootHash();
        NeoFindStates.States states = getNeow3j()
                .findStates(rootHash, contractHash, keyPrefix).send()
                .getStates();

        assertThat(states.getResults(), hasSize(3));
        assertNotNull(states.getFirstProof());
        assertNotNull(states.getLastProof());
        assertFalse(states.isTruncated());
        assertNotNull(states.getResults().get(2));
    }

    @Test
    public void testFindStates_startKey() throws IOException {
        String keyPrefix = "14";
        String startKey = "147f";
        Hash160 contractHash = new Hash160(gasTokenHash());

        long stateHeight = getNeow3j().getStateHeight().send().getStateHeight().getLocalRootIndex();
        Hash256 rootHash = getNeow3j().getStateRoot(stateHeight).send().getStateRoot().getRootHash();
        NeoFindStates.States states = getNeow3j()
                .findStates(rootHash, contractHash, keyPrefix, startKey).send()
                .getStates();

        assertNotNull(states.getFirstProof());
        assertNull(states.getLastProof()); // A last proof is only included if there are more than 1 result.
        assertFalse(states.isTruncated());
        assertThat(states.getResults(), hasSize(1));
        assertNotNull(states.getResults().get(0).getKey());
        assertNotNull(states.getResults().get(0).getValue());
    }

    @Test
    public void testFindStates_count() throws IOException {
        long stateHeight = getNeow3j().getStateHeight().send()
                .getStateHeight().getLocalRootIndex();
        Hash256 rootHash = getNeow3j().getStateRoot(stateHeight).send()
                .getStateRoot().getRootHash();
        String keyPrefix = "14";
        Hash160 contractHash = new Hash160(gasTokenHash());
        NeoFindStates.States states = getNeow3j()
                .findStates(rootHash, contractHash, keyPrefix, 1).send()
                .getStates();
        assertNotNull(states.getFirstProof());
        assertNull(states.getLastProof());
        assertTrue(states.isTruncated());
        assertThat(states.getResults(), hasSize(1));
        assertNotNull(states.getResults().get(0).getKey());
        assertNotNull(states.getResults().get(0).getValue());
    }

    @Test
    public void testFindStates_startKeyAndCount() throws IOException {
        long stateHeight = getNeow3j().getStateHeight().send()
                .getStateHeight().getLocalRootIndex();
        Hash256 rootHash = getNeow3j().getStateRoot(stateHeight).send()
                .getStateRoot().getRootHash();
        String keyPrefix = "14";
        String startKey = "147f";
        Hash160 contractHash = new Hash160(gasTokenHash());
        NeoFindStates.States states = getNeow3j()
                .findStates(rootHash, contractHash, keyPrefix, startKey, 1).send()
                .getStates();
        assertNotNull(states.getFirstProof());
        assertNull(states.getLastProof());
        assertFalse(states.isTruncated());
        assertThat(states.getResults().size(), greaterThanOrEqualTo(1));
        assertNotNull(states.getResults().get(0).getKey());
        assertNotNull(states.getResults().get(0).getValue());
    }

}
