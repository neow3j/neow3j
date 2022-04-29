package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BooleanCondition extends WitnessCondition {

    @JsonProperty("expression")
    private boolean expression;

    public BooleanCondition() {
        super(WitnessConditionType.BOOLEAN);
    }

    public BooleanCondition(boolean expression) {
        this();
        this.expression = expression;
    }

    @Override
    public boolean getBooleanExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanCondition)) {
            return false;
        }
        BooleanCondition other = (BooleanCondition) o;
        return getType() == other.getType() && getBooleanExpression() == other.getBooleanExpression();
    }

    @Override
    public String toString() {
        return "BooleanCondition{" +
                "expression=" + getBooleanExpression() +
                "}";
    }

}
