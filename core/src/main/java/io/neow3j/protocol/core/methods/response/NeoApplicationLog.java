package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neow3j.model.types.ContractParameter;
import io.neow3j.protocol.core.methods.response.stack.Item;
import io.neow3j.protocol.deserializer.StackDeserializer;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("executions")
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
        private List<ContractParameter> stack;

        @JsonProperty("notifications")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Notification> notifications;

        public Execution() {
        }

        public Execution(String trigger, String contract, String state, String gasConsumed, List<ContractParameter> stack, List<Notification> notifications) {
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

        public List<ContractParameter> getStack() {
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
        @JsonDeserialize(using = StackDeserializer.class)
        private Item item;

        public Notification() {
        }

        public Notification(String contract, Item item) {
            this.contract = contract;
            this.item = item;
        }

        public String getContract() {
            return contract;
        }

        public Item getItem() {
            return item;
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<Execution> getExecutions() {
        return executions;
    }
}
