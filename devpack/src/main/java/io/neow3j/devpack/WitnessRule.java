package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

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

    /**
     * Compares this WitnessRule to the given object. The comparison happens by reference only. I.e., if you retrieve
     * the same witness rule twice, e.g., from
     * {@link io.neow3j.devpack.contracts.LedgerContract#getTransactionSigners(Hash256)}, then comparing the two will
     * return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same witness rule. False, otherwise.
     */
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given witness rule by value.
     * <p>
     * Note, that when using this implementation, only the action is compared by value, and the condition is compared
     * by reference only, since its structure is unknown.
     *
     * @param r another witness rule to compare to.
     * @return true if all fields of the two witness rules are equal. False, otherwise.
     */
    public boolean equals(WitnessRule r) {
        return this == r || (action == r.action && condition == r.condition);
    }

}
