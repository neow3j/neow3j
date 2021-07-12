package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractStorageEntry {

    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;

    public ContractStorageEntry() {
    }

    public ContractStorageEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }

    @Override
    public String toString() {
        return "ContractStorageEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
