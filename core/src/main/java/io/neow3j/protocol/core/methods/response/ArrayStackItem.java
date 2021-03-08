package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ArrayStackItem extends ListLikeStackItem {

    public ArrayStackItem() {
        super(StackItemType.ARRAY);
    }

    public ArrayStackItem(List<StackItem> value) {
        super(value, StackItemType.ARRAY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayStackItem)) return false;
        ArrayStackItem other = (ArrayStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }
}
