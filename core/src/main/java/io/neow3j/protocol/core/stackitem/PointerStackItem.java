package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.StackItemType;

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
    protected String valueToString() {
        return value.toString();
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public BigInteger getInteger() {
        nullCheck();
        return value;
    }

    @Override
    public BigInteger getPointer() {
        nullCheck();
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointerStackItem)) {
            return false;
        }
        PointerStackItem other = (PointerStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

}
