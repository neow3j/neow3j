package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.neow3j.model.types.StackItemType;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PointerStackItem extends StackItem {

    @JsonProperty("value")
    private BigInteger value;

    public PointerStackItem() {
        super(StackItemType.POINTER);
    }

    public PointerStackItem(BigInteger value) {
        super(StackItemType.POINTER);
        this.value = value;
    }

    @Override
    public String valueToString() {
        return value.toString();
    }

    @JsonValue
    public BigInteger getValue() {
        return value;
    }

    @Override
    public BigInteger getInteger() {
        return value;
    }

    @Override
    public BigInteger getPointer() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointerStackItem)) return false;
        PointerStackItem other = (PointerStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }
}
