package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.util.List;

public class ArrayStackItem extends StackItem {

    public ArrayStackItem() {
        super(StackItemType.ARRAY);
    }

    public ArrayStackItem(List<StackItem> value) {
        super(StackItemType.ARRAY, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<StackItem> getValue() {
        return (List<StackItem>) this.value;
    }

}
