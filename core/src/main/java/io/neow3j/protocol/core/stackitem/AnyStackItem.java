package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.StackItemType;

import java.util.Objects;

/**
 * This stack item can represent any of the other stack items and can also appear if the item's value is null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnyStackItem extends StackItem {

    @JsonProperty("value")
    private Object value;

    public AnyStackItem() {
        super(StackItemType.ANY);
    }

    public AnyStackItem(Object value) {
        super(StackItemType.ANY);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    protected String valueToString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnyStackItem)) {
            return false;
        }
        AnyStackItem other = (AnyStackItem) o;
        return getType() == other.getType() &&
                (getValue() == other.getValue() || getValue().equals(other.getValue()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

}
