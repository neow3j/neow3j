package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.List;

public class OrCondition extends WitnessCondition {

    private List<WitnessCondition> expressions;

    public OrCondition() {
        type = WitnessConditionType.OR;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        expressions = reader.readSerializableList(WitnessCondition.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(expressions);
    }

    @Override
    public int getSize() {
        return IOUtils.getVarSize(expressions);
    }
}
