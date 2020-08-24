package io.neow3j.protocol;

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

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

// This test class spins up a new private net container for each test. This consumes a lot of time
// but allows the tests to make changes without interfering with each other.
public class Neow3jWriteIntegrationTest {

    // Invoke function variables
    protected static final String INVOKE_TRANSFER = "transfer";
    protected static final String INVOKE_SCRIPT =
            "150c140898ea2197378f623a7670974454448576d0aeaf0c140898ea2197378f623a7670974454448576d0aeaf13c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238";

    // Before the tests 5 NEO is sent to the RECIPIENT_1 address.
    private static final String RECIPIENT_1 = "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2";
    protected static final String RECIPIENT_2 = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

    // The witness for the invokeFunction transfer
    protected static final Signer SIGNER = Signer.calledByEntry(
            new ScriptHash("afaed076854454449770763a628f379721ea9808"));

    protected static final int INVALID_PARAMS_CODE = -32602;
    protected static final String INVALID_PARAMS_MESSAGE = "Invalid params";

    private Neow3jTestWrapper neow3jWrapper;

    @Rule
    public GenericContainer privateNetContainer = setupPrivateNetContainer();

    @Before
    public void setUp() throws IOException {
        neow3jWrapper = new Neow3jTestWrapper(new HttpService(
                IntegrationTestHelper.getNodeUrl(privateNetContainer)));
        // open the wallet for JSON-RPC calls
        neow3jWrapper.openWallet(NODE_WALLET_PATH, NODE_WALLET_PASSWORD).send();
        // ensure that the wallet with NEO/GAS is initialized for the tests
        neow3jWrapper.waitUntilWalletHasBalanceGreaterThanOrEqualToOne();
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction sendRawTransaction = getNeow3j().sendRawTransaction(
                "005368de7758738900000000000a0113000000000064000000010898ea2197378f623a7670974454448576d0aeaf000054110c14c6a1c24a5b87fb8ccd7ac5f7948ffe526d4e01f70c14226730eaeec8e3315468f57153e3b08789cc45cc13c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5201420c40b31404344e976287d79641e9b06adc516bbd895386bf3decff4546545f2b8895a1cef9a25b761f8354bd529778a7e4cc445804aa1b36b620c8bfba8b3429e6e92b110c21026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d110b41138defaf")
                .send();

        String hash = sendRawTransaction.getSendRawTransaction().getHash();
        assertNotNull(hash);
    }

    private Neow3j getNeow3j() {
        return neow3jWrapper;
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

    @Test
    public void testInvokeFunction() throws IOException {
        List<ContractParameter> params = Arrays.asList(
                ContractParameter.hash160(ScriptHash.fromAddress(ACCOUNT_2_ADDRESS)),
                ContractParameter.hash160(ScriptHash.fromAddress(RECIPIENT_1)),
                ContractParameter.integer(1));
        Signer signer = new Signer.Builder()
                .account(ScriptHash.fromAddress(ACCOUNT_2_ADDRESS))
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(new ScriptHash(NEO_HASH))
                .build();
        Request<?, NeoInvokeFunction> invokeFunction = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_TRANSFER, params, signer);
        NeoInvokeFunction send = invokeFunction.send();
        InvocationResult invoc = send.getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
    }

    @Test
    public void testInvokeScript() throws IOException {
        NeoInvokeScript invokeScript = getNeow3j().invokeScript(INVOKE_SCRIPT, SIGNER).send();
        InvocationResult invoc = invokeScript.getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNotNull(invoc.getStack());
        assertNotNull(invoc.getTx());
    }

    // Wallet Methods

    @Test
    public void testSendFrom() throws IOException {
        NeoSendFrom sendFrom = getNeow3j().sendFrom(ACCOUNT_1_ADDRESS, NEO_HASH, RECIPIENT_1, "10")
                .send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws IOException {
        TransactionSendAsset txSendAsset = new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_1);
        NeoSendFrom sendFrom = getNeow3j().sendFrom(ACCOUNT_1_ADDRESS, txSendAsset).send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));
    }

    @Test
    public void testSendMany() throws IOException {
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList(
                        new TransactionSendAsset(NEO_HASH, "100", RECIPIENT_1),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_2)
                )
        ).send();

        assertNotNull(sendMany.getSendMany());
        Transaction tx = sendMany.getSendMany();

        assertNotNull(tx.getHash());
        assertNotNull(tx.getNonce());
        assertNotNull(tx.getSender());
    }

    @Test
    public void testSendMany_Empty_Transaction() throws IOException {
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList()).send();

        assertNull(sendMany.getSendMany());
        assertNotNull(sendMany.getError());
        assertThat(sendMany.getError().getCode(), is(INVALID_PARAMS_CODE));
        assertThat(sendMany.getError().getMessage(), is(INVALID_PARAMS_MESSAGE));
    }

    @Test
    public void testSendToAddress() throws IOException {
        NeoSendToAddress sendToAddress = getNeow3j().sendToAddress(NEO_HASH, RECIPIENT_1, "10")
                .send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws IOException {
        TransactionSendAsset transactionSendAsset = new TransactionSendAsset(NEO_HASH, "10",
                RECIPIENT_1);
        NeoSendToAddress sendToAddress = getNeow3j().sendToAddress(transactionSendAsset).send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }
}
