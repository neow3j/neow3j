package io.neow3j.protocol.core.methods.response;

import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.BigIntegers;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ByteArrayStackItem extends StackItem {

    public ByteArrayStackItem(byte[] value) {
        super(StackItemType.BYTE_ARRAY, value);
    }

    @Override
    public byte[] getValue() {
        return (byte[]) this.value;
    }

    /**
     * <p>Gets this byte array's value as an address.</p>
     * <br>
     * <p>Expects the byte array to be a script hash in little-endian order.</p>
     *
     * @return the address represented by this byte array.
     */
    public String getAsAddress() {
        return new ScriptHash(getValue()).toAddress();
    }

    /**
     * <p>Gets this byte array's value as string.</p>
     * <br>
     * <p>Expects the byte array to encode a string in UTF-8.</p>
     *
     * @return the string represented by the byte array.
     */
    public String getAsString() {
        return new String(getValue(), UTF_8);
    }

    /**
     * Gets this byte array's value as an integer. Expects the byte array to be in little-endian
     * order.
     *
     * @return the integer represented by the byte array.
     */
    public BigInteger getAsNumber() {
        if (getValue().length == 0) {
            return BigInteger.ZERO;
        }
        return BigIntegers.fromLittleEndianByteArray(getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ByteArrayStackItem other = (ByteArrayStackItem) o;
        return this.type == other.type && Arrays.equals(this.getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(getValue()));
    }

}
