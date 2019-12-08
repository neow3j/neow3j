package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("executions")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Execution> executions;

    public NeoApplicationLog() {
    }

    public NeoApplicationLog(String transactionId, List<Execution> executions) {
        this.transactionId = transactionId;
        this.executions = executions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Execution {

        @JsonProperty("trigger")
        private String trigger;

        @JsonProperty("contract")
        private String contract;

        @JsonProperty("vmstate")
        private String state;

        @JsonProperty("gas_consumed")
        private String gasConsumed;

        @JsonProperty("stack")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<StackItem> stack;

        @JsonProperty("notifications")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Notification> notifications;

        public Execution() {
        }

        public Execution(String trigger, String contract, String state, String gasConsumed, List<StackItem> stack, List<Notification> notifications) {
            this.trigger = trigger;
            this.contract = contract;
            this.state = state;
            this.gasConsumed = gasConsumed;
            this.stack = stack;
            this.notifications = notifications;
        }

        public String getTrigger() {
            return trigger;
        }

        public String getContract() {
            return contract;
        }

        public String getState() {
            return state;
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notification {

        @JsonProperty("contract")
        private String contract;

        @JsonProperty("state")
        private StackItem state;

        public Notification() {
        }

        public Notification(String contract, StackItem state) {
            this.contract = contract;
            this.state = state;
        }

        public String getContract() {
            return contract;
        }

        public StackItem getState() {
            return state;
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<Execution> getExecutions() {
        return executions;
    }
}
