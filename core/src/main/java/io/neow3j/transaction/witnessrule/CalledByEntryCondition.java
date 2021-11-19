package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;

public class CalledByEntryCondition extends WitnessCondition {

    public CalledByEntryCondition() {
        type = WitnessConditionType.CALLED_BY_ENTRY;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) {
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) {
    }

    @Override
    public int getSize() {
        return 0;
    }
}
