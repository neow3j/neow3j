package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.types.StackItemType;

import java.util.Arrays;

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
