package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

import java.util.List;

public abstract class CompositeCondition extends WitnessCondition {

    @JsonProperty("expressions")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    protected List<WitnessCondition> expressions;

    protected CompositeCondition(WitnessConditionType type) {
        super(type);
    }

    @Override
    public List<WitnessCondition> getExpressionList() {
        return expressions;
    }

}
