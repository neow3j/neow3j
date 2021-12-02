package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetNep11Balances extends NeoGetTokenBalances<NeoGetNep11Balances.Nep11Balances> {

    public Nep11Balances getBalances() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Balances extends NeoGetTokenBalances.TokenBalances<Nep11Balance> {

        public Nep11Balances() {
        }

        public Nep11Balances(List<Nep11Balance> balances, String address) {
            super(balances, address);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Balance extends NeoGetTokenBalances.TokenBalance {

        @JsonProperty("token")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep11Token> tokens;

        public Nep11Balance() {
        }

        public Nep11Balance(Hash160 assetHash, List<Nep11Token> tokens) {
            super(assetHash);
            this.tokens = tokens;
        }

        public List<Nep11Token> getTokens() {
            return tokens;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nep11Balance)) return false;
            if (!super.equals(o)) return false;
            Nep11Balance that = (Nep11Balance) o;
            return Objects.equals(tokens, that.tokens);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokens);
        }

        public static class Nep11Token {

            @JsonProperty("tokenid")
            private String tokenId;

            @JsonProperty("amount")
            private BigInteger amount;

            @JsonProperty("lastupdatedblock")
            private Long lastUpdatedBlock;

            public Nep11Token() {
            }

            public Nep11Token(String tokenId, BigInteger amount, Long lastUpdatedBlock) {
                this.tokenId = tokenId;
                this.amount = amount;
                this.lastUpdatedBlock = lastUpdatedBlock;
            }

            public String getTokenId() {
                return tokenId;
            }

            public BigInteger getAmount() {
                return amount;
            }

            public Long getLastUpdatedBlock() {
                return lastUpdatedBlock;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Nep11Token)) return false;
                Nep11Token that = (Nep11Token) o;
                return Objects.equals(tokenId, that.tokenId) &&
                        Objects.equals(amount, that.amount) &&
                        Objects.equals(lastUpdatedBlock, that.lastUpdatedBlock);
            }

            @Override
            public int hashCode() {
                return Objects.hash(tokenId, amount, lastUpdatedBlock);
            }
        }

    }

}