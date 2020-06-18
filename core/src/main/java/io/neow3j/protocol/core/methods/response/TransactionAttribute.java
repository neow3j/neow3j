package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.transaction.TransactionAttributeType;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = TransactionCosigner.class, name =
                TransactionAttributeType.COSIGNER_VALUE)
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

    @JsonIgnore
    public TransactionCosigner getAsTransactionCosigner() {
        if (this instanceof TransactionCosigner) {
            return (TransactionCosigner) this;
        }
        throw new IllegalStateException("This object is not of type " +
                TransactionAttributeType.COSIGNER_VALUE + " but of " + this.type.jsonValue());
    }

    public TransactionAttributeType getType() {
        return type;
    }
}