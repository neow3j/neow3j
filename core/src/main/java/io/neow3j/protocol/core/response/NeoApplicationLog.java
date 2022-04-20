package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import io.neow3j.types.NeoVMStateType;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private Hash256 transactionId;

    @JsonProperty("executions")
    private List<Execution> executions;

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
        private List<StackItem> stack;

        @JsonProperty("notifications")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Notification> notifications;

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
         * Gets the trigger type with which the invocation was made. For normal contract
         * invocations the trigger type is "Application" but a contract can also be invoked for
         * verification in which case the trigger type is "Verification".
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
         * Gets the exception thrown by the invocation if any.
         *
         * @return The exception or null if no exception was thrown.
         */
        public String getException() {
            return exception;
        }

        /**
         * Gets the amount of GAS consumed by the execution.
         *
         * @return the amount of GAS.
         */
        public String getGasConsumed() {
            return gasConsumed;
        }

        /**
         * Gets the return stack of the invocation, i.e., the values that were returned by the
         * NeoVM at the end of the invocation. Usually this stack contains one single stack item
         * at index 0;
         *
         * @return the return stack.
         */
        public List<StackItem> getStack() {
            return stack;
        }

        /**
         * Gets the notifications fired by this invocation.
         *
         * @return the notifications.
         */
        public List<Notification> getNotifications() {
            return notifications;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Notification {

            @JsonProperty("contract")
            private Hash160 contract;

            @JsonProperty("eventname")
            private String eventName;

            @JsonProperty("state")
            private StackItem state;

            public Notification() {
            }

            public Notification(Hash160 contract, String eventName, StackItem state) {
                this.contract = contract;
                this.eventName = eventName;
                this.state = state;
            }

            /**
             * Gets the script hash of the contract that fired this notification.
             *
             * @return the script hash.
             */
            public Hash160 getContract() {
                return contract;
            }

            /**
             * Gets the event name as described in the manifest of the contract that
             * fired the notification.
             * <p>
             * The words event and notification can be used synonymously here.
             *
             * @return The event name.
             */
            public String getEventName() {
                return eventName;
            }

            /**
             * Gets the state attached to this notification. It is up to the developer to know
             * what to in the state and it needs knowledge about the contract that triggered the
             * notification.
             *
             * @return the notification's state as a NeoVM stack item.
             */
            public StackItem getState() {
                return state;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Notification)) {
                    return false;
                }
                Notification that = (Notification) o;
                return Objects.equals(getContract(), that.getContract()) &&
                        Objects.equals(getEventName(), that.getEventName()) &&
                        Objects.equals(getState(), that.getState());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getContract(), getEventName(), getState());
            }

            @Override
            public String toString() {
                return "Notification{" +
                        "contract='" + contract + '\'' +
                        ", eventname=" + eventName +
                        ", state=" + state +
                        '}';
            }
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
            return Objects.hash(getTrigger(), getState(), getException(), getGasConsumed(),
                    getStack(), getNotifications());
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
        return Objects
                .hash(getTransactionId(), getExecutions());
    }

    @Override
    public String toString() {
        return "NeoApplicationLog{" +
                "transactionId='" + transactionId + '\'' +
                ", executions=" + executions +
                '}';
    }

}
