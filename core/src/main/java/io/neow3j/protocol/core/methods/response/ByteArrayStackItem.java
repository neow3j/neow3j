package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;

import java.math.BigInteger;

public class ByteArrayStackItem extends StackItem {

    public ByteArrayStackItem() {
        super(StackItemType.BYTE_ARRAY);
    }

    public ByteArrayStackItem(String value) {
        super(StackItemType.BYTE_ARRAY, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public String getValue() {
        return (String) this.value;
    }

    public String getAsAddress() {
        return StackItemParser.readAddress(this);
    }

    public String getAsString() {
        return StackItemParser.readString(this);
    }

    public BigInteger getAsNumber() {
        return StackItemParser.readNumber(this);
    }

}
