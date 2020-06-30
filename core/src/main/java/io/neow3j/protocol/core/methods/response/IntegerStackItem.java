package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.model.types.StackItemType;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegerStackItem extends StackItem {

    @JsonProperty("value")
    private BigInteger value;

    public IntegerStackItem() {
        super(StackItemType.INTEGER);
    }

    public IntegerStackItem(BigInteger value) {
        super(StackItemType.INTEGER);
        this.value = value;
    }

    @JsonSetter("value")
    public void setValue(BigInteger value) {
        if (value != null) {
            this.value = value;
        } else {
            this.value = BigInteger.ZERO;
        }
    }

    @JsonValue
    public BigInteger getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerStackItem)) return false;
        IntegerStackItem other = (IntegerStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }
}
