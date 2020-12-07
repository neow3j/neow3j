package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event3Args;
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

    @Test
    public void fireEvent() throws Throwable {
        String txHash = invokeFunctionAndAwaitExecution(
                byteArray("0f46dc4287b70117ce8354924b5cb3a47215ad93"),
                byteArray("d6c712eb53b1a130f59fd4e5864bdac27458a509"),
                integer(10));
        NeoApplicationLog log = neow3j.getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Notification> notifications = log.getNotifications();
        NeoApplicationLog.Notification event = notifications.get(0);
        ArrayStackItem state = event.getState().asArray();
        assertThat(state.get(0).asByteString().getAsHexString(),
                is("0f46dc4287b70117ce8354924b5cb3a47215ad93"));
        assertThat(state.get(1).asByteString().getAsHexString(),
                is("d6c712eb53b1a130f59fd4e5864bdac27458a509"));
        assertThat(state.get(2).asArray().get(0).asInteger().getValue().intValue(), is(10));
    }

    static class ContractEvents {

        private static Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        private static Event5Args<String, Integer, Boolean, String, Object> event2;

        private static Event3Args<byte[], byte[], int[]> event3;

        public static boolean fireTwoEvents() {
            event1.notify("event text", 10);
            event2.notify("event text", 10, true, "more text", "an object");
            return true;
        }

        public static void fireEventWithMethodReturnValueAsArgument() {
            event1.notify(Runtime.getPlatform(), (int)Blockchain.getHeight());
        }

        public static void fireEvent(byte[] from, byte[] to, int i) {
            event3.notify(from, to, new int[]{i});
        }
    }

}
