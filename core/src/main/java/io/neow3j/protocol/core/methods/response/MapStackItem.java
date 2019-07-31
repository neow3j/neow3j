package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapStackItem extends StackItem {

    public MapStackItem() {
        super(StackItemType.MAP);
    }

    public MapStackItem(Map<StackItem, StackItem> value) {
        super(StackItemType.MAP, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Map<StackItem, StackItem> getValue() {
        return (Map<StackItem, StackItem>) this.value;
    }

    public StackItem get(String key) {
        for (Entry<StackItem, StackItem> e : getValue().entrySet()) {
            if (e.getKey() instanceof ByteArrayStackItem) {
                if (((ByteArrayStackItem) e.getKey()).getAsString().equals(key)) {
                    return e.getValue();
                }
            }
        }
        return null;
    }
}
