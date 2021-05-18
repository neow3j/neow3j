package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSendToken {

    @JsonProperty("asset")
    private Hash160 token;

    @JsonProperty("value")
    private BigInteger value;

    @JsonProperty("address")
    private String address;

    public TransactionSendToken() {
    }

    public TransactionSendToken(Hash160 token, BigInteger value, String address) {
        this.token = token;
        this.value = value;
        this.address = address;
    }

    public Hash160 getToken() {
        return token;
    }

    public BigInteger getValue() {
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
        if (!(o instanceof TransactionSendToken)) {
            return false;
        }
        TransactionSendToken that = (TransactionSendToken) o;
        return Objects.equals(getToken(), that.getToken()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getValue(), getAddress());
    }

    @Override
    public String toString() {
        return "TransactionSendToken{" +
                "token='" + token + '\'' +
                ", value='" + value + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}
