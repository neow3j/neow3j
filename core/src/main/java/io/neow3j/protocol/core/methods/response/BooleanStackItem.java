package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

public class BooleanStackItem extends StackItem {

    public BooleanStackItem(Boolean value) {
        super(StackItemType.BOOLEAN, value);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) this.value;
    }

}
