package io.neow3j.devpack.constants;

public class WitnessRuleConditionType {

    /**
     * Indicates that the condition will always be met or not met.
     */
    public static final byte Boolean = 0x00;

    /**
     * Reverse another condition.
     */
    public static final byte Not = 0x01;

    /**
     * Indicates that all conditions must be met.
     */
    public static final byte And = 0x02;

    /**
     * Indicates that any of the conditions meets.
     */
    public static final byte Or = 0x03;

    /**
     * Indicates that the condition is met when the current context has the specified script hash.
     */
    public static final byte ScriptHash = 0x18;

    /**
     * Indicates that the condition is met when the current context has the specified group.
     */
    public static final byte Group = 0x19;

    /**
     * Indicates that the condition is met when the current context is the entry point or is called by the entry point.
     */
    public static final byte CalledByEntry = 0x20;

    /**
     * Indicates that the condition is met when the current context is called by the specified contract.
     */
    public static final byte CalledByContract = 0x28;

    /**
     * Indicates that the condition is met when the current context is called by the specified group.
     */
    public static final byte CalledByGroup = 0x29;

}
