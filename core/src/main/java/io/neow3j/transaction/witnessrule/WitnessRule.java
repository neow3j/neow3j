package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * The rule used to describe the scope of a witness.
 */
public class WitnessRule extends NeoSerializable {

    /**
     * Indicates the action to be taken if the current context meets with the rule.
     */
    private WitnessAction action;

    /**
     * The condition of the rule.
     */
    private WitnessCondition condition;

    public WitnessRule() {
    }

    public WitnessRule(WitnessAction action, WitnessCondition condition) {
        this.action = action;
        this.condition = condition;
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            action = WitnessAction.valueOf(reader.readByte());
            condition = WitnessCondition.deserializeWitnessCondition(reader);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(action.byteValue());
        writer.writeSerializableFixed(condition);
    }

    @Override
    public int getSize() {
        return 1 // one byte for WitnessRuleAction
                + condition.getSize();
    }

    public WitnessAction getAction() {
        return action;
    }

    public WitnessCondition getCondition() {
        return condition;
    }

}
