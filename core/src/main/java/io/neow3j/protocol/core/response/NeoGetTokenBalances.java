package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.types.Hash160;

import java.util.List;
import java.util.Objects;

public class NeoGetTokenBalances<T extends NeoGetTokenBalances.TokenBalances
        <? extends NeoGetTokenBalances.TokenBalance>> extends Response<T> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenBalances<K extends TokenBalance> {

        @JsonProperty("balance")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<K> balances;

        @JsonProperty("address")
        private String address;

        public TokenBalances() {
        }

        public TokenBalances(List<K> balances, String address) {
            this.balances = balances;
            this.address = address;
        }

        public List<K> getBalances() {
            return balances;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TokenBalances)) {
                return false;
            }
            TokenBalances<?> that = (TokenBalances<?>) o;
            return Objects.equals(balances, that.balances) &&
                    Objects.equals(address, that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(balances, address);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenBalance {

        @JsonProperty("assethash")
        private Hash160 assetHash;

        public TokenBalance() {
        }

        public TokenBalance(Hash160 assetHash) {
            this.assetHash = assetHash;
        }

        public Hash160 getAssetHash() {
            return assetHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TokenBalance)) {
                return false;
            }
            TokenBalance that = (TokenBalance) o;
            return Objects.equals(assetHash, that.assetHash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(assetHash);
        }

    }

}
