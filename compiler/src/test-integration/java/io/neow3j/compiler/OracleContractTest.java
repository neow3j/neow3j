package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.events.Event4Args;
import io.neow3j.devpack.OracleContract;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.Execution.Notification;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class OracleContractTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(OracleContractTestContract.class.getName());
    }

    @Test
    public void getScriptHash() throws IOException {
        // TODO: Test when issue #292 was implemented.
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asBuffer().getValue(),
                is(Numeric.hexStringToByteArray("3c05b488bf4cf699d0631bf80190896ebbf38c3b")));
    }

    @Test
    public void performRequest() throws Throwable {
        // TODO: Test when preview4 privatenet docker image is ready.
        String url = "http://127.0.0.1:8080/test"; // the return value is  { "value": "hello
        // world" }, and when we use private host for testing, don't forget to set
        // `AllowPrivateHost` true
        String filter = "$.value";  // JSONPath format https://github.com/atifaziz/JSONPath
        String userdata = "userdata"; // arbitrary type
        int gasForResponse = 10000000;

        String txHash = invokeFunctionAndAwaitExecution(string(url), string(filter),
                string(userdata), integer(gasForResponse));

        NeoApplicationLog applicationLog = neow3j.getApplicationLog(txHash).send()
                .getApplicationLog();
        List<Notification> notifications = applicationLog.getExecutions().get(0).getNotifications();
        assertThat(notifications, hasSize(1));
        assertThat(notifications.get(0).getEventName(), is("callbackEvent"));

        ArrayStackItem states = notifications.get(0).getState().asArray();
        assertThat(states.getValue(), hasSize(4));
        assertThat(states.get(0).asByteString().getAsString(), is(url));
        assertThat(states.get(1).asByteString().getAsString(), is(userdata));
        // TODO: What code is returned?
        assertThat(states.get(2).asInteger().getValue().intValue(), is(0));
        // TODO: What result is returned?
        assertThat(states.get(3).asByteString().getAsString(), is("unknown"));
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
