package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetNep17Balances extends Response<NeoGetNep17Balances.Nep17Balances> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Nep17Balances getBalances() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Balances extends NeoGetTokenBalances.TokenBalances<Nep17Balance> {

        public Nep17Balances() {
        }

        public Nep17Balances(List<Nep17Balance> balances, String address) {
            super(address, balances);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nep17Balance extends NeoGetTokenBalances.TokenBalance {

        @JsonProperty("name")
        private String name;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("decimals")
        private String decimals;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("lastupdatedblock")
        private BigInteger lastUpdatedBlock;

        public Nep17Balance() {
        }

        public Nep17Balance(Hash160 assetHash, String name, String symbol, String decimals, String amount,
                BigInteger lastUpdatedBlock) {
            super(assetHash);
            this.name = name;
            this.symbol = symbol;
            this.decimals = decimals;
            this.amount = amount;
            this.lastUpdatedBlock = lastUpdatedBlock;
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
            if (!super.equals(o)) {
                return false;
            }
            Nep17Balance that = (Nep17Balance) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(symbol, that.symbol) &&
                    Objects.equals(decimals, that.decimals) &&
                    Objects.equals(amount, that.amount) &&
                    Objects.equals(lastUpdatedBlock, that.lastUpdatedBlock);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name, symbol, decimals, amount, lastUpdatedBlock);
        }

    }

}
