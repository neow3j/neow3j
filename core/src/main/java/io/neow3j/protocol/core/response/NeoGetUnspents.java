package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class NeoGetUnspents extends Response<NeoGetUnspents.Unspents> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Unspents getUnspents() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Unspents {

        @JsonProperty("balance")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<Balance> balances;

        @JsonProperty("address")
        private String address;

        public Unspents() {
        }

        public Unspents(List<Balance> balances, String address) {
            this.balances = balances;
            this.address = address;
        }

        public List<Balance> getBalances() {
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
            if (!(o instanceof Unspents)) {
                return false;
            }
            Unspents unspents = (Unspents) o;
            return Objects.equals(getBalances(), unspents.getBalances()) &&
                    Objects.equals(getAddress(), unspents.getAddress());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getBalances(), getAddress());
        }

        @Override
        public String toString() {
            return "Unspents{" +
                    "balances=" + balances +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {

        @JsonProperty("unspent")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<UnspentTransaction> unspentTransactions;

        @JsonProperty("assethash")
        private String assetHash;

        @JsonProperty("asset")
        private String assetName;

        @JsonProperty("asset_symbol")
        private String assetSymbol;

        @JsonProperty("amount")
        private BigDecimal amount;

        public Balance() {
        }

        public Balance(List<UnspentTransaction> unspentTransactions, String assetHash, String assetName,
                String assetSymbol, BigDecimal amount) {
            this.unspentTransactions = unspentTransactions;
            this.assetHash = assetHash;
            this.assetName = assetName;
            this.assetSymbol = assetSymbol;
            this.amount = amount;
        }

        public List<UnspentTransaction> getUnspentTransactions() {
            return unspentTransactions;
        }

        public String getAssetHash() {
            return assetHash;
        }

        public String getAssetName() {
            return assetName;
        }

        public String getAssetSymbol() {
            return assetSymbol;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Balance)) {
                return false;
            }
            Balance balance = (Balance) o;
            return Objects.equals(getUnspentTransactions(), balance.getUnspentTransactions()) &&
                    Objects.equals(getAssetHash(), balance.getAssetHash()) &&
                    Objects.equals(getAssetName(), balance.getAssetName()) &&
                    Objects.equals(getAssetSymbol(), balance.getAssetSymbol()) &&
                    Objects.equals(getAmount(), balance.getAmount());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUnspentTransactions(), getAssetHash(), getAssetName(), getAssetSymbol(),
                    getAmount());
        }

        @Override
        public String toString() {
            return "Balance{" +
                    "unspentTransactions=" + unspentTransactions +
                    ", assetHash='" + assetHash + '\'' +
                    ", assetName='" + assetName + '\'' +
                    ", assetSymbol='" + assetSymbol + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnspentTransaction {

        @JsonProperty("txid")
        private String txId;

        @JsonProperty("n")
        private Integer index;

        @JsonProperty("value")
        private BigDecimal value;

        public UnspentTransaction() {
        }

        public UnspentTransaction(String txId, Integer index, BigDecimal value) {
            this.txId = txId;
            this.index = index;
            this.value = value;
        }

        public String getTxId() {
            return txId;
        }

        public Integer getIndex() {
            return index;
        }

        public BigDecimal getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UnspentTransaction)) {
                return false;
            }
            UnspentTransaction that = (UnspentTransaction) o;
            return Objects.equals(getTxId(), that.getTxId()) &&
                    Objects.equals(getIndex(), that.getIndex()) &&
                    Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTxId(), getIndex(), getValue());
        }

        @Override
        public String toString() {
            return "UnspentTransaction{" +
                    "txId='" + txId + '\'' +
                    ", index=" + index +
                    ", value=" + value +
                    '}';
        }

    }

}
