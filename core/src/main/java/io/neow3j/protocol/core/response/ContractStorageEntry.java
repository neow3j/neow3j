package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.crypto.Base64;

import java.util.Objects;

import static io.neow3j.utils.Numeric.toHexString;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractStorageEntry {

    @JsonProperty("key")
    String key;

    @JsonProperty("value")
    String value;

    /**
     * @return the key.
     */
    public byte[] getKey() {
        return Base64.decode(key);
    }

    /**
     * @return the key as hexadecimal.
     */
    public String getKeyHex() {
        return toHexString(getKey());
    }

    /**
     * @return the value.
     */
    public byte[] getValue() {
        return Base64.decode(value);
    }

    /**
     * @return the value as hexadecimal.
     */
    public String getValueHex() {
        return toHexString(getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeyHex(), getValueHex());
    }

    @Override
    public String toString() {
        return "ContractStorageEntry{" +
                "key=" + getKeyHex() +
                ", value=" + getValueHex() +
                '}';
    }

}
