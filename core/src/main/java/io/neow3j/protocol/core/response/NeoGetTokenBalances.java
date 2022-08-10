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

        @JsonProperty("address")
        private String address;

        @JsonProperty("balance")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<K> balances;

        public TokenBalances() {
        }

        public TokenBalances(String address, List<K> balances) {
            this.address = address;
            this.balances = balances;
        }

        public String getAddress() {
            return address;
        }

        public List<K> getBalances() {
            return balances;
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
            return Objects.equals(address, that.address) &&
                    Objects.equals(balances, that.balances);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, balances);
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
