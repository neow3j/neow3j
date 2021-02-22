package io.neow3j.protocol;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_1_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.ACCOUNT_2_ADDRESS;
import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PASSWORD;
import static io.neow3j.protocol.IntegrationTestHelper.NODE_WALLET_PATH;
import static io.neow3j.protocol.IntegrationTestHelper.VM_STATE_HALT;
import static io.neow3j.protocol.IntegrationTestHelper.setupPrivateNetContainer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.ScriptHash;
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
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

// This test class spins up a new private net container for each test. This consumes a lot of time
// but allows the tests to make changes without interfering with each other.
public class Neow3jWriteIntegrationTest {

    private static final String NEO_TOKEN_HASH = "0xf61eebf573ea36593fd43aa150c055ad7906ab83";

    // Before the tests 5 NEO is sent to the RECIPIENT_1 address.
    // wif KxwrYazXiCdK33JEddpwHbXTpAYyhXC1YyC4SXTVF6GLRPBuVBFb
    private static final String RECIPIENT_1 = "NhixBNjEBvgyk18RzuXJt1T3BpqgAwANSA";
    // wif KzreXMT3AtLEYpqpZB9VPiw7RB75T3jGZ6cY6pv9QY2Sjqihv7M8
    protected static final String RECIPIENT_2 = "NdUQqdbzHTxyGMUDRhKBbPUbSu2HMLKBfN";

    protected static final int INVALID_PARAMS_CODE = -32602;
    protected static final String INVALID_PARAMS_MESSAGE = "Invalid params";

    private Neow3j neow3j;

    @Rule
    public GenericContainer<?> privateNetContainer = setupPrivateNetContainer();

    @Before
    public void setUp() throws IOException {
        neow3j = Neow3j.build(new HttpService(
                IntegrationTestHelper.getNodeUrl(privateNetContainer)));
        // open the wallet for JSON-RPC calls
        neow3j.openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo(
                "1", new ScriptHash(NEO_TOKEN_HASH), neow3j);
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction sendRawTransaction = neow3j.sendRawTransaction(
                "000b0a96844a959800000000002ac312000000000081160000017afd203255cb2972bd0a6a827e74e387ed322bec0100550b120c14dc84704b8283397326095c0b4e9662282c3a73190c147afd203255cb2972bd0a6a827e74e387ed322bec14c00c087472616e736665720c14b6720fef7e7eb73f25afb470f587997ce3e2460a41627d5b5201420c40a4ccb42bb7ed4a7217a204b836f372aa33c33e93b123e061805c064b23269bd5acadde1d10c60d83e74605def6a3f3d9a53d94e63b299ebb2388e1bbb779a1e52b110c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60110b41138defaf")
                .send();

        RawTransaction rawTx = sendRawTransaction.getSendRawTransaction();
        String hash = rawTx.getHash();
        assertNotNull(hash);
    }

    @Ignore("Future work, act as a consensus to submit blocks.")
    @Test
    public void testSubmitBlock() throws IOException {
        NeoSubmitBlock submitBlock = neow3j
                .submitBlock("")
                .send();
        Boolean getSubmitBlock = submitBlock.getSubmitBlock();

        assertTrue(getSubmitBlock);
    }

    // SmartContract Methods

    // Wallet Methods

    @Test
    public void testSendFrom() throws IOException {
        NeoSendFrom sendFrom = neow3j.sendFrom(ACCOUNT_1_ADDRESS, NEO_HASH, RECIPIENT_1, "10")
                .send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws IOException {
        TransactionSendAsset txSendAsset = new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_1);
        NeoSendFrom sendFrom = neow3j.sendFrom(ACCOUNT_1_ADDRESS, txSendAsset).send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));
    }

    @Test
    public void testSendMany() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(
                Arrays.asList(
                        new TransactionSendAsset(NEO_HASH, "100", RECIPIENT_1),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_2)
                )
        ).send();

        assertNotNull(sendMany.getSendMany());
        Transaction tx = sendMany.getSendMany();

        assertNotNull(tx.getHash());
        assertNotNull(tx.getNonce());
        assertThat(tx.getSender(), is(ACCOUNT_2_ADDRESS));
    }

    @Test
    public void testSendManyWithFrom() throws IOException {
        NeoSendMany response = neow3j.sendMany(
                ACCOUNT_1_ADDRESS,
                Arrays.asList(
                        new TransactionSendAsset(NEO_HASH, "100", RECIPIENT_1),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_2)
                )
        ).send();

        assertNotNull(response.getSendMany());
        Transaction tx = response.getSendMany();
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));

        Await.waitUntilTransactionIsExecuted(response.getSendMany().getHash(), neow3j);
        Execution execution = neow3j.getApplicationLog(response.getSendMany().getHash())
                .send().getApplicationLog().getExecutions().get(0);
        assertThat(execution.getState(), is(VM_STATE_HALT));

        ScriptHash recipient2ScriptHash = ScriptHash.fromAddress(RECIPIENT_2);
        assertThat(neow3j.invokeFunction(
                NEO_HASH, "balanceOf", Arrays.asList(hash160(recipient2ScriptHash)))
                        .send().getInvocationResult().getStack().get(0)
                        .asInteger().getValue().intValue(),
                is(10));
    }

    @Test
    public void testSendMany_Empty_Transaction() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(
                Arrays.asList()).send();

        assertNull(sendMany.getSendMany());
        assertNotNull(sendMany.getError());
        assertThat(sendMany.getError().getCode(), is(INVALID_PARAMS_CODE));
        assertThat(sendMany.getError().getMessage(), is(INVALID_PARAMS_MESSAGE));
    }

    @Test
    public void testSendToAddress() throws IOException {
        NeoSendToAddress sendToAddress = neow3j.sendToAddress(NEO_HASH, RECIPIENT_1, "10")
                .send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws IOException {
        TransactionSendAsset transactionSendAsset = new TransactionSendAsset(NEO_HASH, "10",
                RECIPIENT_1);
        NeoSendToAddress sendToAddress = neow3j.sendToAddress(transactionSendAsset).send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }
}
