package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessAction;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WitnessRule {

    @JsonProperty("action")
    private WitnessAction action;

    @JsonProperty("condition")
    private WitnessCondition condition;

    public WitnessRule() {
    }

    public WitnessRule(WitnessAction action, WitnessCondition condition) {
        this.action = action;
        this.condition = condition;
    }

    public WitnessAction getAction() {
        return action;
    }

    public WitnessCondition getCondition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WitnessRule)) {
            return false;
        }
        WitnessRule other = (WitnessRule) o;
        return Objects.equals(getAction(), other.getAction()) &&
                Objects.equals(getCondition(), other.getCondition());
    }

    @Override
    public String toString() {
        return "WitnessRule{" +
                "action=" + action +
                ", condition=" + condition +
                "}";
    }

}
