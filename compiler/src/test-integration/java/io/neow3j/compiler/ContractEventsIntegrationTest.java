package io.neow3j.compiler;

import io.neow3j.contract.Hash256;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.contracts.PolicyContract;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event3Args;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.StackItem;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static io.neow3j.contract.ContractParameter.byteArray;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContractEventsIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    private static final int FEE_PER_BYTE = 1000;
    private static final int EXEC_FEE_FACTOR = 30;

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(ContractEvents.class.getName());

    @Test
    public void fireTwoEvents() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Execution> executions = log.getExecutions();
        assertThat(executions, hasSize(1));
        List<NeoApplicationLog.Execution.Notification> notifications =
                executions.get(0).getNotifications();
        assertThat(notifications, hasSize(2));

        assertThat(notifications.get(0).getEventName(), is("event1"));
        List<StackItem> state = notifications.get(0).getState().getList();
        assertThat(state.get(0).getString(), is("event text"));
        assertThat(state.get(1).getInteger().intValue(), is(10));

        assertThat(notifications.get(1).getEventName(), is("displayName"));
        state = notifications.get(1).getState().getList();
        assertThat(state.get(0).getString(), is("event text"));
        assertThat(state.get(1).getInteger().intValue(), is(10));
        assertThat(state.get(2).getInteger().intValue(), is(1));
        assertThat(state.get(3).getString(), is("more text"));
        assertThat(state.get(4).getString(), is("an object"));
    }

    @Test
    public void fireEventWithMethodReturnValueAsArgument() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName);
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Execution> executions = log.getExecutions();
        assertThat(executions, hasSize(1));
        List<NeoApplicationLog.Execution.Notification> notifications =
                executions.get(0).getNotifications();
        assertThat(notifications, hasSize(1));

        assertThat(notifications.get(0).getEventName(), is("event4"));
        List<StackItem> state = notifications.get(0).getState().getList();
        assertThat(state.get(0).getInteger().intValue(), is(FEE_PER_BYTE));
        assertThat(state.get(1).getInteger().intValue(), is(EXEC_FEE_FACTOR));
    }

    @Test
    public void fireEvent() throws Throwable {
        Hash256 txHash = ct.invokeFunctionAndAwaitExecution(testName,
                byteArray("0f46dc4287b70117ce8354924b5cb3a47215ad93"),
                byteArray("d6c712eb53b1a130f59fd4e5864bdac27458a509"),
                integer(10));
        NeoApplicationLog log = ct.getNeow3j().getApplicationLog(txHash).send().getApplicationLog();
        List<NeoApplicationLog.Execution> executions = log.getExecutions();
        assertThat(executions, hasSize(1));
        List<NeoApplicationLog.Execution.Notification> notifications =
                executions.get(0).getNotifications();
        NeoApplicationLog.Execution.Notification event = notifications.get(0);
        List<StackItem> state = event.getState().getList();
        assertThat(state.get(0).getHexString(), is("0f46dc4287b70117ce8354924b5cb3a47215ad93"));
        assertThat(state.get(1).getHexString(), is("d6c712eb53b1a130f59fd4e5864bdac27458a509"));
        assertThat(state.get(2).getList().get(0).getInteger().intValue(), is(10));
    }

    static class ContractEvents {

        private static Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        private static Event5Args<String, Integer, Boolean, String, Object> event2;

        private static Event3Args<byte[], byte[], int[]> event3;

        private static Event2Args<Integer, Integer> event4;

        public static boolean fireTwoEvents() {
            event1.notify("event text", 10);
            event2.notify("event text", 10, true, "more text", "an object");
            return true;
        }

        public static void fireEventWithMethodReturnValueAsArgument() {
            event4.notify(PolicyContract.getFeePerByte(), PolicyContract.getExecFeeFactor());
        }

        public static void fireEvent(byte[] from, byte[] to, int i) {
            event3.notify(from, to, new int[]{i});
        }
    }

}
