package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.neow3j.model.types.TransactionAttributeType;
import io.neow3j.utils.Numeric;

import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAttribute {

    @JsonProperty("usage")
    public TransactionAttributeType usage;

    @JsonProperty("data")
    private byte[] data;

    public TransactionAttribute() {
    }

    public TransactionAttribute(TransactionAttributeType usage, byte[] data) {
        this.usage = usage;
        this.data = data;
    }

    public TransactionAttribute(TransactionAttributeType usage, String data) {
        this.usage = usage;
        this.data = (data != null ? Numeric.hexStringToByteArray(data) : null);
    }

    @JsonGetter
    public TransactionAttributeType getUsage() {
        return usage;
    }

    public byte[] getDataAsBytes() {
        return this.data;
    }

    @JsonGetter
    public String getData() {
        return this.data != null ? Numeric.toHexString(data) : null;
    }

    @JsonSetter
    public void setData(String data) {
        this.data = Numeric.hexStringToByteArray(data);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionAttribute)) return false;
        TransactionAttribute that = (TransactionAttribute) o;
        return getUsage() == that.getUsage() &&
                Arrays.equals(getDataAsBytes(), that.getDataAsBytes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getUsage());
        result = 31 * result + Arrays.hashCode(getDataAsBytes());
        return result;
    }

    @Override
    public String toString() {
        return "TransactionAttribute{" +
                "usage=" + usage +
                ", data=" + (data != null ? Numeric.toHexStringNoPrefix(data) : "null") +
                '}';
    }
}