package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoNetworkFee {

    @JsonProperty("networkfee")
    private BigInteger networkFee;

    public NeoNetworkFee() {
    }

    public NeoNetworkFee(BigInteger networkFee) {
        this.networkFee = networkFee;
    }

    public BigInteger getNetworkFee() {
        return networkFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoNetworkFee)) {
            return false;
        }
        NeoNetworkFee that = (NeoNetworkFee) o;
        return Objects.equals(getNetworkFee(), that.getNetworkFee());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNetworkFee());
    }

    @Override
    public String toString() {
        return "NeoNetworkFee{" +
                "networkFee=" + networkFee +
                '}';
    }

}
