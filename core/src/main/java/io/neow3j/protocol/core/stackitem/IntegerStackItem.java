package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.StackItemType;

import java.math.BigInteger;
import java.util.Objects;

import static io.neow3j.utils.BigIntegers.toLittleEndianByteArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegerStackItem extends StackItem {

    @JsonProperty("value")
    private BigInteger value;

    public IntegerStackItem() {
        super(StackItemType.INTEGER);
    }

    public IntegerStackItem(BigInteger value) {
        super(StackItemType.INTEGER);
        this.value = value;
    }

    @Override
    public BigInteger getValue() {
        return this.value;
    }

    @Override
    protected String valueToString() {
        return value.toString();
    }

    @Override
    public boolean getBoolean() {
        nullCheck();
        if (value.equals(BigInteger.ONE)) {
            return true;
        }
        if (value.equals(BigInteger.ZERO)) {
            return false;
        }
        return super.getBoolean();
    }

    @Override
    public BigInteger getInteger() {
        nullCheck();
        return value;
    }

    @Override
    public String getString() {
        nullCheck();
        return value.toString();
    }

    /**
     * Gets this integer stack item as a hex string. I.e., the integer's bytes (in little-endian ordering) are
     * converted to a hex string.
     *
     * @return the hex string.
     */
    @Override
    public String getHexString() {
        nullCheck();
        return toHexStringNoPrefix(getByteArray());
    }

    @Override
    public byte[] getByteArray() {
        nullCheck();
        return toLittleEndianByteArray(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntegerStackItem)) {
            return false;
        }
        IntegerStackItem other = (IntegerStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

}
