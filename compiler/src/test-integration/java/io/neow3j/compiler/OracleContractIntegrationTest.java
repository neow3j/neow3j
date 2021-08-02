package io.neow3j.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.GasToken;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.Hash256;
import io.neow3j.contract.NeoToken;
import io.neow3j.contract.RoleManagement;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.contracts.OracleContract;
import io.neow3j.devpack.events.Event4Args;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.Role;
import io.neow3j.protocol.core.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.core.response.Transaction;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.utils.Await;
import io.neow3j.utils.Numeric;
import io.reactivex.disposables.Disposable;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.TestProperties.oracleContractHash;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.contract.Token.toFractions;
import static io.neow3j.protocol.core.response.OracleResponseCode.TIMEOUT;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OracleContractIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(
            OracleContractIntegrationTestContract.class.getName());

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicHttpsPort());

    @Test
    public void getScriptHash() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName);
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is(Numeric.reverseHexString(oracleContractHash())));
    }

    @Test
    public void performRequest() throws Throwable {
        // GAS and NEO needed to register a candidate.
        Hash256 gasTxHash = ct.transferGas(ct.getDefaultAccount().getScriptHash(),
                toFractions(new BigDecimal("10000"), GasToken.DECIMALS));
        Hash256 neoTxHash = ct.transferNeo(ct.getDefaultAccount().getScriptHash(),
                new BigInteger("10000"));
        Await.waitUntilTransactionIsExecuted(gasTxHash, ct.getNeow3j());
        Await.waitUntilTransactionIsExecuted(neoTxHash, ct.getNeow3j());

        // Register candidate
        ECKeyPair.ECPublicKey publicKey = ct.getDefaultAccount().getECKeyPair().getPublicKey();
        NeoSendRawTransaction response = new NeoToken(ct.getNeow3j()).registerCandidate(publicKey)
                .wallet(ct.getWallet())
                .signers(AccountSigner.calledByEntry(ct.getDefaultAccount().getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());
        // Designate as oracle.
        response = new RoleManagement(ct.getNeow3j()).designateAsRole(Role.ORACLE,
                Arrays.asList(publicKey))
                .wallet(ct.getWallet())
                .signers(AccountSigner.calledByEntry(ct.getCommittee().getScriptHash()))
                .sign().send();
        Await.waitUntilTransactionIsExecuted(response.getSendRawTransaction().getHash(),
                ct.getNeow3j());

        // Start the oracle service on the neo-node
        ct.getNeoTestContainer().execInContainer("screen", "-X", "stuff", "start oracle \\015");

        // Setup WireMock
        String json = "{ \"jsonrpc\": \"2.0\", \"id\": 1, \"result\": 1000 }";
        JsonNode jsonNode = ObjectMapperFactory.getObjectMapper().readTree(json);
        int port = wireMockRule.httpsPort();
        wireMockRule.stubFor(get(urlEqualTo("/test")).willReturn(aResponse()
                .withStatus(200)
                .withJsonBody(jsonNode)));

        // Invoke contract that requests data from oracle.
        String url = "https://127.0.0.1:" + port + "/test";
        String filter = "";  // JSONPath
        String userdata = "userdata";
        int gasForResponse = 100000000;
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName, string(url), string(filter),
                string(userdata), integer(gasForResponse));

        // The oracle response should be available in the next block.
        BigInteger height = ct.getNeow3j().getTransactionHeight(txHash).send().getHeight();
        AtomicReference<Transaction> tx = new AtomicReference<>();
        Disposable subscribe = ct.getNeow3j().catchUpToLatestAndSubscribeToNewBlocksObservable(
                height, true).subscribe(b -> {
            List<Transaction> transactions = b.getBlock().getTransactions();
            if (!transactions.isEmpty() && transactions.get(0).getAttributes().stream()
                    .anyMatch(a -> a.getType().equals(TransactionAttributeType.ORACLE_RESPONSE))) {
                tx.set(transactions.get(0));
            }
        });

        Await.waitUntil(
                () -> tx.get() != null ? tx : null,
                notNullValue(),
                60,
                TimeUnit.SECONDS
        );
        subscribe.dispose();

        assertThat(tx.get().getAttributes().get(0).getType(),
                is(TransactionAttributeType.ORACLE_RESPONSE));
        List<Notification> notifications = ct.getNeow3j().getApplicationLog(tx.get().getHash())
                .send().getApplicationLog().getExecutions().get(0).getNotifications();
        assertThat(notifications.get(0).getEventName(), is("OracleResponse"));
        assertThat(notifications.get(1).getEventName(), is("callbackEvent"));
        List<StackItem> eventState = notifications.get(1).getState().getList();
        assertThat(eventState.get(0).getString(), is(url));
        assertThat(eventState.get(1).getString(), is(userdata));
        assertThat(eventState.get(2).getInteger().byteValue(), is(TIMEOUT.byteValue()));
        assertThat(eventState.get(3).getByteArray(), is(new byte[]{}));
    }

    @Permission(contract = "fe924b7cfe89ddd271abaf7210a80a7e11178758")
    static class OracleContractIntegrationTestContract {

        private static Event4Args<String, String, Integer, String> callbackEvent;

        public static Hash160 getScriptHash() {
            return OracleContract.getHash();
        }

        public static void performRequest(String url, String filter, String userdata,
                int gasForResponse) {

            OracleContract.request(url, filter, "callback", userdata, gasForResponse);
        }

        public static void callback(String url, String userdata, int code, String result) {
            callbackEvent.fire(url, userdata, code, result);
        }
    }

}
