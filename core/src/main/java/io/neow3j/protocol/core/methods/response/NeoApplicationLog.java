package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoApplicationLog {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("trigger")
    private String trigger;

    @JsonProperty("vmstate")
    private String state;

    @JsonProperty("gasConsumed")
    private String gasConsumed;

    @JsonProperty("stack")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<StackItem> stack;

    @JsonProperty("notifications")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Notification> notifications;

    public NeoApplicationLog() {
    }

    public NeoApplicationLog(String transactionId, String trigger, String state,
                             String gasConsumed, List<StackItem> stack,
                             List<Notification> notifications) {
        this.transactionId = transactionId;
        this.trigger = trigger;
        this.state = state;
        this.gasConsumed = gasConsumed;
        this.stack = stack;
        this.notifications = notifications;
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
                    Objects.equals(getState(), that.getState());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getContract(), getState());
        }

        @Override
        public String toString() {
            return "Notification{" +
                    "contract='" + contract + '\'' +
                    ", state=" + state +
                    '}';
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTrigger() {
        return trigger;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoApplicationLog)) {
            return false;
        }
        NeoApplicationLog that = (NeoApplicationLog) o;
        return Objects.equals(getTransactionId(), that.getTransactionId()) &&
                Objects.equals(getTrigger(), that.getTrigger()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getGasConsumed(), that.getGasConsumed()) &&
                Objects.equals(getStack(), that.getStack()) &&
                Objects.equals(getNotifications(), that.getNotifications());
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(getTransactionId(), getTrigger(), getState(), getGasConsumed(),
                        getStack(),getNotifications());
    }

    @Override
    public String toString() {
        return "NeoApplicationLog{" +
                "transactionId='" + transactionId + '\'' +
                ", trigger='" + trigger + '\'' +
                ", state='" + state + '\'' +
                ", gasConsumed='" + gasConsumed + '\'' +
                ", stack=" + stack +
                ", notifications=" + notifications +
                '}';
    }
}
