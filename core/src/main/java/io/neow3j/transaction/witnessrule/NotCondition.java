package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

public class NotCondition extends WitnessCondition {

    private WitnessCondition expression;

    public NotCondition() {
        type = WitnessConditionType.NOT;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        expression = reader.readSerializable(WitnessCondition.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(expression);
    }

    @Override
    public int getSize() {
        return expression.getSize();
    }
}
