package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.StackItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stack item that has a list-like value (e.g., array).
 * <p>
 * This abstraction does not represent an actual stack item type from the neo-vm.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ListLikeStackItem extends StackItem {

    @JsonProperty("value")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<StackItem> value = new ArrayList<>();

    protected ListLikeStackItem(StackItemType type) {
        super(type);
    }

    protected ListLikeStackItem(List<StackItem> value, StackItemType type) {
        super(type);
        this.value = value;
    }

    @Override
    public List<StackItem> getValue() {
        return this.value;
    }

    @Override
    protected String valueToString() {
        if (value.isEmpty()) {
            return "";
        }
        return value.stream()
                .map(StackItem::toString)
                .reduce("", (a, b) -> a + ", " + b)
                .substring(2); // remove the first comma and space
    }

    @Override
    public List<StackItem> getList() {
        nullCheck();
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListLikeStackItem)) {
            return false;
        }
        ListLikeStackItem other = (ListLikeStackItem) o;
        return getType() == other.getType() &&
                Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
