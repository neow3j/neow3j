package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.exceptions.WitnessConditionCastException;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.types.Hash160;

import java.util.List;

import static java.lang.String.format;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = BooleanCondition.class, name = WitnessConditionType.BOOLEAN_VALUE),
        @JsonSubTypes.Type(value = NotCondition.class, name = WitnessConditionType.NOT_VALUE),
        @JsonSubTypes.Type(value = AndCondition.class, name = WitnessConditionType.AND_VALUE),
        @JsonSubTypes.Type(value = OrCondition.class, name = WitnessConditionType.OR_VALUE),
        @JsonSubTypes.Type(value = ScriptHashCondition.class, name = WitnessConditionType.SCRIPT_HASH_VALUE),
        @JsonSubTypes.Type(value = GroupCondition.class, name = WitnessConditionType.GROUP_VALUE),
        @JsonSubTypes.Type(value = CalledByEntryCondition.class, name = WitnessConditionType.CALLED_BY_ENTRY_VALUE),
        @JsonSubTypes.Type(value = CalledByContractCondition.class,
                           name = WitnessConditionType.CALLED_BY_CONTRACT_VALUE),
        @JsonSubTypes.Type(value = CalledByGroupCondition.class, name = WitnessConditionType.CALLED_BY_GROUP_VALUE)})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class WitnessCondition {

    @JsonProperty("type")
    private WitnessConditionType type;

    public WitnessCondition() {
    }

    public WitnessCondition(WitnessConditionType type) {
        this.type = type;
    }

    public WitnessConditionType getType() {
        return type;
    }

    /**
     * @return the boolean expression value.
     * @throws WitnessConditionCastException if the value of the witness condition cannot be converted to a boolean.
     */
    @JsonIgnore
    public boolean getBooleanExpression() {
        throw new WitnessConditionCastException(format("Cannot retrieve a boolean from value %s.", toString()));
    }

    /**
     * @return the expression.
     * @throws WitnessConditionCastException if the value of the witness condition cannot be converted to a
     *                                       {@link WitnessCondition}.
     */
    @JsonIgnore
    public WitnessCondition getExpression() {
        throw new WitnessConditionCastException(format("Cannot retrieve a condition from value %s.", toString()));
    }

    /**
     * @return the list of expressions.
     * @throws WitnessConditionCastException if the value of the witness condition cannot be converted to a list of
     *                                       {@link WitnessCondition}s.
     */
    @JsonIgnore
    public List<WitnessCondition> getExpressionList() {
        throw new WitnessConditionCastException(
                format("Cannot retrieve a list of witness conditions from value %s.", toString()));
    }

    /**
     * @return the {@link Hash160} value.
     * @throws WitnessConditionCastException if the value of the witness condition cannot be converted to a
     *                                       {@link Hash160}.
     */
    @JsonIgnore
    public Hash160 getScriptHash() {
        throw new WitnessConditionCastException(format("Cannot retrieve a Hash160 from value %s.", toString()));
    }

    /**
     * @return the {@link ECPublicKey} value.
     * @throws WitnessConditionCastException if the value of the witness condition cannot be converted to a
     *                                       {@link ECPublicKey}.
     */
    @JsonIgnore
    public ECPublicKey getGroup() {
        throw new WitnessConditionCastException(format("Cannot retrieve a ECPublicKey from value %s.", toString()));
    }

}
