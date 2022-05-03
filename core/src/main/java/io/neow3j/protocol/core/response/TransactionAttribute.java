package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.transaction.TransactionAttributeType;

import static java.lang.String.format;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = HighPriorityAttribute.class, name = TransactionAttributeType.HIGH_PRIORITY_VALUE),
        @JsonSubTypes.Type(value = OracleResponseAttribute.class, name = TransactionAttributeType.ORACLE_RESPONSE_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TransactionAttribute {

    @JsonProperty("type")
    public TransactionAttributeType type;

    public TransactionAttribute() {
    }

    public TransactionAttribute(TransactionAttributeType type) {
        this.type = type;
    }

    /**
     * Casts this transaction attribute to a {@link HighPriorityAttribute} if possible, and returns it.
     *
     * @return this transaction attribute as a {@link HighPriorityAttribute}.
     * @throws IllegalStateException if this transaction attribute is not an instance of {@link HighPriorityAttribute}.
     */
    @JsonIgnore
    public HighPriorityAttribute asHighPriority() {
        if (this instanceof HighPriorityAttribute) {
            return (HighPriorityAttribute) this;
        }
        throw new IllegalStateException(format("This transaction attribute is not of type %s but of %s.",
                TransactionAttributeType.HIGH_PRIORITY.jsonValue(), type.jsonValue()));
    }

    public TransactionAttributeType getType() {
        return type;
    }

    public static TransactionAttribute fromType(TransactionAttributeType type) {
        switch (type) {
            case HIGH_PRIORITY:
                return new HighPriorityAttribute();
            case ORACLE_RESPONSE:
                return new OracleResponseAttribute();
            default:
                throw new IllegalArgumentException(
                        "No concrete class found for transaction attribute type " + type.jsonValue());
        }
    }

}
