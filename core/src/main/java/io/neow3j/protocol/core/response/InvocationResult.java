package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.NeoVMStateType;

import java.util.List;
import java.util.Objects;

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

    @JsonProperty("stack")
    private List<StackItem> stack;

    @JsonProperty("tx")
    private String tx;

    public InvocationResult() {
    }

    public InvocationResult(String script, NeoVMStateType state, String gasConsumed,
            String exception, List<StackItem> stack, String tx) {
        this.script = script;
        this.state = state;
        this.gasConsumed = gasConsumed;
        this.exception = exception;
        this.stack = stack;
        this.tx = tx;
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

    public List<StackItem> getStack() {
        return stack;
    }

    public String getTx() {
        return tx;
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
        return Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getGasConsumed(), that.getGasConsumed()) &&
                Objects.equals(getException(), that.getException()) &&
                Objects.equals(getStack(), that.getStack()) &&
                Objects.equals(getTx(), that.getTx());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript(), getState(), getGasConsumed(),
                getException(), getStack(), getTx());
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "script='" + script + '\'' +
                ", state=" + state +
                ", gasconsumed=" + gasConsumed +
                ", exception='" + exception + '\'' +
                ", stack=" + stack +
                ", tx='" + tx + '\'' +
                '}';
    }

}
