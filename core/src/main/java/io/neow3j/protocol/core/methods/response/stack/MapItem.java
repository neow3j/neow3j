package io.neow3j.protocol.core.methods.response.stack;

import io.neow3j.model.types.StackItemType;

import java.util.Map;

public class MapItem extends Item {

    private Map<String, Item> value;

    public MapItem() {
        super(StackItemType.MAP);
    }

    public MapItem(Map<String, Item> value) {
        super(StackItemType.MAP);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
