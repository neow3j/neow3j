package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.transaction.WitnessScope;

import java.io.IOException;

/**
 * This condition either gives permission to use the witness or not. This behaves like the
 * {@link WitnessScope#GLOBAL} in that {@code true} means the scope is global.
 */
public class BooleanCondition extends WitnessCondition {

    private boolean condition;

    public BooleanCondition() {
        type = WitnessConditionType.BOOLEAN;
    }

    /**
     * Constructs a condition with the given boolean value.
     *
     * @param condition The condition.
     */
    public BooleanCondition(boolean condition) {
        this();
        this.condition = condition;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws IOException {
        condition = reader.readBoolean();
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeBoolean(condition);
    }

    @Override
    public int getSize() {
        return 1; // one byte for boolean
    }
}
