package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InteropInterfaceStackItem extends StackItem {

    @JsonProperty("iterator")
    private List<StackItem> iterator;

    @JsonProperty("truncated")
    private Boolean truncated;

    public InteropInterfaceStackItem() {
        super(StackItemType.INTEROP_INTERFACE);
    }

    @Override
    public List<StackItem> getValue() {
        return iterator;
    }

    @Override
    protected String valueToString() {
        return iterator.stream()
                .map(StackItem::toString)
                .reduce("", (a, b) ->  a + ", " + b)
                .substring(2); // remove the first comma and space
    }

    public InteropInterfaceStackItem(List<StackItem> iterator, Boolean truncated) {
        super(StackItemType.INTEROP_INTERFACE);
        this.iterator = iterator;
        this.truncated = truncated;
    }


    @Override
    public List<StackItem> getIterator() {
        return iterator;
    }

    public boolean isTruncated() {
        return truncated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InteropInterfaceStackItem)) return false;
        InteropInterfaceStackItem other = (InteropInterfaceStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
