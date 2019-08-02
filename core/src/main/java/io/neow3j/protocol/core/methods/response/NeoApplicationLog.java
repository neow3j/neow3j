package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog.EventParameter.EventParameterDeserializer;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
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
        private State state;

        public Notification() {
        }

        public Notification(String contract, State state) {
            this.contract = contract;
            this.state = state;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class State {

            @JsonProperty("type")
            private ContractParameterType type;

            @JsonProperty("value")
            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private List<EventParameter> value;

            public State() {
            }

            public State(ContractParameterType type, List<EventParameter> value) {
                this.type = type;
                this.value = value;
            }

            public ContractParameterType getType() {
                return type;
            }

            public List<EventParameter> getValue() {
                return value;
            }
        }

        public String getContract() {
            return contract;
        }

        public State getState() {
            return state;
        }
    }

    @JsonDeserialize(using = EventParameterDeserializer.class)
    public static class EventParameter extends ContractParameter {

        protected EventParameter(String name, ContractParameterType type, Object value) {
            super(name, type, value);
        }

        public String getAsAddress() {
            if (this.value instanceof String) {
                // The value, a script hash in this case, is expected to be in little-endian order.
                return new ScriptHash(Numeric.hexStringToByteArray((String) this.value)).toAddress();
            }
            if (this.value instanceof byte[]) {
                return new ScriptHash((byte[]) this.value).toAddress();
            }
            return null;
        }

        public String getAsString() {
            if (this.value instanceof String) {
                return Numeric.hexToString((String) this.value);
            }
            if (this.value instanceof byte[]) {
                return Numeric.hexToString(Numeric.toHexStringNoPrefix((byte[]) this.value));
            }
            return null;
        }

        public BigInteger getAsNumber() {
            if (this.value instanceof String) {
                return BigIntegers.fromLittleEndianByteArray(
                        Numeric.hexStringToByteArray((String) this.value));
            }
            if (this.value instanceof byte[]) {
                return BigIntegers.fromLittleEndianByteArray((byte[]) this.value);
            }
            return null;
        }

        protected static class EventParameterDeserializer extends ParameterDeserializer<EventParameter> {

            @Override
            public EventParameter newInstance(String name, ContractParameterType type, Object value) {
                return new EventParameter(name, type, value);
            }
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<Execution> getExecutions() {
        return executions;
    }
}
