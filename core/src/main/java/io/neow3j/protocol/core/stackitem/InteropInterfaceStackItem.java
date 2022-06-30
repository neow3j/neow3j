package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.StackItemType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InteropInterfaceStackItem extends StackItem {

    @JsonProperty("interface")
    private String interfaceName;

    @JsonProperty("id")
    private String iteratorId;

    public InteropInterfaceStackItem() {
        super(StackItemType.INTEROP_INTERFACE);
    }

    public InteropInterfaceStackItem(String interfaceName, String iteratorId) {
        super(StackItemType.INTEROP_INTERFACE);
        this.interfaceName = interfaceName;
        this.iteratorId = iteratorId;
    }

    @Override
    public String getValue() {
        return iteratorId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    protected String valueToString() {
        return iteratorId;
    }

    @Override
    public String getIteratorId() {
        return iteratorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InteropInterfaceStackItem)) {
            return false;
        }
        InteropInterfaceStackItem other = (InteropInterfaceStackItem) o;
        return getType() == other.getType() &&
                getInterfaceName().equals(other.getInterfaceName()) &&
                getIteratorId().equals(other.getIteratorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getInterfaceName(), getIteratorId());
    }

}
