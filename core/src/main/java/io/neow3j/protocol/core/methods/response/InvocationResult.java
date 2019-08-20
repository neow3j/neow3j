package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvocationResult {

    @JsonProperty("script")
    private String script;

    @JsonProperty("state")
    private String state;

    @JsonProperty("gas_consumed")
    private String gasConsumed;

    @JsonProperty("stack")
    private List<StackItem> stack;

    @JsonProperty("tx")
    private String tx;

    public InvocationResult() {
    }

    public InvocationResult(String script, String state, String gasConsumed, List<StackItem> stack, String tx) {
        this.script = script;
        this.state = state;
        this.gasConsumed = gasConsumed;
        this.stack = stack;
        this.tx = tx;
    }

    public String getScript() {
        return script;
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

    public String getTx() {
        return tx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvocationResult)) return false;
        InvocationResult that = (InvocationResult) o;
        return Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getGasConsumed(), that.getGasConsumed()) &&
                Objects.equals(getStack(), that.getStack()) &&
                Objects.equals(getTx(), that.getTx());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript(), getState(), getGasConsumed(), getStack(), getTx());
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "script='" + script + '\'' +
                ", state='" + state + '\'' +
                ", gasConsumed='" + gasConsumed + '\'' +
                ", stack=" + stack +
                ", tx='" + tx + '\'' +
                '}';
    }
}
