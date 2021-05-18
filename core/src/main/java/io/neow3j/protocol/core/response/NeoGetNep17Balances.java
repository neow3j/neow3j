package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.Hash160;
import io.neow3j.protocol.core.Response;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetNep17Balances extends Response<NeoGetNep17Balances.Balances> {

    public Balances getBalances() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balances {

        @JsonProperty("balance")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep17Balance> balances;

        @JsonProperty("address")
        private String address;

        public Balances() {
        }

        public Balances(List<Nep17Balance> balances, String address) {
            this.balances = balances;
            this.address = address;
        }

        public List<Nep17Balance> getBalances() {
            return balances;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Balances)) return false;
            Balances balances1 = (Balances) o;
            return Objects.equals(getBalances(), balances1.getBalances()) &&
                    Objects.equals(getAddress(), balances1.getAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getBalances(), getAddress());
        }

        @Override
        public String toString() {
            return "Balances{" +
                    "balances=" + balances +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Balance {

        @JsonProperty("assethash")
        private Hash160 assetHash;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("lastupdatedblock")
        private BigInteger lastUpdatedBlock;

        public Nep17Balance() {
        }

        public Nep17Balance(Hash160 assetHash, String amount, BigInteger lastUpdatedBlock) {
            this.assetHash = assetHash;
            this.amount = amount;
            this.lastUpdatedBlock = lastUpdatedBlock;
        }

        public Hash160 getAssetHash() {
            return assetHash;
        }

        public String getAmount() {
            return amount;
        }

        public BigInteger getLastUpdatedBlock() {
            return lastUpdatedBlock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Nep17Balance)) {
                return false;
            }
            Nep17Balance that = (Nep17Balance) o;
            return Objects.equals(assetHash, that.assetHash) &&
                    Objects.equals(amount, that.amount) &&
                    Objects.equals(lastUpdatedBlock, that.lastUpdatedBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(assetHash, amount, lastUpdatedBlock);
        }

        @Override
        public String toString() {
            return "Nep17Balance{" +
                    "assetHash='" + assetHash + '\'' +
                    ", amount='" + amount + '\'' +
                    ", lastUpdatedBlock=" + lastUpdatedBlock +
                    '}';
        }
    }

}
