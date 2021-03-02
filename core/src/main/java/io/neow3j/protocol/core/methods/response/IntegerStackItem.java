package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Objects;

import static java.lang.String.format;

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

//    @JsonSetter("value")
//    public void setValue(BigInteger value) {
//        if (value != null) {
//            this.value = value;
//        } else {
//            this.value = BigInteger.ZERO;
//        }
//    }

    @JsonValue
    public BigInteger getValue() {
        return this.value;
    }

    @Override
    public String valueToString() {
       return value.toString();
    }

    @Override
    public boolean getBoolean() {
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
        return value;
    }

    @Override
    public String getString() {
        return value.toString();
    }

    /**
     * Gets this integer stack item as a hex string. I.e., the integer's bytes (in little-endian
     * ordering) are converted to a hex string.
     *
     * @return the hex string.
     */
    @Override
    public String getHexString() {
        return Numeric.toHexStringNoPrefix(getByteArray());
    }

    @Override
    public byte[] getByteArray(){
        return BigIntegers.toLittleEndianByteArray(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerStackItem)) return false;
        IntegerStackItem other = (IntegerStackItem) o;
        return getType() == other.getType() &&
                getValue().equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }
}
