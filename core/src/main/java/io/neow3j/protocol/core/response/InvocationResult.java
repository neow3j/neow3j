package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.NeoVMStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvocationResult {

    @JsonProperty("script")
    private String script;

    @JsonProperty("state")
    private NeoVMStateType state;

    @JsonProperty("gasconsumed")
    private String gasConsumed;

    @JsonProperty("exception")
    private String exception;

    @JsonProperty("notifications")
    private List<Notification> notifications = new ArrayList<>();

    @JsonProperty("diagnostics")
    private Diagnostics diagnostics;

    @JsonProperty("stack")
    private List<StackItem> stack;

    @JsonProperty("tx")
    private String tx;

    @JsonProperty("pendingsignature")
    private PendingSignature pendingSignature;

    @JsonProperty("session")
    private String sessionId;

    public InvocationResult() {
    }

    public InvocationResult(String script, NeoVMStateType state, String gasConsumed, String exception,
            List<Notification> notifications, Diagnostics diagnostics, List<StackItem> stack, String tx,
            PendingSignature pendingSignature, String sessionId) {
        this.script = script;
        this.state = state;
        this.gasConsumed = gasConsumed;
        this.notifications = notifications;
        this.diagnostics = diagnostics;
        this.exception = exception;
        this.stack = stack;
        this.tx = tx;
        this.pendingSignature = pendingSignature;
        this.sessionId = sessionId;
    }

    public String getScript() {
        return script;
    }

    public NeoVMStateType getState() {
        return state;
    }

    public boolean hasStateFault() {
        return state == NeoVMStateType.FAULT;
    }

    public String getGasConsumed() {
        return gasConsumed;
    }

    public String getException() {
        return exception;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @JsonIgnore
    public Notification getFirstNotification() {
        if (notifications.size() == 0) {
            throw new IndexOutOfBoundsException("No notifications have been sent in this invocation.");
        }
        return notifications.get(0);
    }

    @JsonIgnore
    public Notification getNotification(int index) {
        if (index >= notifications.size()) {
            throw new IndexOutOfBoundsException(
                    format("Only %s notifications have been sent in this invocation. Tried to access index %s in the " +
                            "invocation result.", notifications.size(), index));
        }
        return notifications.get(index);
    }

    public Diagnostics getDiagnostics() {
        return diagnostics;
    }

    public List<StackItem> getStack() {
        return stack;
    }

    @JsonIgnore
    public StackItem getFirstStackItem() {
        if (stack.size() == 0) {
            throw new IndexOutOfBoundsException("The stack is empty. This means that no items were left on the NeoVM " +
                    "stack after this invocation.");
        }
        return getStackItem(0);
    }

    @JsonIgnore
    public StackItem getStackItem(int index) {
        if (index >= stack.size()) {
            throw new IndexOutOfBoundsException(
                    format("There were only %s items left on the NeoVM stack after this invocation", stack.size()));
        }
        return stack.get(index);
    }

    public String getTx() {
        return tx;
    }

    public PendingSignature getPendingSignature() {
        return pendingSignature;
    }

    public String getSessionId() {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalStateException("No session id was found. The connected Neo node might not support " +
                    "sessions.");
        }
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvocationResult)) {
            return false;
        }
        InvocationResult that = (InvocationResult) o;
        return Objects.equals(script, that.script) &&
                Objects.equals(state, that.state) &&
                Objects.equals(gasConsumed, that.getGasConsumed()) &&
                Objects.equals(exception, that.exception) &&
                Objects.equals(notifications, that.notifications) &&
                Objects.equals(diagnostics, that.diagnostics) &&
                Objects.equals(stack, that.stack) &&
                Objects.equals(tx, that.tx) &&
                Objects.equals(pendingSignature, that.pendingSignature) &&
                Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript(), getState(), getGasConsumed(), getException(), getNotifications(), getStack(),
                getTx(), getPendingSignature(), sessionId, getDiagnostics());
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "script='" + script + '\'' +
                ", state=" + state +
                ", gasconsumed=" + gasConsumed +
                ", exception='" + exception + '\'' +
                ", notifications=" + notifications +
                ", diagnostics=" + diagnostics +
                ", stack=" + stack +
                ", tx='" + tx + '\'' +
                ", pendingsignature='" + pendingSignature + '\'' +
                ", session='" + sessionId + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PendingSignature {

        @JsonProperty("type")
        private String type; // the string "Transaction"

        @JsonProperty("data")
        private String data; // base64 string of the serialized, unsigned transaction

        @JsonProperty("items")
        Map<String, Item> items;

        @JsonProperty("network")
        private long network;

        public PendingSignature() {
        }

        public PendingSignature(String type, String data, Map<String, Item> items, long network) {
            this.type = type;
            this.data = data;
            this.items = items;
            this.network = network;
        }

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }

        public Map<String, Item> getItems() {
            return items;
        }

        public long getNetwork() {
            return network;
        }

        public static class Item {

            @JsonProperty("script")
            private String script; // Base64 string

            @JsonProperty("parameters")
            private List<ContractParameter> parameters;

            // encoded, hexadeximal EC-Point mapped to base64-encoded, signature
            @JsonProperty("signatures")
            private Map<String, String> signatures;

            public Item() {
            }

            public Item(String script, List<ContractParameter> parameters, Map<String, String> signatures) {
                this.script = script;
                this.parameters = parameters;
                this.signatures = signatures;
            }

            public String getScript() {
                return script;
            }

            public List<ContractParameter> getParameters() {
                return parameters;
            }

            public Map<String, String> getSignatures() {
                return signatures;
            }
        }

    }

}
