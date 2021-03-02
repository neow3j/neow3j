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
public class BufferStackItem extends StackItem {

    @JsonProperty("value")
    private byte[] value;

    public BufferStackItem() {
        super(StackItemType.BUFFER);
    }

    public BufferStackItem(byte[] value) {
        super(StackItemType.BUFFER);
        this.value = value;
    }

    /**
     * Returns the stack item's value as a byte array.
     *
     * @return the value of this stack item.
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * Gets this item's value as an address.
     * <p>
     * Treats the underlying bytes as a script hash in little-endian order.
     *
     * @return the address.
     */
    @Override
    public String getAddress() {
        try {
            return new ScriptHash(value).toAddress();
        } catch (IllegalArgumentException e) {
            return super.getAddress();
        }
    }

    /**
     * Treats this item's value as a UTF-8 encoded string, i.e. reads and returns the underlying
     * bytes as a UTF-8 string.
     *
     * @return the string.
     */
    @Override
    public String getString() {
        return new String(value, UTF_8);
    }

    /**
     * Gets this item's value as a hexadecimal string.
     *
     * @return the hex string.
     */
    @Override
    public String getHexString() {
        return Numeric.toHexStringNoPrefix(value);
    }

    /**
     * Gets value of this stack item.
     *
     * @return the bytes;
     */
    @Override
    public byte[] getByteArray() {
        return value;
    }

    /**
     * Gets this item's value as an integer. Treats the value as a little-endian byte array.
     *
     * @return the integer.
     */
    @Override
    public BigInteger getInteger() {
        if (value.length == 0) {
            return BigInteger.ZERO;
        }
        return BigIntegers.fromLittleEndianByteArray(value);
    }

    @Override
    public String valueToString() {
        return Numeric.toHexStringNoPrefix(value);
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

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(getValue()));
    }
}
