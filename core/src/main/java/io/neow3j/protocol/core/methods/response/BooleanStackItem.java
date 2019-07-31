package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;

import java.math.BigInteger;

public class BooleanStackItem extends StackItem {

    public BooleanStackItem() {
        super(StackItemType.BOOLEAN);
    }

    public BooleanStackItem(Boolean value) {
        super(StackItemType.BOOLEAN, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Boolean getValue() {
        return (Boolean) this.value;
    }

}
