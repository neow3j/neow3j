package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TransactionOutput {

    @JsonProperty("n")
    @JsonAlias("N")
    private int index;

    @JsonProperty("asset")
    @JsonAlias("Asset")
    private String assetId;

    @JsonProperty("value")
    @JsonAlias("Value")
    private String value;

    @JsonProperty("address")
    @JsonAlias("Address")
    private String address;

    public TransactionOutput() {
    }

    public TransactionOutput(int index, String assetId, String value, String address) {
        this.index = index;
        this.assetId = assetId;
        this.value = value;
        this.address = address;
    }

    public int getIndex() {
        return index;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getValue() {
        return value;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionOutput)) return false;
        TransactionOutput that = (TransactionOutput) o;
        return getIndex() == that.getIndex() &&
                Objects.equals(getAssetId(), that.getAssetId()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getAssetId(), getValue(), getAddress());
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "index=" + index +
                ", assetId='" + assetId + '\'' +
                ", value='" + value + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
