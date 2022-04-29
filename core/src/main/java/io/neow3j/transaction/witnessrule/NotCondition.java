package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;

/**
 * Reverses an expression.
 */
public class NotCondition extends WitnessCondition {

    private WitnessCondition expression;

    public NotCondition() {
        type = WitnessConditionType.NOT;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#NOT}.
     *
     * @param expression the expression to reverse.
     */
    public NotCondition(WitnessCondition expression) {
        this();
        this.expression = expression;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        expression = WitnessCondition.deserializeWitnessCondition(reader);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(expression);
    }

    @Override
    public int getSize() {
        return super.getSize() + expression.getSize();
    }

    public WitnessCondition getExpression() {
        return expression;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.NotCondition(getExpression().toDTO());
    }

}
