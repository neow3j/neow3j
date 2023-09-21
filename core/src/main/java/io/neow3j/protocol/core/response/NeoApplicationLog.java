package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash256;
import io.neow3j.types.NeoVMStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private Hash256 transactionId;

    @JsonProperty("executions")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Execution> executions = new ArrayList<>();

    public NeoApplicationLog() {
    }

    public NeoApplicationLog(Hash256 transactionId, List<Execution> executions) {
        this.transactionId = transactionId;
        this.executions = executions;
    }

    public Hash256 getTransactionId() {
        return transactionId;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    @JsonIgnore
    public Execution getFirstExecution() {
        if (executions.size() == 0) {
            throw new IndexOutOfBoundsException("This transaction does not have any executions.");
        }
        return executions.get(0);
    }

    @JsonIgnore
    public Execution getExecution(int index) {
        if (index >= executions.size()) {
            throw new IndexOutOfBoundsException(format("This transaction has only %s executions.", executions.size()));
        }
        return executions.get(index);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Execution {

        @JsonProperty("trigger")
        private String trigger;

        @JsonProperty("vmstate")
        private NeoVMStateType state;

        @JsonProperty("exception")
        private String exception;

        @JsonProperty("gasconsumed")
        private String gasConsumed;

        @JsonProperty("stack")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<StackItem> stack = new ArrayList<>();

        @JsonProperty("notifications")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Notification> notifications = new ArrayList<>();

        public Execution() {
        }

        public Execution(String trigger, NeoVMStateType state, String exception, String gasConsumed,
                List<StackItem> stack, List<Notification> notifications) {
            this.trigger = trigger;
            this.state = state;
            this.exception = exception;
            this.gasConsumed = gasConsumed;
            this.stack = stack;
            this.notifications = notifications;
        }

        /**
         * Gets the trigger type with which the invocation was made. For normal contract invocations the trigger type
         * is "Application" but a contract can also be invoked for verification in which case the trigger type is
         * "Verification".
         *
         * @return the trigger.
         */
        public String getTrigger() {
            return trigger;
        }

        /**
         * @return the NeoVM state of this execution.
         */
        public NeoVMStateType getState() {
            return state;
        }

        /**
         * @return the exception or null if no exception was thrown.
         */
        public String getException() {
            return exception;
        }

        /**
         * @return the amount of GAS consumed by the execution.
         */
        public String getGasConsumed() {
            return gasConsumed;
        }

        /**
         * @return the return stack of the invocation, i.e., the values that were returned by the NeoVM at the end of
         * the invocation. Usually, this stack contains one single stack item at index 0.
         */
        public List<StackItem> getStack() {
            return stack;
        }

        @JsonIgnore
        public StackItem getFirstStackItem() {
            if (stack.size() == 0) {
                throw new IndexOutOfBoundsException("The stack is empty. This means that no items were left on the " +
                        "NeoVM stack after this execution.");
            }
            return getStackItem(0);
        }

        public StackItem getStackItem(int index) {
            if (index >= stack.size()) {
                throw new IndexOutOfBoundsException(
                        format("There were only %s items left on the NeoVM stack after this execution.", stack.size()));
            }
            return stack.get(index);
        }

        /**
         * @return the notifications fired by this invocation.
         */
        public List<Notification> getNotifications() {
            return notifications;
        }

        @JsonIgnore
        public Notification getFirstNotification() {
            if (notifications.size() == 0) {
                throw new IndexOutOfBoundsException("This execution did not send any notifications.");
            }
            return notifications.get(0);
        }

        public Notification getNotification(int index) {
            if (index >= notifications.size()) {
                throw new IndexOutOfBoundsException(format("This execution only sent %s notifications.",
                        notifications.size()));
            }
            return notifications.get(index);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Execution)) {
                return false;
            }
            Execution that = (Execution) o;
            return Objects.equals(getTrigger(), that.getTrigger()) &&
                    Objects.equals(getState(), that.getState()) &&
                    Objects.equals(getException(), that.getException()) &&
                    Objects.equals(getGasConsumed(), that.getGasConsumed()) &&
                    Objects.equals(getStack(), that.getStack()) &&
                    Objects.equals(getNotifications(), that.getNotifications());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTrigger(), getState(), getException(), getGasConsumed(), getStack(),
                    getNotifications());
        }

        @Override
        public String toString() {
            return "Execution{" +
                    "trigger='" + trigger + '\'' +
                    ", state='" + state + '\'' +
                    ", exception='" + exception + '\'' +
                    ", gasConsumed='" + gasConsumed + '\'' +
                    ", stack=" + stack +
                    ", notifications=" + notifications +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoApplicationLog)) {
            return false;
        }
        NeoApplicationLog that = (NeoApplicationLog) o;
        return Objects.equals(getTransactionId(), that.getTransactionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionId(), getExecutions());
    }

    @Override
    public String toString() {
        return "NeoApplicationLog{" +
                "transactionId='" + transactionId + '\'' +
                ", executions=" + executions +
                '}';
    }

}
