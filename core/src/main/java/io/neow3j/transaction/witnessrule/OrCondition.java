package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents a witness condition where at least one of its contained expressions must be met.
 */
public class OrCondition extends CompositeCondition {

    public OrCondition() {
        type = WitnessConditionType.OR;
        expressions = new ArrayList<>();
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#OR} with the given expressions.
     *
     * @param expressions the expressions.
     * @throws IllegalArgumentException if more than {@link WitnessCondition#MAX_SUBITEMS} are added.
     */
    public OrCondition(WitnessCondition... expressions) {
        this();
        if (expressions.length > MAX_SUBITEMS) {
            throw new IllegalArgumentException(
                    format("A maximum of %s subitems is allowed for an OR witness condition.", MAX_SUBITEMS));
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
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.OrCondition(
                getExpressions().stream().map(WitnessCondition::toDTO).collect(Collectors.toList()));
    }

}
