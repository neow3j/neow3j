package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

public class AnyStackItem extends StackItem {

    public AnyStackItem() {
        super(StackItemType.ANY, null);
    }

    @Override
    public String toString() {
        return "AnyStackItem{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}