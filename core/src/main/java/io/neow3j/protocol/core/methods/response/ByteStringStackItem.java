package io.neow3j.protocol.core.methods.response;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.StackItemType;
import io.neow3j.utils.BigIntegers;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ByteStringStackItem extends StackItem {

    @JsonProperty("value")
    private byte[] value;

    public ByteStringStackItem() {
        super(StackItemType.BYTE_STRING);
    }

    public ByteStringStackItem(byte[] value) {
        super(StackItemType.BYTE_STRING);
        this.value = value;
    }

    /**
     * TODO: Rectify the disparity between comment and method logic. Decodes the stack item's
     * base64-encoded value and returns it as a byte array.
     *
     * @return the value of this stack item.
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * Gets this item's value as an address.
     * <p>
     * Expects the byte string to be a script hash in little-endian order.
     *
     * @return the address represented by this item.
     */
    public String getAsAddress() {
        return new ScriptHash(getValue()).toAddress();
    }

    /**
     * Gets this item's value as string.
     * <p>
     * Expects the byte string to be UTF-8-encoded.
     *
     * @return the string represented by this item.
     */
    public String getAsString() {
        return new String(getValue(), UTF_8);
    }

    /**
     * Gets this item's value as a hexadecimal string.
     * <p>
     * Simply translates the underlying byte array into its corresponding hexadecimal form.
     *
     * @return the hex string represented by this item.
     */
    public String getAsHexString() {
        return Numeric.toHexStringNoPrefix(getValue());
    }

    /**
     * Gets this item's value as an integer. Expects the underlying bytes to be in little-endian
     * order.
     *
     * @return the integer represented by this item.
     */
    public BigInteger getAsNumber() {
        if (getValue().length == 0) {
            return BigInteger.ZERO;
        }
        return BigIntegers.fromLittleEndianByteArray(getValue());
    }

    /**
     * Deserializes this byte array into the given class.
     *
     * @param clazz The class to deserialize to.
     * @return the deserialized JSON content of the byte array's value.
     * @throws IOException if an error occurs when trying to deserialize to the given class.
     */
    public <T> T getAsJson(Class<T> clazz) throws IOException {
        String json = new String(getValue(), UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
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

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(getValue()));
    }
}
