package io.neow3j.protocol.core.stackitem;

import io.neow3j.types.StackItemType;

import java.util.List;
import java.util.Objects;

public class StructStackItem extends ListLikeStackItem {

    public StructStackItem() {
        super(StackItemType.STRUCT);
    }

    public StructStackItem(List<StackItem> value) {
        super(value, StackItemType.STRUCT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StructStackItem)) {
            return false;
        }
        StructStackItem other = (StructStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

}
