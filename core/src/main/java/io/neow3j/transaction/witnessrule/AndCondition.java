package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.List;

/**
 * Represents the condition that all its sub-conditions must be met.
 */
public class AndCondition extends WitnessCondition {

    private List<WitnessCondition> conditions;

    public AndCondition() {
        type = WitnessConditionType.AND;
    }

    /**
     * Constructs an AND condition with the given sub-conditions.
     *
     * @param conditions The conditions.
     * @throws IllegalArgumentException if more than {@link WitnessCondition#MAX_SUBITEMS} are
     *                                  added.
     */
    public AndCondition(List<WitnessCondition> conditions) {
        this();
        if (conditions.size() > MAX_SUBITEMS) {
            throw new IllegalArgumentException("A maximum of " + MAX_SUBITEMS + " subitems is " +
                    "allowed for the AND witness condition.");
        }
        this.conditions = conditions;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        conditions = reader.readSerializableList(WitnessCondition.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(conditions);
    }

    @Override
    public int getSize() {
        return IOUtils.getVarSize(conditions);
    }
}
