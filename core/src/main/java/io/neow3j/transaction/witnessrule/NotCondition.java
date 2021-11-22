package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Reverses another condition.
 */
public class NotCondition extends CompositeCondition {

    private WitnessCondition condition;

    public NotCondition() {
        type = WitnessConditionType.NOT;
    }

    /**
     * Constructs the reverse of the given condition.
     *
     * @param condition the condition to reverse.
     */
    public NotCondition(WitnessCondition condition) {
        this();
        this.condition = condition;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        condition = WitnessCondition.deserializeWitnessCondition(reader);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(condition);
    }

    @Override
    public int getSize() {
        return condition.getSize();
    }

    @Override
    public List<WitnessCondition> getConditions() {
        return Arrays.asList(condition);
    }

    public WitnessCondition getCondition() {
        return condition;
    }
}
