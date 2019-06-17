package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StackItem {

    @JsonProperty("type")
    protected StackItemType type;

    @JsonProperty("value")
    protected Object value;

    public StackItem() {
    }

    public StackItem(StackItemType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public StackItemType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackItem stackItem = (StackItem) o;
        return type == stackItem.type &&
                Objects.equals(value, stackItem.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "StackItem{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
