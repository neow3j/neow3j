package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InteropInterfaceStackItem extends StackItem {

    @JsonProperty("value")
    private String value;

    public InteropInterfaceStackItem() {
        super(StackItemType.INTEROP_INTERFACE);
    }

    public InteropInterfaceStackItem(String value) {
        super(StackItemType.INTEROP_INTERFACE);
        this.value = value;
    }

    /**
     * Decodes the stack item's value and returns it.
     *
     * @return the value of this stack item.
     */
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof io.neow3j.protocol.core.methods.response.InteropInterfaceStackItem)) return false;
        io.neow3j.protocol.core.methods.response.InteropInterfaceStackItem other =
                (io.neow3j.protocol.core.methods.response.InteropInterfaceStackItem) o;
        return getType() == other.getType()
                && getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
