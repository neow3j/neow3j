package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.types.StackItemType;
import io.neow3j.utils.Numeric;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ByteStringStackItem extends ByteArrayStackItem {

    public ByteStringStackItem() {
        super(StackItemType.BYTE_STRING);
    }

    public ByteStringStackItem(byte[] value) {
        super(value, StackItemType.BYTE_STRING);
    }

    public ByteStringStackItem(String hexValue) {
        this(Numeric.hexStringToByteArray(hexValue));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteStringStackItem)) {
            return false;
        }
        ByteStringStackItem other = (ByteStringStackItem) o;
        return getType() == other.getType() &&
                Arrays.equals(this.getValue(), other.getValue());
    }

}
