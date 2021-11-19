package io.neow3j.transaction.witnessrule;

public enum WitnessConditionType {

    /**
     * Indicates that the condition will always be met or not met.
     */
    BOOLEAN("Boolean", 0x00, BooleanCondition.class),

    /**
     * Reverse another condition.
     */
    NOT("Not", 0x01, NotCondition.class),

    /**
     * Indicates that all conditions must be met.
     */
    AND("And", 0x02, AndCondition.class),

    /**
     * Indicates that any of the conditions meets.
     */
    OR("Or", 0x03, OrCondition.class),

    /**
     * Indicates that the condition is met when the current context has the specified script
     * hash.
     */
    SCRIPT_HASH("ScriptHash", 0x18, ScriptHashCondition.class),

    /**
     * Indicates that the condition is met when the current context has the specified group.
     */
    GROUP("Group", 0x19, GroupCondition.class),

    /**
     * Indicates that the condition is met when the current context is the entry point or is
     * called by the entry point.
     */
    CALLED_BY_ENTRY("CalledByEntry", 0x20, CalledByEntryCondition.class),

    /**
     * Indicates that the condition is met when the current context is called by the
     * specified contract.
     */
    CALLED_BY_CONTRACT("CalledByContract", 0x28, CalledByContractCondition.class),

    /**
     * Indicates that the condition is met when the current context is called by the
     * specified group.
     */
    CALLED_BY_GROUP("CalledByGroup", 0x29, CalledByGroupCondition.class);

    private String jsonValue;
    private byte byteValue;
    private Class<? extends WitnessCondition> conditionClass;

    WitnessConditionType(String jsonValue, int byteValue,
            Class<? extends WitnessCondition> conditionClass) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
        this.conditionClass = conditionClass;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public static WitnessConditionType valueOf(byte byteValue) {
        for (WitnessConditionType e : WitnessConditionType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.",
                WitnessConditionType.class.getName()));
    }

    public int getSize()  {
        // TODO: If the size is fixed then move it to the enum definition.
        try {
            return this.conditionClass.getDeclaredField("size").getInt(null);
        } catch (Exception ignore) {
            return 0;
        }
    }
}
