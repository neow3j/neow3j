package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.math.BigInteger;

public class IntegerStackItem extends StackItem {

    public IntegerStackItem(BigInteger value) {
        super(StackItemType.INTEGER, value);
    }

    @Override
    public BigInteger getValue() {
        return (BigInteger) this.value;
    }
}
