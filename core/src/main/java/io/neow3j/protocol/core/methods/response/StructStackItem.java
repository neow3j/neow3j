package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StructStackItem extends ListLikeStackItem {

    public StructStackItem() {
        super(StackItemType.STRUCT);
    }

    public StructStackItem(List<StackItem> value) {
        super(value, StackItemType.STRUCT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructStackItem)) return false;
        StructStackItem other = (StructStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

}
