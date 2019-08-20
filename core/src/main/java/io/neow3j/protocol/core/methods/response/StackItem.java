package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;
import io.neow3j.protocol.deserializer.StackDeserializer;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = StackDeserializer.class)
public class StackItem {

    @JsonProperty("type")
    protected StackItemType type;

    @JsonProperty("value")
    protected Object value;

    public StackItem() {
    }

    public StackItem(StackItemType type) {
        this.type = type;
    }

    public StackItem(StackItemType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public StackItemType getType() {
        return type;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackItem stackItem = (StackItem) o;
        return type == stackItem.type &&
                Objects.equals(getValue(), stackItem.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getValue());
    }

    @Override
    public String toString() {
        return "StackItem{" +
                "type=" + type +
                ", value=" + getValue() +
                '}';
    }
}
