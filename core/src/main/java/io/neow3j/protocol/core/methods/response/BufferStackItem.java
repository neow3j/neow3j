package io.neow3j.protocol.core.methods.response;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BufferStackItem extends ByteArrayStackItem {

    public BufferStackItem() {
        super(StackItemType.BUFFER);
    }

    public BufferStackItem(byte[] value) {
        super(value, StackItemType.BUFFER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BufferStackItem)) {
            return false;
        }
        BufferStackItem other = (BufferStackItem) o;
        return getType() == other.getType() && Arrays.equals(this.getValue(), other.getValue());
    }
}
