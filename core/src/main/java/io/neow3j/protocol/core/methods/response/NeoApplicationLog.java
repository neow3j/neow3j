package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.model.types.ContractParameter;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.HexUtils;

import java.math.BigInteger;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("executions")
    private List<Execution> executions;

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

        public String getTrigger() {
            return trigger;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getGasConsumed() {
            return gasConsumed;
        }

        public void setGasConsumed(String gasConsumed) {
            this.gasConsumed = gasConsumed;
        }

        public List<ContractParameter> getStack() {
            return stack;
        }

        public void setStack(List<ContractParameter> stack) {
            this.stack = stack;
        }

        public List<Notification> getNotifications() {
            return notifications;
        }

        public void setNotifications(List<Notification> notifications) {
            this.notifications = notifications;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notification {

        @JsonProperty("contract")
        private String contract;

        @JsonProperty("state")
        private State state;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class State {

            @JsonProperty("type")
            private ContractParameterType type;

            @JsonProperty("value")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<EventParameter> value;

            public ContractParameterType getType() {
                return type;
            }

            public void setType(ContractParameterType type) {
                this.type = type;
            }

            public List<EventParameter> getValue() {
                return value;
            }

            public void setValue(List<EventParameter> value) {
                this.value = value;
            }
        }

        public String getContract() {
            return contract;
        }

        public void setContract(String contract) {
            this.contract = contract;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }

    public static class EventParameter extends ContractParameter {

        public String getAsAddress() {
            if (this.value instanceof String) {
                return HexUtils.scriptHashToAddress((String) this.value);
            }
            return null;
        }

        public String getAsString() {
            if (this.value instanceof String) {
                return HexUtils.hexToString((String) this.value);
            }
            return null;
        }

        public BigInteger getAsNumber() {
            if (this.value instanceof String) {
                return HexUtils.hexToInteger((String) this.value);
            }
            return null;
        }

    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }
}
