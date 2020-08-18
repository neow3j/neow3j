package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.transaction.TransactionAttributeType;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAttribute {

    @JsonProperty("type")
    public TransactionAttributeType type;

    public TransactionAttribute() {
    }

    public TransactionAttribute(TransactionAttributeType type) {
        this.type = type;
    }

    public TransactionAttributeType getType() {
        return type;
    }
}