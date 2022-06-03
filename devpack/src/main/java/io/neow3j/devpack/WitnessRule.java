package io.neow3j.devpack;

import io.neow3j.devpack.constants.WitnessRuleAction;

/**
 * The rule used to describe the scope of the witness.
 */
public class WitnessRule {

    /**
     * Indicates the action to be taken if the current context meets with the rule.
     */
    public byte action;

    /**
     * The condition of the rule.
     */
    public WitnessRuleCondition condition;

}
