package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static io.neow3j.utils.ArrayUtils.reverseArray;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a stack item that has a value of type byte array.
 * <p>
 * This abstraction does not represent an actual stack item type form the neo-vm.
 */
abstract class ByteArrayStackItem extends StackItem {

    @JsonProperty("value")
    private byte[] value;

    protected ByteArrayStackItem(StackItemType type) {
        super(type);
    }

    protected ByteArrayStackItem(byte[] value, StackItemType type) {
        super(type);
        this.value = value;
    }

    /**
     * Returns this item's value as a byte array.
     *
     * @return the byte array.
     */
    @Override
    public byte[] getValue() {
        return this.value;
    }

    /**
     * Gets this item's value as an address.
     * <p>
     * Treats the underlying bytes as a script hash in little-endian order.
     *
     * @return the address.
     * @throws StackItemCastException if this stack item's value is null or it is not a valid Neo address.
     */
    @Override
    public String getAddress() {
        nullCheck();
        try {
            return new Hash160(reverseArray(value)).toAddress();
        } catch (IllegalArgumentException e) {
            throw new StackItemCastException(e);
        }
    }

    /**
     * Treats this item's value as a UTF-8 encoded string, i.e. reads and returns the underlying bytes as a UTF-8
     * string.
     *
     * @return the string.
     * @throws StackItemCastException if this stack item's value is null.
     */
    @Override
    public String getString() {
        nullCheck();
        return new String(value, UTF_8);
    }

    /**
     * Gets this item's value as a hexadecimal string.
     *
     * @return the hex string.
     * @throws StackItemCastException if this stack item's value is null.
     */
    @Override
    public String getHexString() {
        nullCheck();
        return Numeric.toHexStringNoPrefix(value);
    }

    /**
     * Gets this item's value as a byte array.
     *
     * @return the byte array;
     * @throws StackItemCastException if this stack item's value is null.
     */
    @Override
    public byte[] getByteArray() {
        nullCheck();
        return value;
    }

    /**
     * Gets this item's value as an integer.
     * <p>
     * Treats the value as a little-endian byte array.
     *
     * @return the integer.
     * @throws StackItemCastException if this stack item's value is null or an empty.
     */
    @Override
    public BigInteger getInteger() {
        nullCheck();
        try {
            return BigIntegers.fromLittleEndianByteArray(value);
        } catch (NumberFormatException e) {
            throw new StackItemCastException(e);
        }
    }

    /**
     * Gets this item's value as a boolean.
     *
     * @return true if the value represents an integer bigger than 0. False otherwise.
     * @throws StackItemCastException if this stack item's value is null or an empty.
     */
    @Override
    public boolean getBoolean() {
        nullCheck();
        return getInteger().compareTo(BigInteger.ZERO) > 0;
    }

    @Override
    protected String valueToString() {
        return Numeric.toHexStringNoPrefix(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(getValue()));
    }

}
