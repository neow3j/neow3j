package io.neow3j.transaction.witnessrule;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Indicates the action to be taken if the current context meets with the rule.
 */
public enum WitnessRuleAction {

    /**
     * Deny the witness according to the rule.
     */
    DENY("Deny", 0),

    /**
     * Allow the witness according to the rule.
     */
    ALLOW("Allow", 1);

    private String jsonValue;
    private byte byteValue;

    WitnessRuleAction(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    public static WitnessRuleAction valueOf(byte byteValue) {
        for (WitnessRuleAction e : WitnessRuleAction.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.",
                WitnessRuleAction.class.getName()));
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }
}
