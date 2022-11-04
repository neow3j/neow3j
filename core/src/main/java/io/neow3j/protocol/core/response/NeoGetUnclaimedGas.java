package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.Objects;

public class NeoGetUnclaimedGas extends Response<NeoGetUnclaimedGas.GetUnclaimedGas> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public GetUnclaimedGas getUnclaimedGas() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetUnclaimedGas {

        @JsonProperty("unclaimed")
        private String unclaimed;

        @JsonProperty("address")
        private String address;

        public GetUnclaimedGas() {
        }

        public GetUnclaimedGas(String unclaimed, String address) {
            this.unclaimed = unclaimed;
            this.address = address;
        }

        public String getUnclaimed() {
            return unclaimed;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GetUnclaimedGas)) {
                return false;
            }
            GetUnclaimedGas that = (GetUnclaimedGas) o;
            return Objects.equals(getUnclaimed(), that.getUnclaimed()) &&
                    Objects.equals(getAddress(), that.getAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUnclaimed(), getAddress());
        }

        @Override
        public String toString() {
            return "GetUnclaimedGas{" + "unclaimed=" + unclaimed + ", address=" + address + "}";
        }

    }

}
