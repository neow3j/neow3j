package io.neow3j.protocol;

import io.neow3j.constants.NeoConstants;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.util.Arrays;

import static io.neow3j.protocol.jsonrpc.JsonRpcErrorConstants.INVALID_PARAMS_CODE;
import static io.neow3j.protocol.jsonrpc.JsonRpcErrorConstants.INVALID_PARAMS_MESSAGE;
import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

// This test class spins up a new private net container for each test. This consumes a lot of time
// but allows the tests to make changes without interfering with each other.
public class Neow3jWriteIntegrationTest extends Neow3jIntegrationTest {

    @Rule
    public GenericContainer privateNetContainer = new GenericContainer(PRIVNET_CONTAINER)
            .withExposedPorts(EXPOSED_INTERNAL_PORT_NEO_DOTNET)
            .waitingFor(Wait.forListeningPort());

    @Override
    protected GenericContainer getPrivateNetContainer() {
        return privateNetContainer;
    }

    @Test
    public void testGetNewAddress() throws IOException {
        NeoGetNewAddress getNewAddress = getNeow3j().getNewAddress().send();
        String address = getNewAddress.getAddress();
        assertNotNull(address);
        assertThat(address.length(), is(NeoConstants.ADDRESS_SIZE));
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction neoSendRawTransaction = getNeow3j()
                .sendRawTransaction("80000001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6" +
                        "f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf" +
                        "6efc336fc500e1f505000000001cc9c05cefffe6cdd7b182816a9152ec218d2ec09b7cff" +
                        "daa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001fcb69f28623" +
                        "0023ba2703c53263e8d6e522dc32203339dcd8eee9014140ccd298c88d8c3609d9369d27" +
                        "0c6ab2278b3a4c1540df73b65fee93cbc088160bf3b9ada13a6c089f5a7b970985d4a31f" +
                        "54bf549cfa2bac8b9121cb0c56a6c7e22321031a6c6fbbdf02ca351745fa86b9ba5a9452" +
                        "d785ac4f7fc2b7548ca2a46c4fcf4aac")
                .send();
        Boolean sendRawTransaction = neoSendRawTransaction.getSendRawTransaction();
        assertThat(sendRawTransaction, is(true));
    }

    @Test
    public void testSendToAddress() throws Exception {
        NeoSendToAddress neoSendToAddress = getNeow3j()
                .sendToAddress(NEOAsset.HASH_ID, ADDRESS_4, "10")
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(
                sendToAddress.getOutputs(),
                hasItem(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", ADDRESS_4)
                )
        );
    }

    @Test
    public void testSendToAddress_Fee() throws IOException {
        NeoSendToAddress neoSendToAddress = getNeow3j()
                .sendToAddress(NEOAsset.HASH_ID, ADDRESS_4, "10", "0.1")
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(
                sendToAddress.getOutputs(),
                hasItem(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", ADDRESS_4)
                )
        );
        assertThat(
                sendToAddress.getNetFee(),
                is("0.1")
        );
    }

    @Test
    public void testSendToAddress_Fee_And_ChangeAddress() throws IOException {
        NeoGetNewAddress neoGetNewAddress = getNeow3j().getNewAddress().send();
        String newChangeAddress = neoGetNewAddress.getAddress();
        NeoSendToAddress neoSendToAddress = getNeow3j()
                .sendToAddress(NEOAsset.HASH_ID, ADDRESS_4, "10", "0.1", newChangeAddress)
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(sendToAddress.getOutputs().size(), greaterThanOrEqualTo(2));
        assertThat(sendToAddress.getOutputs().get(0).getValue(), is("10"));
        assertThat(sendToAddress.getOutputs().get(0).getAddress(), is(ADDRESS_4));
        assertThat(sendToAddress.getOutputs().get(1).getValue(), notNullValue());
        assertThat(sendToAddress.getOutputs().get(1).getAddress(), is(newChangeAddress));
        assertThat(
                sendToAddress.getNetFee(),
                is("0.1")
        );
    }

    @Test
    public void testSendMany() throws IOException, InterruptedException {
        int balance = getCurrentNeoBalance();
        String newNeoBalance = Integer.toString(balance - 110);
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList(
                        new TransactionOutput(NEOAsset.HASH_ID, "100", ADDRESS_2),
                        new TransactionOutput(NEOAsset.HASH_ID, "10", ADDRESS_2)
                )
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", ADDRESS_2),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", ADDRESS_2),
                        // instead of "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", the address
                        // "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs" is also part of the wallet -- default address
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), newNeoBalance, ADDRESS_3)
                )
        );
        assertThat(sendMany.getSendMany().getInputs(), not(empty()));
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
    }

    @Test
    public void testSendMany_Empty_Transaction() throws IOException {
        NeoSendMany sendMany = getNeow3j().sendMany(Arrays.asList()).send();
        assertThat(sendMany.getSendMany(), is(nullValue()));
        assertThat(sendMany.getError(), is(notNullValue()));
        assertThat(sendMany.getError().getCode(), is(INVALID_PARAMS_CODE));
        assertThat(sendMany.getError().getMessage(), is(INVALID_PARAMS_MESSAGE));
    }

    @Test
    public void testSendMany_Fee() throws IOException, InterruptedException {
        int balance = getCurrentNeoBalance();
        String newNeoBalance = Integer.toString(balance - 110);
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList(
                        new TransactionOutput(NEOAsset.HASH_ID, "100", ADDRESS_2),
                        new TransactionOutput(NEOAsset.HASH_ID, "10", ADDRESS_2)
                ),
                "0.1"
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", ADDRESS_2),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", ADDRESS_2),
                        // instead of "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", the address
                        // "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs" is also part of the wallet -- default address
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), newNeoBalance, ADDRESS_3)
                )
        );
        assertThat(sendMany.getSendMany().getInputs(), not(empty()));
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                sendMany.getSendMany().getNetFee(),
                is("0.1")
        );
    }

    @Test
    public void testSendMany_Fee_And_ChangeAddress() throws IOException, InterruptedException {
        int balance = getCurrentNeoBalance();
        String newNeoBalance = Integer.toString(balance - 110);
        NeoSendMany sendMany = getNeow3j().sendMany(
                Arrays.asList(
                        new TransactionOutput(NEOAsset.HASH_ID, "100", ADDRESS_2),
                        new TransactionOutput(NEOAsset.HASH_ID, "10", ADDRESS_2)
                ),
                "0.1",
                ADDRESS_1
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", ADDRESS_2),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", ADDRESS_2),
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), newNeoBalance, ADDRESS_1)
                )
        );
        assertThat(sendMany.getSendMany().getInputs(), not(empty()));
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                sendMany.getSendMany().getNetFee(),
                is("0.1")
        );
    }

    private int getCurrentNeoBalance() throws IOException {
        NeoGetBalance.Balance b = getNeow3j().getBalance(NEOAsset.HASH_ID).send().getBalance();
        return Integer.parseInt(b.getBalance());
    }

    @Test
    public void testInvoke() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    @Test
    public void testInvokeFunction() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    @Test
    public void testInvokeScript() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    @Test
    public void testSubmitBlock() throws IOException {
        // TODO: 2019-03-21 Guil:
        // to be implemented
    }
}
