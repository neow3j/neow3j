package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CalledByEntryCondition extends WitnessCondition {

    public CalledByEntryCondition() {
        super(WitnessConditionType.CALLED_BY_ENTRY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CalledByEntryCondition)) {
            return false;
        }
        CalledByEntryCondition other = (CalledByEntryCondition) o;
        return getType() == other.getType();
    }

    @Override
    public String toString() {
        return "CalledByEntryCondition";
    }

}
