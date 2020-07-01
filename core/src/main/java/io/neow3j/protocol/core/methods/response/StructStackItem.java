package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StructStackItem extends StackItem {

    @JsonProperty("value")
    private List<StackItem> value;

    public StructStackItem() {
        super(StackItemType.STRUCT);
    }

    public StructStackItem(List<StackItem> value) {
        super(StackItemType.STRUCT);
        this.value = value;
    }

    public List<StackItem> getValue() {
        return this.value;
    }

    /**
     * Gets the stack item at the given position in this struct stack item.
     *
     * @param i the position of the desired stack item in this struct
     * @return the stack item at the given position.
     */
    public StackItem get(int i) {
        return getValue().get(i);
    }

    /**
     * Returns the number of elements that this struct stack item contains.
     *
     * @return the number of elements that this struct stack item contains.
     */
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructStackItem)) return false;
        StructStackItem other = (StructStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
