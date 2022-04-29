package io.neow3j.transaction.witnessrule;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents a witness condition where all its contained expressions must be met.
 */
public class AndCondition extends CompositeCondition {

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
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.AndCondition(
                getExpressions().stream().map(WitnessCondition::toDTO).collect(Collectors.toList()));
    }

}
