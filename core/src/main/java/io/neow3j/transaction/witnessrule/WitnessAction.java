package io.neow3j.transaction.witnessrule;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.lang.String.format;

/**
 * Indicates the action to be taken if the current context meets with the rule.
 */
public enum WitnessAction {

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

    WitnessAction(String jsonValue, int byteValue) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) byteValue;
    }

    public static WitnessAction valueOf(byte byteValue) {
        for (WitnessAction e : WitnessAction.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(format("%s value type not found.", WitnessAction.class.getName()));
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

}
