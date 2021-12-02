package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetNep17Balances extends Response<NeoGetNep17Balances.Nep17Balances> {

    public Nep17Balances getBalances() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Balances extends NeoGetTokenBalances.TokenBalances<Nep17Balance> {

        public Nep17Balances() {
        }

        public Nep17Balances(List<Nep17Balance> balances, String address) {
            super(balances, address);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Balance extends NeoGetTokenBalances.TokenBalance {

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("lastupdatedblock")
        private BigInteger lastUpdatedBlock;

        public Nep17Balance() {
        }

        public Nep17Balance(Hash160 assetHash, String amount, BigInteger lastUpdatedBlock) {
            super(assetHash);
            this.amount = amount;
            this.lastUpdatedBlock = lastUpdatedBlock;
        }

        public String getAmount() {
            return amount;
        }

        public BigInteger getLastUpdatedBlock() {
            return lastUpdatedBlock;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nep17Balance)) return false;
            if (!super.equals(o)) return false;
            Nep17Balance that = (Nep17Balance) o;
            return Objects.equals(amount, that.amount) &&
                    Objects.equals(lastUpdatedBlock, that.lastUpdatedBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), amount, lastUpdatedBlock);
        }
    }

}
