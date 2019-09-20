package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.util.List;

public class ArrayStackItem extends StackItem {

    public ArrayStackItem(List<StackItem> value) {
        super(StackItemType.ARRAY, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<StackItem> getValue() {
        return (List<StackItem>) this.value;
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

}
