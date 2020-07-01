package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArrayStackItem extends StackItem {

    @JsonProperty("value")
    private List<StackItem> value;

    public ArrayStackItem() {
        super(StackItemType.ARRAY);
    }

    public ArrayStackItem(List<StackItem> value) {
        super(StackItemType.ARRAY);
        this.value = value;
    }

    public List<StackItem> getValue() {
        return this.value;
    }

    /**
     * Gets the stack item at the given index of this stack item array.
     *
     * @param i index of the stack item to return
     * @return the stack item at the given index
     */
    public StackItem get(int i) {
        return getValue().get(i);
    }

    /**
     * Returns the number of elements in this array stack item.
     *
     * @return the number of elements in this array stack item.
     */
    public int size() {
        return getValue().size();
    }

    /**
     * Returns true if this array stack item contains no elements.
     *
     * @return true if this array stack item contains no elements. False, otherwise.
     */
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayStackItem)) return false;
        ArrayStackItem other = (ArrayStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
