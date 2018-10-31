package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NeoListAddress extends Response<List<NeoListAddress.Address>> {

    public List<Address> getAddresses() {
        return getResult();
    }

    public static class Address {

        @JsonProperty("address")
        private String address;
        @JsonProperty("haskey")
        private Boolean hasKey;
        @JsonProperty("label")
        private String label;
        @JsonProperty("watchonly")
        private Boolean watchOnly;

        public Address() {
        }

        public Address(String address, Boolean hasKey, String label, Boolean watchOnly) {
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
    }

}
