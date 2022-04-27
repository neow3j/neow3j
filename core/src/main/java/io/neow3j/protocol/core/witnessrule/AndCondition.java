package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AndCondition extends CompositeCondition {

    public AndCondition() {
        super(WitnessConditionType.AND);
    }

    public AndCondition(List<WitnessCondition> expressions) {
        this();
        this.expressions = expressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AndCondition)) {
            return false;
        }
        AndCondition other = (AndCondition) o;
        return getType() == other.getType() &&
                Objects.equals(getExpressionList(), other.getExpressionList());
    }

    @Override
    public String toString() {
        return "AndCondition{" +
                "expressions=" + getExpressionList() +
                "}";
    }

}
