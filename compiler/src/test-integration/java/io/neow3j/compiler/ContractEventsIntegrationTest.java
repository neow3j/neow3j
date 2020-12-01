package io.neow3j.compiler;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.devpack.neo.Blockchain;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractEventsIntegrationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(ContractEvents.class.getName());
    }

    @Test
    public void fireTwoEvents() throws Throwable {
        String txHash = invokeFunctionAndAwaitExecution();
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Notification> notifications = log.getNotifications();
        assertThat(notifications, hasSize(2));

        assertThat(notifications.get(0).getEventName(), is("event1"));
        ArrayStackItem state = notifications.get(0).getState().asArray();
        assertThat(state.get(0).asByteString().getAsString(), is("event text"));
        assertThat(state.get(1).asInteger().getValue().intValue(), is(10));

        assertThat(notifications.get(1).getEventName(), is("displayName"));
        state = notifications.get(1).getState().asArray();
        assertThat(state.get(0).asByteString().getAsString(), is("event text"));
        assertThat(state.get(1).asInteger().getValue().intValue(), is(10));
        assertThat(state.get(2).asInteger().getValue().intValue(), is(1));
        assertThat(state.get(3).asByteString().getAsString(), is("more text"));
        assertThat(state.get(4).asByteString().getAsString(), is("an object"));
    }

    @Test
    public void fireEventWithMethodReturnValueAsArgument() throws Throwable {
        String txHash = invokeFunctionAndAwaitExecution();
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Notification> notifications = log.getNotifications();
        assertThat(notifications, hasSize(1));

        assertThat(notifications.get(0).getEventName(), is("event1"));
        ArrayStackItem state = notifications.get(0).getState().asArray();
        assertThat(state.get(0).asByteString().getAsString(), is("NEO"));
        assertThat(state.get(1).asInteger().getValue().intValue(), greaterThanOrEqualTo(1));
    }

    static class ContractEvents {

        private static Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        private static Event5Args<String, Integer, Boolean, String, Object> event2;

        public static boolean fireTwoEvents() {
            event1.notify("event text", 10);
            event2.notify("event text", 10, true, "more text", "an object");
            return true;
        }

        public static void fireEventWithMethodReturnValueAsArgument() {
            event1.notify(Runtime.getPlatform(), (int)Blockchain.getHeight());
        }
    }

}
