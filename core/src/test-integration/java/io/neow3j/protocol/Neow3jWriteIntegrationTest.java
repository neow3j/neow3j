package io.neow3j.protocol;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

// This test class spins up a new private net container for each test. This consumes a lot of time
// but allows the tests to make changes without interfering with each other.
public class Neow3jWriteIntegrationTest extends Neow3jIntegrationTest {

    @Rule
    public GenericContainer privateNetContainer = new GenericContainer(NEO3_PRIVATENET_CONTAINER_IMG)
            .withClasspathResourceMapping("/node-config/config.json",
                    "/neo-cli/config.json", BindMode.READ_ONLY)
            .withClasspathResourceMapping("/node-config/protocol.json",
                    "/neo-cli/protocol.json", BindMode.READ_ONLY)
            .withCopyFileToContainer(MountableFile.forClasspathResource("/node-config/wallet.json", 777),
                    "/neo-cli/wallet.json")
            .withClasspathResourceMapping("/node-config/rpcserver.config.json",
                    "/neo-cli/Plugins/RpcServer/config.json", BindMode.READ_ONLY)
            .withExposedPorts(EXPOSED_JSONRPC_PORT)
            .waitingFor(Wait.forListeningPort());

    @Override
    protected GenericContainer getPrivateNetContainer() {
        return privateNetContainer;
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction sendRawTransaction = getNeow3j().sendRawTransaction(
                "00835e32b9941343239213fa0e765f1027ce742f48db779a96" +
                        "a472890000000000064b130000000000881300000101941343239213fa0e765f1027ce742f48db779a96015500" +
                        "640c14d785dc45b8103f46ffb930ee7ffe4eff5d86bbf70c14941343239213fa0e765f1027ce742f48db779a96" +
                        "13c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5201420c40d222" +
                        "850ae7416a3b381a11b9a22f2130b61944b4c0f3e3e92c8bdd13f1dc5d86dd1989f986b96e010a41fcc8ccdd64" +
                        "dd0a7c94088877451f1f4ebbab2abf09c3290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0" +
                        "ebf561cb8f9562380b418a6b1e75")
                .send();

        String hash = sendRawTransaction.getSendRawTransaction().getHash();
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
                ADDRESS_2_HASH160,
                RECIPIENT_ADDRESS_HASH160,
                ContractParameter.integer(1));
        Signer signer = new Signer.Builder()
                .account(ScriptHash.fromAddress(ADDRESS_2))
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
        NeoInvokeScript invokeScript = getNeow3j().invokeScript(INVOKE_SCRIPT, WITNESS).send();
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
        NeoSendFrom sendFrom = getNeow3j().sendFrom(ADDRESS_1, NEO_HASH, RECIPIENT_ADDRESS_1, "10").send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ADDRESS_1));
    }

    @Test
    public void testSendFrom_TransactionSendAsset() throws IOException {
        TransactionSendAsset txSendAsset = new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_ADDRESS_1);
        NeoSendFrom sendFrom = getNeow3j().sendFrom(ADDRESS_1, txSendAsset).send();
        Transaction tx = sendFrom.getSendFrom();

        assertNotNull(tx);
        assertNotNull(tx.getHash());
        assertThat(tx.getSender(), is(ADDRESS_1));
    }

    @Test
    public void testSendMany() throws IOException {
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList(
                        new TransactionSendAsset(NEO_HASH, "100", RECIPIENT_ADDRESS_1),
                        new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_ADDRESS_2)
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
        NeoSendToAddress sendToAddress = getNeow3j().sendToAddress(NEO_HASH, RECIPIENT_ADDRESS_1, "10").send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }

    @Test
    public void testSendToAddress_TransactionSendAsset() throws IOException {
        TransactionSendAsset transactionSendAsset = new TransactionSendAsset(NEO_HASH, "10", RECIPIENT_ADDRESS_1);
        NeoSendToAddress sendToAddress = getNeow3j().sendToAddress(transactionSendAsset).send();
        Transaction tx = sendToAddress.getSendToAddress();

        assertNotNull(tx);
    }
}
