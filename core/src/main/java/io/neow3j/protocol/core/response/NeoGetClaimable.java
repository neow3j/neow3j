package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetClaimable extends Response<NeoGetClaimable.Claimables> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Claimables getClaimables() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Claimables {

        @JsonProperty("claimable")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Claim> claims;

        @JsonProperty("address")
        private String address;

        @JsonProperty("unclaimed")
        private String totalUnclaimed;

        public Claimables() {
        }

        public Claimables(List<Claim> claims, String address, String totalUnclaimed) {
            this.claims = claims;
            this.address = address;
            this.totalUnclaimed = totalUnclaimed;
        }

        public List<Claim> getClaims() {
            return claims;
        }

        public String getAddress() {
            return address;
        }

        public String getTotalUnclaimed() {
            return totalUnclaimed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Claimables that = (Claimables) o;
            return Objects.equals(claims, that.claims) &&
                    Objects.equals(address, that.address) &&
                    Objects.equals(totalUnclaimed, that.getTotalUnclaimed());
        }

        @Override
        public int hashCode() {
            return Objects.hash(claims, address, totalUnclaimed);
        }

        @Override
        public String toString() {
            return "Claimables{" +
                    "claims=" + claims +
                    ", address='" + address + '\'' +
                    ", totalUnclaimed=" + totalUnclaimed +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Claim {

        @JsonProperty("txid")
        private String txId;

        @JsonProperty("n")
        private int index;

        @JsonProperty("value")
        private BigInteger neoValue;

        @JsonProperty("start_height")
        private BigInteger startHeight;

        @JsonProperty("end_height")
        private BigInteger endHeight;

        @JsonProperty("generated")
        private String generatedGas;

        @JsonProperty("sysfee")
        private String systemFee;

        @JsonProperty("unclaimed")
        private String unclaimedGas;

        public Claim() {
        }

        public Claim(String txId, int index, BigInteger neoValue, BigInteger startHeight, BigInteger endHeight,
                     String generatedGas, String systemFee, String unclaimedGas) {

            this.txId = txId;
            this.index = index;
            this.neoValue = neoValue;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
            this.generatedGas = generatedGas;
            this.systemFee = systemFee;
            this.unclaimedGas = unclaimedGas;
        }

        public String getTxId() {
            return txId;
        }

        public int getIndex() {
            return index;
        }

        public BigInteger getNeoValue() {
            return neoValue;
        }

        public BigInteger getStartHeight() {
            return startHeight;
        }

        public BigInteger getEndHeight() {
            return endHeight;
        }

        public String getGeneratedGas() {
            return generatedGas;
        }

        public String getSystemFee() {
            return systemFee;
        }

        public String getUnclaimedGas() {
            return unclaimedGas;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Claim claim = (Claim) o;
            return index == claim.getIndex() &&
                    startHeight.compareTo(claim.getStartHeight()) == 0 &&
                    endHeight.compareTo(claim.getEndHeight()) == 0 &&
                    Objects.equals(txId, claim.getTxId()) &&
                    neoValue.compareTo(claim.getNeoValue()) == 0 &&
                    Objects.equals(generatedGas, claim.getGeneratedGas()) &&
                    Objects.equals(systemFee, claim.getSystemFee()) &&
                    Objects.equals(unclaimedGas, claim.getUnclaimedGas());
        }

        @Override
        public int hashCode() {
            return Objects.hash(txId, index, neoValue, startHeight, endHeight, generatedGas, systemFee, unclaimedGas);
        }

        @Override
        public String toString() {
            return "Claim{" +
                    "txId='" + txId + '\'' +
                    ", index=" + index +
                    ", neoValue=" + neoValue +
                    ", startHeight=" + startHeight +
                    ", endHeight=" + endHeight +
                    ", generatedGas=" + generatedGas +
                    ", systemFee=" + systemFee +
                    ", unclaimedGas=" + unclaimedGas +
                    '}';
        }
    }
}
