package io.neow3j.compiler;

import io.neow3j.contract.NeoToken;
import io.neow3j.contract.RoleManagement;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.events.Event4Args;
import io.neow3j.devpack.neo.OracleContract;
import io.neow3j.protocol.core.BlockParameter;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.OracleResponseCode;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.utils.Await;
import io.reactivex.disposables.Disposable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OracleContractTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(OracleContractTestContract.class.getName());
    }

    @Test
    public void getScriptHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsHexString(),
                is("8dc0e742cbdfdeda51ff8a8b78d46829144c80ee"));
    }

    @Test
    public void performRequest() throws Throwable {
        // GAS and NEO needed to register a candidate.
        String gasTxHash = transferGas(defaultAccount.getScriptHash(), "10000");
        String neoTxHash = transferNeo(defaultAccount.getScriptHash(), "10000");
        Await.waitUntilTransactionIsExecuted(gasTxHash, neow3j);
        Await.waitUntilTransactionIsExecuted(neoTxHash, neow3j);

        // Register candidate
        ECKeyPair.ECPublicKey publicKey = defaultAccount.getECKeyPair().getPublicKey();
        NeoSendRawTransaction response = new NeoToken(neow3j).registerCandidate(publicKey)
                .wallet(wallet)
                .signers(Signer.calledByEntry(defaultAccount.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);
        // Designate as oracle.
        response = new RoleManagement(neow3j).designateAsRole(Role.ORACLE, Arrays.asList(publicKey))
                .wallet(wallet)
                .signers(Signer.calledByEntry(committee.getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(), neow3j);

        // Start the oracle service on the neo-node
        privateNetContainer.execInContainer("screen", "-X", "stuff", "start oracle \\015");

        // Invoke contract that requests data from oracle.
        String url = "http://127.0.0.1:64928/test"; // Request URL that is not reachable
        String filter = "$.value";  // JSONPath
        String userdata = "userdata";
        int gasForResponse = 100000000;
        String txHash = invokeFunctionAndAwaitExecution(string(url), string(filter),
                string(userdata), integer(gasForResponse));

        // The oracle response should be available in the next block.
        BigInteger height = neow3j.getTransactionHeight(txHash).send().getHeight();
        AtomicReference<Transaction> tx = new AtomicReference<>();
        Disposable subscribe = neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                BlockParameter.valueOf(height), true).subscribe(b -> {
            List<Transaction> transactions = b.getBlock().getTransactions();
            if (!transactions.isEmpty() && transactions.get(0).getAttributes().stream()
                    .anyMatch(a -> a.getType().equals(TransactionAttributeType.ORACLE_RESPONSE))) {
                tx.set(transactions.get(0));
            }
        });

        while (tx.get() == null) {
            Thread.sleep(1000);
        }
        assertThat(tx.get().getAttributes().get(0).getType(),
                is(TransactionAttributeType.ORACLE_RESPONSE));
        List<Notification> notifications = neow3j.getApplicationLog(tx.get().getHash())
                .send().getApplicationLog().getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getEventName(), is("OracleResponse"));
        assertThat(notifications.get(1).getEventName(), is("callbackEvent"));
        List<StackItem> eventState = notifications.get(1).getState().asArray().getValue();
        assertThat(eventState.get(0).asByteString().getAsString(), is(url));
        assertThat(eventState.get(1).asByteString().getAsString(), is(userdata));
        assertThat(eventState.get(2).asInteger().getValue().byteValue(),
                is(OracleResponseCode.PROTOCOL_NOT_SUPPORTED.byteValue()));
        assertThat(eventState.get(3).asByteString().getValue(), is(new byte[]{}));
    }

    static class OracleContractTestContract {

        private static Event4Args<String, String, Integer, String> callbackEvent;

        public static Hash160 getScriptHash() {
            return OracleContract.getHash();
        }

        public static void performRequest(String url, String filter, String userdata,
                int gasForResponse) {

            OracleContract.request(url, filter, "callback", userdata, gasForResponse);
        }

        public static void callback(String url, String userdata, int code, String result) {
            callbackEvent.notify(url, userdata, code, result);
        }
    }

}
