package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsensusData {

    @JsonProperty("primary")
    private int primary;

    @JsonProperty("nonce")
    private String nonce;

    public ConsensusData() {
    }

    public ConsensusData(int primary, String nonce) {
        this.primary = primary;
        this.nonce = nonce;
    }

    public int getPrimary() {
        return primary;
    }

    public String getNonce() {
        return nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsensusData)) {
            return false;
        }
        ConsensusData that = (ConsensusData) o;
        return getPrimary() == that.getPrimary() &&
                Objects.equals(getNonce(), that.getNonce());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrimary(), getNonce());
    }

    @Override
    public String toString() {
        return "ConsensusData{" +
                "primary=" + primary +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
