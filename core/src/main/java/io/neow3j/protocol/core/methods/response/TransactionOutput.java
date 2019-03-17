package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionOutput {

    @JsonProperty("n")
    @JsonAlias("N")
    private Integer index;

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

    public TransactionOutput(String assetId, String value, String address) {
        this.assetId = assetId;
        this.value = value;
        this.address = address;
    }

    public TransactionOutput(Integer index, String assetId, String value, String address) {
        this.index = index;
        this.assetId = assetId;
        this.value = value;
        this.address = address;
    }

    public Integer getIndex() {
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
        return Objects.equals(getIndex(), that.getIndex()) &&
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
