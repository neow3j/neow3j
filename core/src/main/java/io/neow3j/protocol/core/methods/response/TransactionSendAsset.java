package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.contract.Hash160;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSendAsset {

    @JsonProperty("asset")
    private Hash160 asset;

    @JsonProperty("value")
    private String value;

    @JsonProperty("address")
    private String address;

    public TransactionSendAsset() {
    }

    public TransactionSendAsset(Hash160 asset, String value, String address) {
        this.asset = asset;
        this.value = value;
        this.address = address;
    }

    public Hash160 getAsset() {
        return asset;
    }

    public String getValue() {
        return value;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionSendAsset)) {
            return false;
        }
        TransactionSendAsset that = (TransactionSendAsset) o;
        return Objects.equals(getAsset(), that.getAsset()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAsset(), getValue(), getAddress());
    }

    @Override
    public String toString() {
        return "TransactionSendAsset{" +
                "asset='" + asset + '\'' +
                ", value='" + value + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}
