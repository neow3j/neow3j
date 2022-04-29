package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.List;

/**
 * Provides the variable and methods used in witness conditions that contain a list of expressions. I.e.,
 * {@link AndCondition} and {@link OrCondition}.
 */
public abstract class CompositeCondition extends WitnessCondition {

    protected List<WitnessCondition> expressions;

    public List<WitnessCondition> getExpressions() {
        return expressions;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        try {
            long nrOfExpressions = reader.readVarInt();
            for (int i = 0; i < nrOfExpressions; i++) {
                this.expressions.add(WitnessCondition.deserializeWitnessCondition(reader));
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableVariable(expressions);
    }

    @Override
    public int getSize() {
        return super.getSize() + IOUtils.getVarSize(expressions);
    }

}
