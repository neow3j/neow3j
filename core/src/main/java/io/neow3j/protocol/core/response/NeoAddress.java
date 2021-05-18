package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoAddress {

    @JsonProperty("address")
    private String address;

    @JsonProperty("haskey")
    private Boolean hasKey;

    @JsonProperty("label")
    private String label;

    @JsonProperty("watchonly")
    private Boolean watchOnly;

    public NeoAddress() {
    }

    public NeoAddress(String address, Boolean hasKey, String label, Boolean watchOnly) {
        this.address = address;
        this.hasKey = hasKey;
        this.label = label;
        this.watchOnly = watchOnly;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getHasKey() {
        return hasKey;
    }

    public void setHasKey(Boolean hasKey) {
        this.hasKey = hasKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getWatchOnly() {
        return watchOnly;
    }

    public void setWatchOnly(Boolean watchOnly) {
        this.watchOnly = watchOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NeoAddress)) {
            return false;
        }
        NeoAddress that = (NeoAddress) o;
        return Objects.equals(getAddress(), that.getAddress()) &&
                Objects.equals(getHasKey(), that.getHasKey()) &&
                Objects.equals(getLabel(), that.getLabel()) &&
                Objects.equals(getWatchOnly(), that.getWatchOnly());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getHasKey(), getLabel(), getWatchOnly());
    }

    @Override
    public String toString() {
        return "Address{" +
                "address='" + address + '\'' +
                ", hasKey=" + hasKey +
                ", label='" + label + '\'' +
                ", watchOnly=" + watchOnly +
                '}';
    }

}
