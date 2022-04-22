package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents a witness condition where all its contained expressions must be met.
 */
public class AndCondition extends CompositeCondition {

    private List<WitnessCondition> expressions;

    public AndCondition() {
        type = WitnessConditionType.AND;
        expressions = new ArrayList<>();
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#AND} with the given expressions.
     *
     * @param expressions the expressions.
     * @throws IllegalArgumentException if more than {@link WitnessCondition#MAX_SUBITEMS} are added.
     */
    public AndCondition(WitnessCondition... expressions) {
        this();
        if (expressions.length > MAX_SUBITEMS) {
            throw new IllegalArgumentException(
                    format("A maximum of %s subitems is allowed for an AND witness condition.", MAX_SUBITEMS));
        }
        this.expressions = asList(expressions);
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

    @Override
    public List<WitnessCondition> getExpressions() {
        return expressions;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toJson() {
        return new io.neow3j.protocol.core.witnessrule.AndCondition(
                getExpressions().stream().map(WitnessCondition::toJson).collect(Collectors.toList()));
    }

}
