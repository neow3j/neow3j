package io.neow3j.devpack;

public class WitnessRuleCondition {

    public static final int maxSubItems = 16;
    public static final int maxNestingDepth = 2;

    /**
     * The type of the witness condition.
     */
    public byte type;

    /**
     * The expression of the witness condition.
     */
    public Object value;

}
