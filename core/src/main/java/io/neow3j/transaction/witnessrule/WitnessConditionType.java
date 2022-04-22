package io.neow3j.transaction.witnessrule;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

public enum WitnessConditionType {

    /**
     * Indicates that the condition will always be met or not met.
     */
    BOOLEAN(WitnessConditionType.BOOLEAN_VALUE, 0x00, BooleanCondition.class),

    /**
     * Reverse another condition.
     */
    NOT(WitnessConditionType.NOT_VALUE, 0x01, NotCondition.class),

    /**
     * Indicates that all conditions must be met.
     */
    AND(WitnessConditionType.AND_VALUE, 0x02, AndCondition.class),

    /**
     * Indicates that any of the conditions meets.
     */
    OR(WitnessConditionType.OR_VALUE, 0x03, OrCondition.class),

    /**
     * Indicates that the condition is met when the current context has the specified script hash.
     */
    SCRIPT_HASH(WitnessConditionType.SCRIPT_HASH_VALUE, 0x18, ScriptHashCondition.class),

    /**
     * Indicates that the condition is met when the current context has the specified group.
     */
    GROUP(WitnessConditionType.GROUP_VALUE, 0x19, GroupCondition.class),

    /**
     * Indicates that the condition is met when the current context is the entry point or is called by the entry point.
     */
    CALLED_BY_ENTRY(WitnessConditionType.CALLED_BY_ENTRY_VALUE, 0x20, CalledByEntryCondition.class),

    /**
     * Indicates that the condition is met when the current context is called by the specified contract.
     */
    CALLED_BY_CONTRACT(WitnessConditionType.CALLED_BY_CONTRACT_VALUE, 0x28, CalledByContractCondition.class),

    /**
     * Indicates that the condition is met when the current context is called by the specified group.
     */
    CALLED_BY_GROUP(WitnessConditionType.CALLED_BY_GROUP_VALUE, 0x29, CalledByGroupCondition.class);

    public static final String BOOLEAN_VALUE = "Boolean";
    public static final String NOT_VALUE = "Not";
    public static final String AND_VALUE = "And";
    public static final String OR_VALUE = "Or";
    public static final String SCRIPT_HASH_VALUE = "ScriptHash";
    public static final String GROUP_VALUE = "Group";
    public static final String CALLED_BY_ENTRY_VALUE = "CalledByEntry";
    public static final String CALLED_BY_CONTRACT_VALUE = "CalledByContract";
    public static final String CALLED_BY_GROUP_VALUE = "CalledByGroup";

    private String jsonValue;
    private byte byteValue;
    private Class<? extends WitnessCondition> conditionClass;

    WitnessConditionType(String jsonValue, int byteValue, Class<? extends WitnessCondition> conditionClass) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
        this.conditionClass = conditionClass;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public Class<? extends WitnessCondition> conditionClass() {
        return conditionClass;
    }

    public static WitnessConditionType valueOf(byte byteValue) {
        for (WitnessConditionType e : WitnessConditionType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", WitnessConditionType.class.getName()));
    }

    @Override
    public String toString() {
        return jsonValue;
    }

}
