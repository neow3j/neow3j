package io.neow3j.protocol;

import io.neow3j.NeoTestContainer;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.model.types.NeoVMStateType;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction.RawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionSendToken;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Await;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.NeoTestContainer.getNodeUrl;
import static io.neow3j.TestProperties.committeeAccountAddress;
import static io.neow3j.TestProperties.defaultAccountAddress;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.protocol.IntegrationTestHelper.COMMITTEE_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.DEFAULT_ACCOUNT_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// This test class spins up a new private net container for each test. This consumes a lot of time
// but allows the tests to make changes without interfering with each other.
public class Neow3jWriteIntegrationTest {

    // wif KzreXMT3AtLEYpqpZB9VPiw7RB75T3jGZ6cY6pv9QY2Sjqihv7M8
    protected static final String RECIPIENT = "NdUQqdbzHTxyGMUDRhKBbPUbSu2HMLKBfN";

    protected static final int INVALID_PARAMS_CODE = -32602;
    protected static final String INVALID_PARAMS_MESSAGE = "Invalid params";

    private static Neow3j neow3j;

    @Rule
    public NeoTestContainer neoTestContainer = new NeoTestContainer();

    @Before
    public void setUp() throws IOException {
        neow3j = Neow3j.build(new HttpService(getNodeUrl(neoTestContainer)));
        // open the wallet for JSON-RPC calls
        getNeow3j().openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", NEO_HASH,
                getNeow3j());
    }

    private static Neow3j getNeow3j() {
        return neow3j;
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        // Tx generated from the following code with a local neo-node running on the first few
        // blocks:
//        io.neow3j.transaction.Transaction tx = new GasToken(neow3j)
//                .transferFromSpecificAccounts(
//                        w, client1.getScriptHash(), BigInteger.TEN, committee.getScriptHash())
//                .validUntilBlock(1000)
//                .sign();

        NeoSendRawTransaction sendRawTransaction = getNeow3j().sendRawTransaction(
                "0x00ffb88f68963f9800000000000cc3120000000000e8030000017f65d434362708b255f0e06856bdcb5ce99d85050100560b1a0c1463bfef60905cd87c56da4064644be6f7ae8ec4880c147f65d434362708b255f0e06856bdcb5ce99d850514c01f0c087472616e736665720c14cf76e28bd0062c4a478ee35561011319f3cfa4d241627d5b5201420c4085add1b6dbeb3e64da6469eb078d87604bc4ae901a89106709b50e521081d1693f5dc52ffd6983c7359a7c6c06bd9a54abd93659f8f245b4245329568af3c1db2a110c21033a4d051b04b7fc0230d2b1aaedfd5a84be279a5361a7358db665ad7857787f1b11419ed0dc3a")
                .send();
        RawTransaction rawTx = sendRawTransaction.getSendRawTransaction();
        Hash256 hash = rawTx.getHash();
        assertThat(hash, is(
                new Hash256("0xda5a53a79ac399e07c6eea366c192a4942fa930d6903ffc10b497f834a538fee")));
    }

    @Ignore("Future work, act as a consensus to submit blocks.")
    @Test
    public void testSubmitBlock() throws IOException {
        NeoSubmitBlock submitBlock = getNeow3j()
                .submitBlock("")
                .send();

        Boolean getSubmitBlock = submitBlock.getSubmitBlock();

        assertTrue(getSubmitBlock);
    }

    // SmartContract Methods

    // Wallet Methods

    @Test
    public void testSendFrom() throws IOException {
        NeoSendFrom sendFrom = getNeow3j()
                .sendFrom(NEO_HASH, COMMITTEE_HASH, DEFAULT_ACCOUNT_HASH, BigInteger.TEN)
                .send();

        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(committeeAccountAddress()));
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws IOException {
        TransactionSendToken txSendToken =
                new TransactionSendToken(NEO_HASH, BigInteger.TEN, defaultAccountAddress());
        NeoSendFrom sendFrom = getNeow3j()
                .sendFrom(COMMITTEE_HASH, txSendToken)
                .send();

        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(committeeAccountAddress()));
    }

    @Test
    public void testSendMany() throws IOException {
        NeoSendMany sendMany = getNeow3j()
                .sendMany(asList(
                        new TransactionSendToken(NEO_HASH, new BigInteger("100"),
                                defaultAccountAddress()),
                        new TransactionSendToken(NEO_HASH, BigInteger.TEN, RECIPIENT)))
                .send();

        assertNotNull(sendMany.getSendMany());

        Transaction tx = sendMany.getSendMany();

        assertNotNull(tx.getHash());
        assertNotNull(tx.getNonce());
        assertThat(tx.getSender(), is(defaultAccountAddress()));
    }

    @Test
    public void testSendManyWithFrom() throws IOException {
        NeoSendMany response = getNeow3j()
                .sendMany(COMMITTEE_HASH, asList(
                        new TransactionSendToken(NEO_HASH, new BigInteger("100"),
                                defaultAccountAddress()),
                        new TransactionSendToken(NEO_HASH, BigInteger.TEN, RECIPIENT)))
                .send();

        assertNotNull(response.getSendMany());
        Transaction tx = response.getSendMany();
        assertThat(tx.getSender(), is(committeeAccountAddress()));

        Await.waitUntilTransactionIsExecuted(response.getSendMany().getHash(), neow3j);
        Execution execution = neow3j.getApplicationLog(response.getSendMany().getHash())
                .send().getApplicationLog().getExecutions().get(0);
        assertThat(execution.getState(), is(NeoVMStateType.HALT));

        Hash160 recipient2Hash160 = Hash160.fromAddress(RECIPIENT);
        assertThat(neow3j.invokeFunction(
                NEO_HASH, "balanceOf", singletonList(hash160(recipient2Hash160))).send()
                        .getInvocationResult().getStack().get(0).getInteger().intValue(),
                is(10));
    }

    @Test
    public void testSendMany_Empty_Transaction() throws IOException {
        NeoSendMany sendMany = getNeow3j()
                .sendMany(emptyList())
                .send();

        assertNull(sendMany.getSendMany());
        assertNotNull(sendMany.getError());
        assertThat(sendMany.getError().getCode(), is(INVALID_PARAMS_CODE));
        assertThat(sendMany.getError().getMessage(), is(INVALID_PARAMS_MESSAGE));
    }

    @Test
    public void testSendToAddress() throws IOException {
        NeoSendToAddress sendToAddress = getNeow3j()
                .sendToAddress(NEO_HASH, DEFAULT_ACCOUNT_HASH, BigInteger.TEN)
                .send();

        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws IOException {
        TransactionSendToken transactionSendToken = new TransactionSendToken(NEO_HASH,
                BigInteger.TEN, defaultAccountAddress());
        NeoSendToAddress sendToAddress = getNeow3j()
                .sendToAddress(transactionSendToken)
                .send();

        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

}
