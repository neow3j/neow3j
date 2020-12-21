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
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction.RawTransaction;
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
            "DBQJpVh0wtpLhuXUn/UwobFT6xLH1hEMFAmlWHTC2kuG5dSf9TChsVPrEsfWDBR6/SAyVcspcr0KaoJ+dOOH7TIr7BTADAh0cmFuc2ZlcgwUtnIP735+tz8lr7Rw9YeZfOPiRgpBYn1bUg==";

    // Before the tests 5 NEO is sent to the RECIPIENT_1 address.
    // wif KxwrYazXiCdK33JEddpwHbXTpAYyhXC1YyC4SXTVF6GLRPBuVBFb
    private static final String RECIPIENT_1 = "NhixBNjEBvgyk18RzuXJt1T3BpqgAwANSA";
    // wif KzreXMT3AtLEYpqpZB9VPiw7RB75T3jGZ6cY6pv9QY2Sjqihv7M8
    protected static final String RECIPIENT_2 = "NdUQqdbzHTxyGMUDRhKBbPUbSu2HMLKBfN";

    // The witness for the invokeFunction transfer
    protected static final Signer SIGNER = Signer.calledByEntry(
            new ScriptHash("ec2b32ed87e3747e826a0abd7229cb553220fd7a"));

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

    private Neow3j getNeow3j() {
        return neow3jWrapper;
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction sendRawTransaction = getNeow3j().sendRawTransaction(
                "003b1b6c437673890000000000322413000000000081142000017afd203255cb2972bd0a6a827e74e387ed322bec01005d0300c50ef9e64501000c14c09cdcf9140b28b3eebd8f1332e51786431860220c147afd203255cb2972bd0a6a827e74e387ed322bec13c00c087472616e736665720c14bcaf41d684c7d4ad6ee0d99da9707b9d1f0c8e6641627d5b523801420c40dd7e57ba41e734d123f9b4aa20e6254132ceb0a19035d7cee703d5c73b66203319f48e5f5d59d9ed46e4250df7a93623e76358cfa0d071630d35172c8d9945af2b110c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60110b41138defaf")
                .send();

        RawTransaction rawTx = sendRawTransaction.getSendRawTransaction();
        String hash = rawTx.getHash();
        assertNotNull(hash);
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
                ContractParameter.integer(1),
                ContractParameter.string(""));
        Signer signer = new Signer.Builder()
                .account(ScriptHash.fromAddress(ACCOUNT_2_ADDRESS))
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(new ScriptHash(NEO_HASH))
                .build();
        InvocationResult invoc = getNeow3j()
                .invokeFunction(NEO_HASH, INVOKE_TRANSFER, params, signer)
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
    public void testInvokeScript() throws IOException {
        NeoInvokeScript invokeScript = getNeow3j().invokeScript(INVOKE_SCRIPT, SIGNER).send();
        InvocationResult invoc = invokeScript.getInvocationResult();

        assertNotNull(invoc);
        assertNotNull(invoc.getScript());
        assertThat(invoc.getState(), is(VM_STATE_HALT));
        assertNotNull(invoc.getGasConsumed());
        assertNull(invoc.getException());
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
        assertThat(tx.getSender(), is(ACCOUNT_2_ADDRESS));
    }

    @Test
    public void testSendManyWithFrom() throws IOException {
        NeoSendMany response = getNeow3j().sendMany(
                ACCOUNT_1_ADDRESS,
                Arrays.asList(
                        new TransactionSendAsset(NEO_HASH, "100", RECIPIENT_1),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_2)
                )
        ).send();

        assertNotNull(response.getSendMany());
        Transaction tx = response.getSendMany();

        assertThat(tx.getScript(), is("CwBkDBTvPVn3QBwB2Gs6jQf2+RQLJaFVtQwUev0gMlXLKXK9CmqCfnTjh+0yK+wUwAwIdHJhbnNmZXIMFLZyD+9+frc/Ja+0cPWHmXzj4kYKQWJ9W1I4CxoMFMCc3PkUCyiz7r2PEzLlF4ZDGGAiDBR6/SAyVcspcr0KaoJ+dOOH7TIr7BTADAh0cmFuc2ZlcgwUtnIP735+tz8lr7Rw9YeZfOPiRgpBYn1bUjg="));
        assertThat(tx.getSender(), is(ACCOUNT_1_ADDRESS));
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
