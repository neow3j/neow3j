package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotCondition extends WitnessCondition {

    @JsonProperty("expression")
    private WitnessCondition expression;

    public NotCondition() {
        super(WitnessConditionType.NOT);
    }

    public NotCondition(WitnessCondition expression) {
        this();
        this.expression = expression;
    }

    @Override
    public WitnessCondition getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotCondition)) {
            return false;
        }
        NotCondition other = (NotCondition) o;
        return getType() == other.getType() &&
                Objects.equals(getExpression(), other.getExpression());
    }

    @Override
    public String toString() {
        return "NotCondition{" +
                "expression=" + getExpression() +
                "}";
    }

}
