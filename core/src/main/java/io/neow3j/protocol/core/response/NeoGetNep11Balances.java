package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;

import java.util.List;
import java.util.Objects;

public class NeoGetNep11Balances extends NeoGetTokenBalances<NeoGetNep11Balances.Nep11Balances> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Nep11Balances getBalances() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Balances extends NeoGetTokenBalances.TokenBalances<Nep11Balance> {

        public Nep11Balances() {
        }

        public Nep11Balances(String address, List<Nep11Balance> balances) {
            super(address, balances);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep11Balance extends NeoGetTokenBalances.TokenBalance {

        @JsonProperty("name")
        private String name;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("decimals")
        private String decimals;

        @JsonProperty("tokens")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Nep11Token> tokens;

        public Nep11Balance() {
        }

        public Nep11Balance(Hash160 assetHash, String name, String symbol, String decimals, List<Nep11Token> tokens) {
            super(assetHash);
            this.name = name;
            this.symbol = symbol;
            this.decimals = decimals;
            this.tokens = tokens;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getDecimals() {
            return decimals;
        }

        public List<Nep11Token> getTokens() {
            return tokens;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Nep11Balance)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Nep11Balance that = (Nep11Balance) o;
            return Objects.equals(getAssetHash(), that.getAssetHash()) &&
                    Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getSymbol(), that.getSymbol()) &&
                    Objects.equals(getDecimals(), that.getDecimals()) &&
                    Objects.equals(getTokens(), that.getTokens());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getName(), getSymbol(), getDecimals(), getTokens());
        }

        public static class Nep11Token {

            @JsonProperty("tokenid")
            private String tokenId;

            @JsonProperty("amount")
            private String amount;

            @JsonProperty("lastupdatedblock")
            private Long lastUpdatedBlock;

            public Nep11Token() {
            }

            public Nep11Token(String tokenId, String amount, Long lastUpdatedBlock) {
                this.tokenId = tokenId;
                this.amount = amount;
                this.lastUpdatedBlock = lastUpdatedBlock;
            }

            public String getTokenId() {
                return tokenId;
            }

            public String getAmount() {
                return amount;
            }

            public Long getLastUpdatedBlock() {
                return lastUpdatedBlock;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Nep11Token)) {
                    return false;
                }
                Nep11Token that = (Nep11Token) o;
                return Objects.equals(getTokenId(), that.getTokenId()) &&
                        Objects.equals(getAmount(), that.getAmount()) &&
                        Objects.equals(getLastUpdatedBlock(), that.getLastUpdatedBlock());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getTokenId(), getAmount(), getLastUpdatedBlock());
            }
        }

    }

}
