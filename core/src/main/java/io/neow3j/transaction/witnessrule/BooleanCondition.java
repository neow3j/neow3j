package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.transaction.WitnessScope;

import java.io.IOException;

/**
 * This condition either gives permission to use the witness or not. It behaves like the {@link WitnessScope#GLOBAL}
 * in that {@code true} means the scope is global.
 */
public class BooleanCondition extends WitnessCondition {

    private boolean expression;

    public BooleanCondition() {
        type = WitnessConditionType.BOOLEAN;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#BOOLEAN} with the given boolean value.
     *
     * @param expression the boolean expression.
     */
    public BooleanCondition(boolean expression) {
        this();
        this.expression = expression;
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
        return super.getSize() + 1; // one byte for boolean
    }

    public boolean getExpression() {
        return expression;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.BooleanCondition(getExpression());
    }

}
