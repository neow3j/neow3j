package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.Objects;

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

    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "AnyStackItem{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnyStackItem)) return false;
        AnyStackItem other = (AnyStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
