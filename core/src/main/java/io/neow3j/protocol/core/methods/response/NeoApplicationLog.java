package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;

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
        private String state;

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

        public Execution(String trigger, String state, String exception, String gasConsumed,
                List<StackItem> stack, List<Notification> notifications) {
            this.trigger = trigger;
            this.state = state;
            this.exception = exception;
            this.gasConsumed = gasConsumed;
            this.stack = stack;
            this.notifications = notifications;
        }

        public String getTrigger() {
            return trigger;
        }

        public String getState() {
            return state;
        }

        public String getException() {
            return exception;
        }

        public String getGasConsumed() {
            return gasConsumed;
        }

        public List<StackItem> getStack() {
            return stack;
        }

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

            public Hash160 getContract() {
                return contract;
            }

            public String getEventName() {
                return eventName;
            }

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
