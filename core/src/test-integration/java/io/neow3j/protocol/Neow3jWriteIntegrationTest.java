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
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
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
//        io.neow3j.transaction.Transaction tx = new GasToken(neow)
//                .transferFromSpecificAccounts(
//                      w, a.getAddress(), BigDecimal.TEN, committee.getScriptHash())
//                .validUntilBlock(1000)
//                .sign();

        NeoSendRawTransaction sendRawTransaction = getNeow3j().sendRawTransaction(
                "00ccc7576a968b970000000000acd2120000000000e80300000100159d24baf9a34b3607bb86486a0c6b39a73f4801005a0b0200ca9a3b0c14620c0e6f7f9326ab7aab5f6815e5bfb5cded36030c1400159d24baf9a34b3607bb86486a0c6b39a73f4814c01f0c087472616e736665720c14cf76e28bd0062c4a478ee35561011319f3cfa4d241627d5b5201420c40ac3c4a6ddf30373023ea6930ea4fd1952a339b2ab9dfe29b42746e27f52f4613c973cc90ae278f4a2ef21d5db0b8faba28b10a95d541e31ff729a4df1afe22b02a110c21036cfcc5d0550d0481b66f58e25067280f042b4933fc013dc4930ce2a4194c9d9411417bce6ca5")
                .send();
        RawTransaction rawTx = sendRawTransaction.getSendRawTransaction();
        Hash256 hash = rawTx.getHash();
        assertThat(hash.toString(),
                is("c7fa8aab1e5d64e5bde866826dc3362a203bd32c5617ee644651aacf57145825"));
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
        TransactionSendAsset txSendAsset =
                new TransactionSendAsset(NEO_HASH, "10",
                        defaultAccountAddress());
        NeoSendFrom sendFrom = getNeow3j()
                .sendFrom(COMMITTEE_HASH, txSendAsset)
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
                        new TransactionSendAsset(NEO_HASH, "100", defaultAccountAddress()),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT)))
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
                        new TransactionSendAsset(NEO_HASH, "100", defaultAccountAddress()),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT)))
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
        TransactionSendAsset transactionSendAsset = new TransactionSendAsset(NEO_HASH, "10",
                defaultAccountAddress());
        NeoSendToAddress sendToAddress = getNeow3j()
                .sendToAddress(transactionSendAsset)
                .send();

        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

}
