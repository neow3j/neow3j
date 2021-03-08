package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

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
     */
    @Override
    public String getAddress() {
        nullOrEmptyCheck();
        return new ScriptHash(value).toAddress();
    }

    /**
     * Treats this item's value as a UTF-8 encoded string, i.e. reads and returns the underlying
     * bytes as a UTF-8 string.
     *
     * @return the string.
     */
    @Override
    public String getString() {
        nullOrEmptyCheck();
        return new String(value, UTF_8);
    }

    /**
     * Gets this item's value as a hexadecimal string.
     *
     * @return the hex string.
     */
    @Override
    public String getHexString() {
        nullOrEmptyCheck();
        return Numeric.toHexStringNoPrefix(value);
    }

    /**
     * Gets this item's value as a byte array.
     *
     * @return the byte array;
     */
    @Override
    public byte[] getByteArray() {
        nullOrEmptyCheck();
        return value;
    }

    /**
     * Gets this item's value as an integer.
     * <p>
     * Treats the value as a little-endian byte array.
     *
     * @return the integer.
     */
    @Override
    public BigInteger getInteger() {
        nullOrEmptyCheck();
        return BigIntegers.fromLittleEndianByteArray(value);
    }

    /**
     * Gets this item's value as a boolean.
     * <p>
     * @return true, if the value represents an integer bigger than 0. False otherwise.
     */
    @Override
    public boolean getBoolean() {
        nullOrEmptyCheck();
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
