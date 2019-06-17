package io.neow3j.protocol.core.methods.response.stack;

import io.neow3j.model.types.StackItem;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.StackItemParser;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Item extends StackItem {

    public String getAsAddress() {
        return StackItemParser.readAddress(this);
    }

    public String getAsString() {
        return StackItemParser.readString(this);
    }

    public BigInteger getAsNumber() {
        return StackItemParser.readNumber(this);
    }

    public Item() {
    }

    public Item(StackItemType type) {
        this.type = type;
    }

    public Item(StackItemType itemType, Object value) {
        super(itemType, value);
    }

    public List<Item> getArray() {
        return (List<Item>) getValue();
    }

    public Map<String, Item> getMap() {
        return (Map<String, Item>) getValue();
    }

}
