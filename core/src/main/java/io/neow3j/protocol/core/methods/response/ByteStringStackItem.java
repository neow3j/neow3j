package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ByteStringStackItem extends ByteArrayStackItem {

    public ByteStringStackItem() {
        super(StackItemType.BYTE_STRING);
    }

    public ByteStringStackItem(byte[] value) {
        super(value, StackItemType.BYTE_STRING);
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
        return getType() == other.getType() && Arrays.equals(this.getValue(), other.getValue());
    }
}
