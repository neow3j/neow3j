package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.model.types.StackItemType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InteropInterfaceStackItem extends StackItem {

    @JsonProperty("value")
    private Object value;

    public InteropInterfaceStackItem() {
        super(StackItemType.INTEROP_INTERFACE);
    }

    public InteropInterfaceStackItem(Object value) {
        super(StackItemType.INTEROP_INTERFACE);
        this.value = value;
    }

    /**
     * Decodes the stack item's value and returns it.
     *
     * @return the value of this stack item.
     */
    public Object getValue() {
        return value;
    }

    @Override
    protected String valueToString() {
        return value.toString();
    }

    @Override
    public Object getInteropInterface() {
        nullCheck();
        return value;
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
