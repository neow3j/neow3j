package io.neow3j.transaction.witnessrule;

import java.util.List;

public abstract class CompositeCondition extends WitnessCondition {

    public CompositeCondition() {
    }

    public abstract List<WitnessCondition> getConditions();

}
