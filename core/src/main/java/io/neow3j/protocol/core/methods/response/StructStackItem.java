package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.util.List;

public class StructStackItem extends StackItem {

    public StructStackItem(List<StackItem> value) {
        super(StackItemType.STRUCT, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<StackItem> getValue() {
        return (List<StackItem>) this.value;
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
}
