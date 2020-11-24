package io.neow3j.compiler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.StringLiteralHelperIntegrationTest.StringLiterals;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractEventsIntegrationTest extends ContractTest {


    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(StringLiterals.class.getName());
    }

    @Test
    public void addressToScriptHashInMethod() throws Throwable {
        String txHash = invokeFunctionAndAwaitExecution();
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Notification> notifications = log.getNotifications();
        assertThat(notifications.get(0).getEventName(), is("event1"));
        // TODO: Check application log state.
        assertThat(notifications.get(0).getEventName(), is("displayName"));
        // TODO: Check application log state.
    }

    static class ContractEvents {

        static Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        static Event5Args<String, Integer, Boolean, String, Object> event2;

        public static boolean main() {
            event1.send("event text", 10);
            event2.send("event text", 10, true, "more text", "an object");
            return true;
        }
    }

}
