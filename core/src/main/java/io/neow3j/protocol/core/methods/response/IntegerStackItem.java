package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;

import java.math.BigInteger;

public class IntegerStackItem extends StackItem {

    public IntegerStackItem() {
        super(StackItemType.INTEGER);
    }

    public IntegerStackItem(BigInteger value) {
        super(StackItemType.INTEGER, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public BigInteger getValue() {
        return (BigInteger) this.value;
    }
}
