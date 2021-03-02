package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StructStackItem extends StackItem {

    @JsonProperty("value")
    private StackItem[] value;

    public StructStackItem() {
        super(StackItemType.STRUCT);
    }

    public StructStackItem(StackItem[] value) {
        super(StackItemType.STRUCT);
        this.value = value;
    }

    public StackItem[] getValue() {
        return this.value;
    }

    @Override
    public String valueToString() {
        return Stream.of(value)
                .map(StackItem::toString)
                .reduce("", (a, b) ->  a + ", " + b)
                .substring(1); // remove the first comma.
    }

    @Override
    public StackItem[] getArray() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructStackItem)) return false;
        StructStackItem other = (StructStackItem) o;
        return getType() == other.getType() &&
                Arrays.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
