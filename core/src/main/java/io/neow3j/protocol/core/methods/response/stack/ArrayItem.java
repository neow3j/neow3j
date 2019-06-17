package io.neow3j.protocol.core.methods.response.stack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.model.types.StackItemType;

import java.util.List;

public class ArrayItem extends Item {

    @JsonProperty("value")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Item> value;

    public ArrayItem() {
        super(StackItemType.ARRAY);
    }

    public ArrayItem(List<Item> value) {
        super(StackItemType.ARRAY);
        this.value = value;
    }

    public List<Item> getValue() {
        return value;
    }
}
