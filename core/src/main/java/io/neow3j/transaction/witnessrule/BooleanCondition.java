package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;

import java.io.IOException;

public class BooleanCondition extends WitnessCondition {

    private boolean expression;

    public BooleanCondition() {
        type = WitnessConditionType.BOOLEAN;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws IOException {
        expression = reader.readBoolean();
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeBoolean(expression);
    }

    @Override
    public int getSize() {
        return 1; // one byte for boolean
    }
}
